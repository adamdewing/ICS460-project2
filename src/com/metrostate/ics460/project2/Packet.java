package com.metrostate.ics460.project2;

public class Packet {
	
	private short cksum;
	private short len;
	private int ackno;
	private int seq_num;
	private byte data[];
	
	public Packet(short cksum, short len, int ackno, int seq_num, byte[] data) {
		this.cksum = cksum;
		this.len = len;
		this.ackno = ackno;
		this.seq_num = seq_num;
		this.data = data;
	}

	public short getCksum() {
		return cksum;
	}

	public void setCksum(short cksum) {
		this.cksum = cksum;
	}

	public short getLen() {
		return len;
	}

	public void setLen(short len) {
		this.len = len;
	}

	public int getAckno() {
		return ackno;
	}

	public void setAckno(int ackno) {
		this.ackno = ackno;
	}

	public int getSeqno() {
		return seq_num;
	}

	public void setSeqno(int seqno) {
		this.seq_num = seqno;
	}

	public byte[] getData() {
		byte bytes = (Byte) null;
		for(int i = 0; i < data.length; i++) {
			bytes += data[i];
		}
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	
	
	
}
