package com.metrostate.ics460.project2;

public class Packet {
	private short cksum;
	private short len;
	private int ackno;
	private int seqno;
	private byte data[];
	
	public Packet () {
		
	}
}
