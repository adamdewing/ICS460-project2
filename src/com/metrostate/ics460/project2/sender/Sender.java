package com.metrostate.ics460.project2.sender;

import com.metrostate.ics460.project2.gui.UDPGUI;

import java.io.IOException;
import java.util.Scanner;

public class Sender {

	private Loader loader = new FileLoader();
	private DataSender dataSender = new UDPDataSender();

	public static void main(String[] args) throws IOException {
//		try {
//
//			UDPGUI.main(args);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Sender sender = new Sender();
		sender.start();
	}

	public void start() {

		System.out.println("+ ======================================================= +");
		System.out.println("\t\tClient Starting Transfer Data");
		System.out.println("+ ======================================================= +");
		
//		Scanner scanner = new Scanner(System.in);
//		System.out.print("Please provide ip address: ");
//		String ipAddress = scanner.next();
//		System.out.print("Please provide port number: ");
//		int port = scanner.nextInt();
//		System.out.print("Enter packet size: ");
//		int packetSize = scanner.nextInt();
//		System.out.print("Enter window size: ");
//		int windowSize = scanner.nextInt();
//		System.out.print("Enter timeout interval: ");
//		int timeout = scanner.nextInt();
//		System.out.println("");
//		byte[] bytes = loader.loadData();
//		scanner.close();

		byte[] bytes = loader.loadData();
		int packetSize = 100;
		int timeout = 2000;
		String ipAddress = "127.0.0.1";
		int port = 11;
		int windowSize = 5;
		double errors = .5;

		dataSender.sendData(bytes, packetSize, timeout, ipAddress, port, windowSize, errors);

	}

}
