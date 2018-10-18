package com.metrostate.ics460.project2.sender;

import java.io.IOException;

public class Sender {

	private Loader loader = new FileLoader();
    private DataSender dataSender = new UDPDataSender();

    public static void main(String[] args) throws IOException {
    	Sender sender = new Sender();
    	sender.start();
    }
    
    public void start() {
    	byte[] bytes = loader.loadData();
    	dataSender.sendData(bytes, 0, 0, null, null, 0, 0);
    }

}
