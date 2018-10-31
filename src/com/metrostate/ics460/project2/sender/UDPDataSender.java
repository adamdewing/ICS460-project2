package com.metrostate.ics460.project2.sender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.metrostate.ics460.project2.Packet;

public class UDPDataSender implements DataSender {

	// Datagram for sent packet
	private DatagramPacket send_packet = null;
	// Datagram for received packet
	private DatagramPacket recieve_packet = null;
	// Probability of loss during packet sending
	private static final double PROBABILITY = 0.1;
	// Sequence number of the last packet sent
	private int last_seq_num = 0;
	// Sequence number of the last acknowledged packet
	private int waitingForAck = 0;
	// Last packet sequence number
	private int last_packet_seq = 0;

	// TODO change all variables with underscores to camel case.  So  packet_size to pacetkSize and window_size to windowSize to conform to java naming standards
	@Override
	public void sendData(byte[] bytes, int packet_size, long timeout, String ipAddress, int port, int window_size) {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Started To Send Data");
		System.out.println("+ ======================================================= +");

		// List of all the packets sent
		List<Packet> sent_packet_list = new ArrayList<Packet>();

		// Get an input file data
		FileLoader file_loader = new FileLoader();
		List<byte[]> byteList = byteArrayToChunks(file_loader.loadData(), packet_size);
		
		// TODO IMPORTANT!!!! Suggestion : create a method that takes the byteList and returns a List<Packet>.
		sent_packet_list = byteListToPacketList(byteList, bytes, window_size, window_size);

		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(0)) {
			// Default timeout
			// socket.setSoTimeout(3000);

			// Receiver address
			InetAddress host_ip = InetAddress.getByName(ipAddress);

			// Create a buffer to store the incoming datagrams packets
			for (byte[] data_out : byteList) {

				// Packet sending while loop
				while (last_seq_num - waitingForAck < window_size && last_seq_num < last_packet_seq) {

					// Array to store a part of the bytes to send
					byte[] bytes_to_send = new byte[packet_size];

					// Copy segment of data bytes to array
					bytes_to_send = Arrays.copyOfRange(data_out, last_seq_num * packet_size,
							last_seq_num * packet_size + packet_size);  // TODO this won't work.  This will cause an index out of range error.

					// Create packet object
					Packet packet = new Packet();

					// Serialize the RDTPacket object
					byte[] sendData = SerializeObject.objectToByteArray(packet);

					// Create datagram packet to send
					DatagramPacket sendPacket = new DatagramPacket(data_out, data_out.length, host_ip, port);  // TODO your DatagramPacket should be sending the sendData, which is the serialized packet object.  The data_out is the raw file data.

					System.out.println("Sending packet with sequence number " + last_packet_seq + " and size "
							+ sendPacket.getLength() + " bytes");

					// Send a packet
					socket.send(sendPacket);

					// Send with some probability of loss
					if (Math.random() > PROBABILITY) {
						socket.send(sendPacket);
					} else {
						System.out.println("[X] Lost packet with sequence number " + last_packet_seq);
					}
					// Increase the last sent
					last_seq_num++;

				}

				// Byte array for the ACK sent by the receiver
				byte[] ack_bytes = new byte[40];

				// Creating packet for acknowledgment
				DatagramPacket acknowledgment = new DatagramPacket(ack_bytes, ack_bytes.length);

				try {
					// If an acknowledgement was not received in the time specified (continues on
					// the catch
					// clause)
					socket.setSoTimeout((int) timeout);

					// Receive the packet
					socket.receive(acknowledgment);

					// Unserialize the Acknowledged object
					Packet ack_packet = (Packet) SerializeObject
							.toObject(acknowledgment.getData());

					System.out.println("Received ACK for " + ack_packet.getAckno());

					// If this acknowledged is for the last packet, stop the sender
					if (ack_packet.getAckno() == last_packet_seq) {
						break;
					}

					waitingForAck = Math.max(waitingForAck, ack_packet.getAckno());

				} catch (SocketTimeoutException e) {
					// then send all the sent but non-acknowledged packets

					for (int i = waitingForAck; i < last_seq_num; i++) {

						// Serialize the RDTPacket object
						byte[] sendData = SerializeObject.objectToByteArray(sent_packet_list.get(i));

						// Create the packet
						DatagramPacket packet = new DatagramPacket(sendData, sendData.length, host_ip, port);

						// Send with some probability
						if (Math.random() > PROBABILITY) {
							try {
								socket.send(packet);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} else {
							System.out.println("[X] Lost packet with sequence number " + sent_packet_list.get(i).getSeqno());
						}

						System.out.println("Resending packet with sequence number " + sent_packet_list.get(i).getSeqno()
								+ " and size " + sendData.length + " bytes");
					}
				}

			}

			System.out.println("Finished transmission");

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Split byte[] to smaller chunks
	 */
	private List<byte[]> byteArrayToChunks(byte[] bytes, int packet_size) {

		List<byte[]> byteList = new ArrayList<byte[]>();
		  // TODO PACKET_SIZE should be replaced by a passed in parameter packetSize.  500 is the max packetSize.  The receiving class will break if you pass 1024 data bytes like this.

		for (int i = 0; i < bytes.length; i += packet_size) {
			byte[] chunk_bytes = Arrays.copyOfRange(bytes, i, i + packet_size);

			byteList.add(chunk_bytes);
		}

		return byteList;
		
	}
	
	// Convert byteList to a List<Packet Object>.
	private static List<Packet> byteListToPacketList(List<byte[]> byteList, byte[] data, int seqno, int ackno){
		List<Packet> packet_list = new ArrayList<Packet>();
		for(int i = 0; i < byteList.size(); i++) {
			Packet packet = new Packet();
			packet.setCksum((short) 0);
			packet.setLen((short) (data.length + 12));
			packet.setAckno(ackno);
			packet.setSeqno(seqno);
			packet.setData(data);
			
			packet_list.add(packet);
		}
		return packet_list;
		
	}
	

	// Private class for Serializing Object
	private static class SerializeObject {

		// Convert an object to byte array
		private static byte[] objectToByteArray(Packet packet2) {

			Packet packet = new Packet();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;

			try {

				oos = new ObjectOutputStream(bos);
				oos.writeObject(packet);

			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				oos.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] data = bos.toByteArray();

			return data;
		}

		private static Object toObject(byte[] bytes) {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = null;
			try {
				ois = new ObjectInputStream(bais);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				return ois.readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return bytes;
		}

	}

}
