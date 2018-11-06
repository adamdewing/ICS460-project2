package com.metrostate.ics460.project2.sender;



public interface DataSender {

	void sendData(byte[] bytes, int packetSize, int timeout, String ipAddress, int port, int windowSize, double errors);
	
}
