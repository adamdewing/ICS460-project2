package client_server;

import java.io.Serializable;

/*This is a Packet class to create acknowledgement and data packet objects for the sender
and receiver to use. */
public class Packet implements Serializable {

	private static final long serialVersionUID = 8065498869540255101L;

	private short cksum;
	private short len;
	private int ackno;
	private int seqno;
	private byte[] data;

	// Constructor to create ACK packets.
	Packet(short cksum, int ackno) {
		this.cksum = cksum;
		this.len = 8;
		this.ackno = ackno;
	}

	// Constructor to create data packets.
	Packet(short cksum, short len, int seqno, byte[] data) {
		this.cksum = cksum;
		this.len = len;
		this.seqno = seqno;
		this.data = data;
	}

	public Packet() {
		// TODO Auto-generated constructor stub
	}

	public short getCksum() {
		return cksum;
	}

	public short getLen() {
		return len;
	}

	public int getAckno() {
		return ackno;
	}

	public int getSeqno() {
		return seqno;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * @param cksum the cksum to set
	 */
	public void setCksum(short cksum) {
		this.cksum = cksum;
	}

	/**
	 * @param len the len to set
	 */
	public void setLen(short len) {
		this.len = len;
	}

	/**
	 * @param ackno the ackno to set
	 */
	public void setAckno(int ackno) {
		this.ackno = ackno;
	}

	/**
	 * @param seqno the seqno to set
	 */
	public void setSeqno(int seqno) {
		this.seqno = seqno;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Packet [cksum=" + cksum + ", len=" + len + ", ackno=" + ackno + ", seqno=" + seqno + "]";
	}


}