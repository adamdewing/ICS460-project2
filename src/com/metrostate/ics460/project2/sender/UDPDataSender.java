package com.metrostate.ics460.project2.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.metrostate.ics460.project2.Packet;

public class UDPDataSender implements DataSender {

	private Scanner scanner = new Scanner(System.in);
	private DatagramPacket send_packet = null;
	private int SEQUENCE_NUMBER = -1;
	private long end_time, start_time;
	private int squence_num;
	private int total_packets;
	private int next_squence_num;
	private int base_squence_num;
	private double bitErrorProbability;
	private int i;
	private String protocolName;

	@Override
	public void sendData(byte[] bytes, int packetSize, long timeout, String ipAddress, int port, int window_size) {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Started To Send Data");
		System.out.println("+ ======================================================= +");

		ArrayList<Packet> sent_packet_list = new ArrayList<>();

		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(0)) {
			socket.setSoTimeout(3000);
			// NetworkConnection.instance().getConnection() socket = new DatagramSocket();
			// socket = NetworkConnection.instance().getConnection();
			// Get sender’s address and port number from the datagram
			InetAddress host_ip = InetAddress.getByName(ipAddress);

			Integer squence_num = SEQUENCE_NUMBER;
			int packets_sent = 0;
			int bytes_sent = 0;

			// Get an input file
			FileLoader data_sender = new FileLoader();
			// Create a buffer to store the incoming datagrams packets
			byte[] data_out = data_sender.loadData();
			boolean timedOut = true;
			while (timedOut) {
				squence_num++;

				try {

					DatagramPacket sendPacket = new DatagramPacket(data_out, data_out.length, host_ip, port);
					// Send it to the receiver socket
					socket.send(sendPacket);

					// Create a datagram packet object for outgoing datagrams packets
					send_packet = new DatagramPacket(data_out, data_out.length, host_ip, port);

					// Send datagram packets to a receiver server
					socket.send(send_packet);
					packets_sent++;

					// timestamp = System.currentTimeMillis();

					System.out.println(
							"\nSENT PACKET #: " + packets_sent + "\tBYTE (" + (bytes_sent + data_out.length - 1) + " - "
									+ (bytes_sent + data_out.length) + ")" + "\tSEQUENCE #: SEQ-" + squence_num);

					byte[] data_in = new byte[packetSize];

					// Create a datagram packet object for incoming datagrams packets
					DatagramPacket recieve_packet = new DatagramPacket(data_in, data_in.length);

					// Receive incoming datagrams packets
					socket.receive(recieve_packet);

					// Receive acknowledgment message from the Server
					String result = new String(data_in, 0, recieve_packet.getLength());
					System.out.println("\nACKNOWLEDGMENT FROM HOSTNAME: " + recieve_packet.getAddress().getHostAddress()
							+ " PORT #: " + recieve_packet.getPort() + " " + result);

				} catch (IOException e) {
					// If client don't get an acknowledgment, re-send sequence number
					System.out.println("TIMEDOUT FOR SEQUENCE NUMBER:\t" + squence_num);
					squence_num--;
					e.printStackTrace();
				}

			}
		} catch (SocketException | UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
