package com.metrostate.ics460.project2.receiver;

public interface DataReceiver {

	byte[] receiveData(String receiverIpAddress, int receiverPort, int windowSize, int errorRate);
}
