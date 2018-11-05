package com.metrostate.ics460.project2.sender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.metrostate.ics460.project2.packet.Packet;

public class UDPDataSender implements DataSender {

	// Probability of loss during packet sending
	private static final double ERROR_PROBABILITY = 0.1;
	// Datagram for received packet
	private DatagramPacket recievePacket = null;
	// Datagram for sent packet
	private DatagramPacket sendPacket = null;
	private int lastSequNum = 1;
	// Sequence number of the last acknowledged packet
	private int packetWaitingForAck;
	// A position of packet is being sent which is (in windowBuffer)
	int packetBufferIndex = 0;


	@Override
	public void sendData(byte[] bytes, int packet_size, int timeout, String ipAddress, int port, int windowSize) {
		// Get an input file data
		FileLoader file_loader = new FileLoader();

		// Add byte[] into a list by split byte to smaller chunks byte 
		List<byte[]> byteList = byteArrayToChunks(file_loader.loadData(), packet_size);

		// For Debugging purpose
		System.out.println("Debugging a byte[] in the list");
		byteList.forEach(items->System.out.print(byteList.toString() + " "));

		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(port)) {
			socket.setSoTimeout(timeout);
			
			// Receiver address
			InetAddress host_ip = InetAddress.getByName(ipAddress);

			// Set to slide window size
			packetWaitingForAck = windowSize;
			
			// Convert byteList to a List of Packet Object and add into a list
			List<Packet> sent_packet_list = byteListToPacketList(byteList);
			
			// Total number of packets
			int totalPackets = sent_packet_list.size();

			// Create an array of packet for slide window buffer
			Packet[] windowBuffer = new Packet[windowSize];

			// Loop controller
			boolean loop = true;
			while(loop) {
				
				while(lastSequNum - packetWaitingForAck  <= windowBuffer.length && lastSequNum <= totalPackets) {
					// A byte array to store serialized packet to send
					byte[] packet_out = SerializeObject.serializePacketObject(sent_packet_list);
					
					// Create datagram packet to send
					sendPacket = new DatagramPacket(packet_out, packet_out.length, host_ip, port);
					
					// Get packet object to determine index of the packet in window buffer
					Packet packetIndex  = new Packet();
					windowBuffer[lastSequNum] = packetIndex;
					

					// Subtract window size from a total packet
					totalPackets = totalPackets - windowSize;
					lastSequNum++;

					System.out.println(" ");
					// Send all the packet in windowBuffer
					for (int i = 0; i < windowBuffer.length; i++) {

						// For the last packet
						if (totalPackets > 0 && totalPackets > packet_size) {
							byte[] remainingPacket = new byte[totalPackets];
							Packet remainPacketIndex = new Packet();
							windowBuffer[lastSequNum] = remainPacketIndex;
							DatagramPacket sendLastPacket = new DatagramPacket(remainingPacket, remainingPacket.length, host_ip, port);
							System.out.println("Sending last packet " + remainingPacket + "  " + remainingPacket.length);
							socket.send(sendLastPacket);
							totalPackets = 0;
						}

						byte[] data_in = new byte[packet_size];
						// Create a datagram packet object for incoming datagrams packets
						DatagramPacket recieve_packet = new DatagramPacket(data_in , data_in.length);
						
						if(isPacketAcknowledge(recieve_packet, port, packetBufferIndex)) {				
							System.out.println("Sending packets #: " +  i  + " " + packet_out + " " + packet_out.length);
							socket.send(sendPacket);
							windowSize++;
						}

					}
					
				}
			}

		}
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}



	/**
	 * Split byte[] to smaller chunks
	 */
	private List<byte[]> byteArrayToChunks(byte[] bytes, int packet_size) {

		List<byte[]> byteList = new ArrayList<byte[]>();

		for (int i = 0; i < bytes.length; i += packet_size) {
			byte[] chunk_bytes = Arrays.copyOfRange(bytes,  i,  i + packet_size);

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

	private boolean isPacketAcknowledge(DatagramPacket datagramPacket, int port, int seq) throws IOException {
		DatagramSocket socket = new DatagramSocket(port);
		byte[] recivedByte = new byte[1024];
		datagramPacket = new DatagramPacket(recivedByte, recivedByte.length);
		// Receive incoming datagrams packets
		socket.receive(datagramPacket);
		Packet packet = new Packet();
		try {
			packet = SerializeObject.deserializePacketObject(recivedByte);
			if(seq == packet.getSeqno()) {
				return true;
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	// Private class for Serializing Object
	private static class SerializeObject {

		// Convert an object to byte array
		private static byte[] serializePacketObject(List<Packet> sent_packet_list) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(bos);
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				oos.writeObject(sent_packet_list);
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
