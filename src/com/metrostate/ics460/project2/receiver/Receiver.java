package com.metrostate.ics460.project2.receiver;

import java.util.Scanner;

public class Receiver {

    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        receiver.start();
    }

    public void start() {
        System.out.println("+ =========================================================== +");
        System.out.println("\t\tServer Started To Receive Data");
        System.out.println("+ =========================================================== +");
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please provide ip address: ");
		String ipAddress = scanner.next();
		System.out.print("Please provide port number: ");
		int port = scanner.nextInt();
		System.out.print("Enter window size: ");
		int windowSize = scanner.nextInt();
		System.out.println("");
        DataReceiver dataReceiver = new UDPDataReceiver();
        byte[] bytes = dataReceiver.receiveData(ipAddress, port, windowSize, 0);

        Saver saver = new FileSaver();
        saver.saveData(bytes);
    }

}
