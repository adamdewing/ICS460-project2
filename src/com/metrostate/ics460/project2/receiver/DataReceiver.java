package com.metrostate.ics460.project2.receiver;

public interface DataReceiver {

	public byte[] receiveData(String ipAddress, String port, int windowSize, int errorRate);
}
