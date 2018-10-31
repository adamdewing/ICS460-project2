package com.metrostate.ics460.project2.sender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import com.metrostate.ics460.project2.Packet;

public class UDPDataSender implements DataSender {

	// Datagram for sent packet
	private DatagramPacket sendPacket = null;
	// Datagram for received packet
	private DatagramPacket recievePacket = null;
	// Probability of loss during packet sending
	private static final double ERROR_PROBABILITY = 0.1;
	private int totalPackets = 0;
	// Sequence number of the last packet sent
	private int seqNum = 0;
	// Sequence number of the last acknowledged packet
	private int waitingForAck = 0;
	// Last packet sequence number
	private int packetSeq = 0;
	// List for windows
	private LinkedList<Packet> WindowsList;

	@Override
	public void sendData(byte[] bytes, int packet_size, int timeout, String ipAddress, int port, int windowSize) {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Started To Send Data");
		System.out.println("+ ======================================================= +");

		// List of all the packets to sent not yet been acknowledged in a buffer
		List<Packet> sent_packet_list = new ArrayList<Packet>();

		// Get an input file data
		FileLoader file_loader = new FileLoader();
		// Add byte[] into a list by split byte to smaller chunks byte 
		List<byte[]> byteList = byteArrayToChunks(file_loader.loadData(), packet_size);

		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(port)) {
			// Default timeout
			socket.setSoTimeout(timeout);

			// Receiver address
			InetAddress host_ip = InetAddress.getByName(ipAddress);

			// Create a buffer to store the incoming datagrams packets
			for (byte[] bytes_to_send : byteList) {

				// Packet sending while loop
				while (seqNum - waitingForAck < windowSize && seqNum < packetSeq) {
					
					// Convert byteList to a List of Packet Object
					sent_packet_list = byteListToPacketList(byteList);
					// Total number of packets
					
					// A byte array to store serialized packet to send
					byte[] packet_out = SerializeObject.serializePacketObject((Packet) sent_packet_list);
					
					totalPackets = sent_packet_list.size();
	
					// Create datagram packet to send
					DatagramPacket sendPacket = new DatagramPacket(packet_out, packet_out.length, host_ip, port);
					System.out.println("Sending packet with sequence number " + packetSeq + " and size "
							+ sendPacket.getLength() + " bytes");
					
					// Send with some bit error probability of bit losses
					if (Math.random() > ERROR_PROBABILITY) {
						socket.send(sendPacket);

					} else {

						System.out.println("[X] Lost packet with sequence number " + packetSeq);
					}

					// Copy a part of the bytes to send into byte array
					byte[] sentPacketCopy = Arrays.copyOfRange(packet_out, seqNum * packet_size,
							seqNum * packet_size + packet_size);

					// Send a packet
					socket.send(sendPacket);

					// Increase the last sent sequence number
					seqNum++;

				}

				// Byte array for acknowledged packet object sent by the receiver
				byte[] ack_bytes = new byte[0];

				// Creating datagram packet for acknowledgment
				DatagramPacket ackPacket = new DatagramPacket(ack_bytes, ack_bytes.length);

				try {
					// Timeout will trigger if an acknowledgement was not received in the time specified
					socket.setSoTimeout(timeout);

					// Receive the packet
					socket.receive(ackPacket);

					// deserialize the acknowledged packet object
					Packet acknPacket = SerializeObject.deserializePacketObject(ack_bytes);

					System.out.println("Received ACK for " + acknPacket.getAckno());

					// If this acknowledged is for the last packet, stop the sender
					if (acknPacket.getAckno() == packetSeq) {
						
						break;
					}

					waitingForAck = Math.max(waitingForAck, acknPacket.getAckno());

				} catch (SocketTimeoutException | ClassNotFoundException e) {
					// Re-send all non-acknowledged packets
					for (int i = waitingForAck; i < seqNum; i++) {

						// Serialize the Packet object
						byte[] sendData = SerializeObject.serializePacketObject(sent_packet_list.get(i));

						// Create the packet
						DatagramPacket packet = new DatagramPacket(sendData, sendData.length, host_ip, port);

						// Send with some probability
						if (Math.random() > ERROR_PROBABILITY) {
							try {
								socket.send(packet);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						} else {
							System.out.println(
									"[X] Lost packet with sequence number " + sent_packet_list.get(i).getSeqno());
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

		for (int i = 0; i < bytes.length; i += packet_size) {
			byte[] chunk_bytes = Arrays.copyOfRange(bytes, i, i + packet_size);

			byteList.add(chunk_bytes);
		}

		return byteList;

	}

	// Convert byteList to a List<Packet Object>
	private static List<Packet> byteListToPacketList(List<byte[]> byteList) {
		List<Packet> packet_list = new ArrayList<Packet>();
		for (int i = 0; i < byteList.size(); i++) {
			Packet packet = new Packet();
			byte[] bytes = byteList.get(i);
			packet.setCksum((short) 0);
			packet.setLen((short) (bytes.length + 12));
			packet.setSeqno(i + 1);
			packet.setData(bytes);

			packet_list.add(packet);
		}

		return packet_list;

	}

	// Private class for Serializing Object
	private static class SerializeObject {

		// Convert an object to byte array
		private static byte[] serializePacketObject(Packet packet) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(bos);
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				oos.writeObject(packet);
			} catch (IOException e) {

				e.printStackTrace();
			}

			try {
				oos.flush();
			} catch (IOException e) {

				e.printStackTrace();
			}

			byte[] bytes = bos.toByteArray();

			return bytes;
		}

	private static Packet deserializePacketObject(byte[] ackPacket) throws ClassNotFoundException, IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(ackPacket);
		ObjectInputStream ois = new ObjectInputStream(bais);
		
		return (Packet) ois.readObject();

	}
	}
	

}
