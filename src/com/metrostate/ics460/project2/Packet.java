package com.metrostate.ics460.project2;

import java.io.Serializable;

public class Packet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8065498869540255100L;
	
	private short cksum;
	private short len;
	private int ackno;
	private int seqno;
	private byte[] data;
	
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
		return seqno;
	}
	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}

}
