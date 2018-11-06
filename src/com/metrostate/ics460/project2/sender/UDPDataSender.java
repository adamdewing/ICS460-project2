package com.metrostate.ics460.project2.sender;

import com.metrostate.ics460.project2.NetworkLogger;
import com.metrostate.ics460.project2.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UDPDataSender implements DataSender {

	private static final NetworkLogger log = new NetworkLogger();

	private static final int port = 13;
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
	public void sendData(byte[] bytes, int packet_size, int timeout, String receiverIpAddress, int receiverPort, int windowSize) {

		// Add byte[] into a list by split byte to smaller chunks byte
		List<byte[]> byteList = byteArrayToChunks(bytes, packet_size);


		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(port)) {
			socket.setSoTimeout(timeout);

			// Receiver address
			InetAddress receiverInetAddress = InetAddress.getByName(receiverIpAddress);

			// Set to slide window size
			packetWaitingForAck = windowSize;

			// Convert byteList to a List of Packet Object and add into a list
			List<Packet> sent_packet_list = byteListToPacketList(byteList);
			sent_packet_list.forEach(packet -> System.out.println(packet));
			int nextFrameExpected = 1;
			boolean[] isSent = new boolean[sent_packet_list.size() + 1];
			boolean[] hasBeenSentAlready =  new boolean[sent_packet_list.size() + 1];
			while(true){
				// Iterate over sliding window to see which packets need to be sent
				for(int i = nextFrameExpected; i < nextFrameExpected + windowSize; i++){
					if(i < isSent.length && !isSent[i]){
						Packet packet = sent_packet_list.get(i - 1);
						packet.setAckno(nextFrameExpected);
						byte[] data = UDPDataSender.SerializeObject.serializePacketObject(packet);
						DatagramPacket datagramPacket = new DatagramPacket(data, data.length, receiverInetAddress, receiverPort);
						System.out.println("Getting ready to send packet " + packet);
						socket.send(datagramPacket);
						log.logSendPacket(hasBeenSentAlready[i], packet, packet_size, NetworkLogger.DatagramCondition.SENT);
						isSent[i] = true;
						hasBeenSentAlready[i] = true;

					}else{
						// We already sent this packet so do nothing
					}
				}
				DatagramPacket receiveDatagramPacket = new DatagramPacket(new byte[1024], 1024);
				try {
					socket.receive(receiveDatagramPacket);
					Packet receivePacket = UDPDataSender.SerializeObject.deserializePacketObject(receiveDatagramPacket.getData());
					nextFrameExpected = receivePacket.getAckno();
					System.out.println("Received Ack packet of " + receivePacket);
					if(nextFrameExpected > sent_packet_list.size()){
						return;
					}

				} catch (SocketTimeoutException e) {
					// Need to resend the first packet we are waiting on
					isSent[nextFrameExpected] = false;
				} catch (ClassNotFoundException e) {
					// Could not deserialize.  Could mean a network error.  Just eat error and keep waiting for another packet.
					System.out.println("Warning, could not deserialize packet!");
				}
			}


		}catch (IOException e) {
			System.err.println();
			e.printStackTrace();
		}

	}


	/**
	 * Split byte[] to smaller chunks
	 */
	private List<byte[]> byteArrayToChunks(byte[] bytes, int packet_size) {

		List<byte[]> byteList = new ArrayList<byte[]>();

		byte[] chunk_bytes;
		for (int i = 0; i < bytes.length; i += packet_size) {
			if(i + packet_size > bytes.length){
				chunk_bytes = Arrays.copyOfRange(bytes,  i,  bytes.length);
			}else{
				chunk_bytes = Arrays.copyOfRange(bytes,  i,  i + packet_size);
			}
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

		// Add final packet with no data to let receiver know we are done.
		Packet packet = new Packet();
		packet.setCksum((short)0);
		packet.setLen((short)12);
		packet.setSeqno(packet_list.size() + 1);
		packet.setData(new byte[0]);
		packet_list.add(packet);

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

			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bytes;
		}

		private static Packet deserializePacketObject(byte[] ackPacket) throws ClassNotFoundException, IOException {
			Packet packet;
			ByteArrayInputStream bais;
			ObjectInputStream ois = null;
			try {
				bais = new ByteArrayInputStream(ackPacket);
				ois = new ObjectInputStream(bais);

				packet = (Packet) ois.readObject();
			} finally {
				if(ois != null){
					ois.close();
				}
			}
			return packet;
		}
	}


}
