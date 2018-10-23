package com.metrostate.ics460.project2;

import java.util.Random;

public class Packet {

	private short cksum;
	private short len;
	private int ackno;
	private int seq_num;
	private short remainder;
	private int windowSize;
	byte[] checksum;
	private byte[] data = new byte[1024];


	public Packet(short cksum, short len, int ackno, int seq_num, 
			short remainder, int windowSize) {
		this.cksum = cksum;
		this.len = len;
		this.ackno = ackno;
		this.seq_num = seq_num;
		this.remainder = remainder;
		this.windowSize = windowSize;
	}
	
	public Packet(int seq_num, byte[] bytes, byte[] checksum, int windowSize) {
		this.checksum = checksum;
		this.seq_num = seq_num;
		this.windowSize = windowSize;
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

	public int getSeq_num() {
		return seq_num;
	}

	public void setSeq_num(int seq_num) {
		this.seq_num = seq_num;
	}

	public short getRemainder() {
		return remainder;
	}

	public void setRemainder(short remainder) {
		this.remainder = remainder;
	}

	public int getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public byte[] getData() {
		byte bytes = 0;
		for (int i = 0; i < data.length; i++) {
			bytes += data[i];
		}
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * Randomly corrupt the packet
	 */
	private void makePackets() {

		Random random = new Random();
		int num = random.nextInt(10);

		// Half of the time corrupt the frame
		if(num > 5){
			
			if(num == 7){
				// corrupt length
				len = (short)random.nextInt(65535);
			} 
			
			else if(num == 6){
				// corrupt sequence number
				len  = (short)random.nextInt(65535);
			} 

			else if(num == 8){
				// corrupt first byte
				data[0] = '*';

				// randomly corrupt rest of bytes
				for(int i = 1; i < data.length; i++){
					if(random.nextInt(2) == 1){
						data[i] = (byte)random.nextInt(256);
					}
				}
			}
			
			else if(num == 9){
				//Corrupting Remainder on frame
				remainder = (short)random.nextInt(65535);
			}
		}
	}

}
