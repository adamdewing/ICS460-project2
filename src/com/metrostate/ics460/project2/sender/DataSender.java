package com.metrostate.ics460.project2.sender;

public interface DataSender {

	public void sendData(byte[] bytes, int packetSize, long timeout, String ipAddress, String port, int windowSize, int errorRate);
	
}
