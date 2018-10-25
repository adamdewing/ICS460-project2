package com.metrostate.ics460.project2.receiver;

public interface DataReceiver {

	byte[] receiveData(String ipAddress, int port, int windowSize, int errorRate);
}
