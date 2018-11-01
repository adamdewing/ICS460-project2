package com.metrostate.ics460.project2.sender;

import java.io.IOException;
import java.util.Scanner;

public class Sender {

	private Loader loader = new FileLoader();
	private DataSender dataSender = new UDPDataSender();

	public static void main(String[] args) throws IOException {
		Sender sender = new Sender();
		sender.start();
	}

	public void start() {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Starting Transfer Data");
		System.out.println("+ ======================================================= +");
		
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please provide ip address: ");
		String ipAddress = scanner.next();
		System.out.print("Please provide port number: ");
		int port = scanner.nextInt();
		System.out.print("Enter packet size: ");
		int packetSize = scanner.nextInt();
		System.out.print("Enter window size: ");
		int windowSize = scanner.nextInt();
		System.out.print("Enter timeout interval: ");
		int timeout = scanner.nextInt();
		System.out.println("");
		byte[] bytes = loader.loadData();

		dataSender.sendData(bytes, packetSize, timeout, ipAddress, port, windowSize);

		scanner.close();
	}

}
