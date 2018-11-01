package com.metrostate.ics460.project2.sender;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.metrostate.ics460.project2.Packet;

public class UDPDataSender implements DataSender {

	// Datagram for sent packet
	private DatagramPacket sendPacket = null;
	// Datagram for received packet
	private DatagramPacket recievePacket = null;
	// Probability of loss during packet sending
	private static final double ERROR_PROBABILITY = 0.1;
	private int totalPackets;
	// Sequence number of the last packet sent
	private int seqNum = 1;
	// Sequence number of the last acknowledged packet
	private int waitingForAck = 0;  // TODO you need to initialize this to windowSize
	// Last packet sequence number
	private int packetSeq = 0;
	// A position of packet is being sent which is (in windowBuffer)
	private int windowIndex;
	// Create an array of packet for slide window buffer
	private Packet[] windowBuffer;


	@Override
	public void sendData(byte[] bytes, int packet_size, int timeout, String ipAddress, int port, int windowSize) {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Started To Send Data");
		System.out.println("+ ======================================================= +");

		// List of all the packets to sent which is not yet been acknowledged in a buffer
		List<Packet> sent_packet_list = new ArrayList<Packet>();
		// Total number of packets
		totalPackets = sent_packet_list.size();

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

			// Convert byteList to a List of Packet Object
			sent_packet_list = byteListToPacketList(byteList);

			// A byte array to store serialized packet to send
			byte[] packet_out = SerializeObject.serializePacketObject((Packet) sent_packet_list);

			while(true) {
				// Create datagram packet to send
				sendPacket = new DatagramPacket(packet_out, packet_out.length, host_ip, port);

				// Create an array of packet for slide window buffer
				windowBuffer = new Packet[windowSize];

				while(totalPackets >= windowSize) {

					// Create packet with its index position
					Packet packetPosition = new Packet();
					windowBuffer[seqNum] = packetPosition;

					// Subtract window size from a total packet
					totalPackets = totalPackets - windowSize;
					seqNum++;

					if(windowIndex == totalPackets) {
						break;
					}
				}
				// Send all the packet in windowBuffer
				for (int j = 0; j <= windowIndex; j++) {
					System.out.println("Sending " + packet_out + "  " + packet_out.length + " bytes to client " + windowBuffer[j]);

				}
			}

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
