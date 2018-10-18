package com.metrostate.ics460.project2.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class UDPDataSender implements DataSender {

	private Scanner scanner = new Scanner(System.in);
	private final int SEQUENCE_NUMBER = -1;
	private int packetSize;

	@Override
	public void sendData(byte[] bytes, int packetSize, long timeout, String ipAddress, int port, int windowSize,
			int errorRate) {

		List<byte[]> byteList = splitBytes(bytes);

		// Create server socket
		try (DatagramSocket client_socket = new DatagramSocket(0)) {
			InetAddress host = InetAddress.getByName(ipAddress);
			client_socket.setSoTimeout((int) timeout);

			int packets_sequence = SEQUENCE_NUMBER;
			int bytesSent = 0;

			for (byte[] packetBytes : byteList) {
				DatagramPacket sent = new DatagramPacket(packetBytes, packetBytes.length, host, port);
				client_socket.send(sent);
				boolean loop = true;
				while (loop) {
					packets_sequence++;

					System.out.println("Sending packet number: " + packets_sequence + ", bytes: " + bytesSent + " - "
							+ (bytesSent + packetBytes.length - 1));
					bytesSent += packetBytes.length;

					byte[] received_data = new byte[1024];
					DatagramPacket received = new DatagramPacket(received_data, received_data.length);
					client_socket.receive(received);

					String ackn_message = new String(packetBytes, 0, received_data.length);
					System.out.println(ackn_message + "\t" + packets_sequence);
					loop = false;

				}
				
				System.out.println("Timeout to send the packet\t" + packets_sequence);
				packets_sequence--;

			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println("Total number of packets received: " + byteList.size());
	}

	private List<byte[]> splitBytes(byte[] bytes) {
		List<byte[]> byteList = new ArrayList<byte[]>();
		for (int i = 0; i < bytes.length; i += packetSize) {
			byte[] b = Arrays.copyOfRange(bytes, i, i + packetSize);
			byteList.add(b);
		}
		return byteList;
	}

}
