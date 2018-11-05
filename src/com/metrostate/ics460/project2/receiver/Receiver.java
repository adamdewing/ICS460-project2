package com.metrostate.ics460.project2.receiver;

public class Receiver {

    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        receiver.start();
    }

    public void start() {
        DataReceiver dataReceiver = new UDPDataReceiver();
        byte[] bytes = dataReceiver.receiveData("127.0.0.1", 11, 2, 0);

        Saver saver = new FileSaver();
        saver.saveData(bytes);
    }

}
