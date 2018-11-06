package com.metrostate.ics460.project2.sender;

import com.metrostate.ics460.project2.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UDPDataSender implements DataSender {

	private static final NetworkSendLogger log = new NetworkSendLogger();

	private static final int port = 13;
	// Sequence number of the last acknowledged packet
    int nextFrameExpected = 1;
    double errorRate = 0;

	@Override
	public void sendData(byte[] bytes, int packet_size, int timeout, String receiverIpAddress, int receiverPort, int windowSize, double errors) {
        System.out.println("+ =========================================================== +");
        System.out.println("\t\tClient Started To Send Data");
        System.out.println("+ =========================================================== +");
        this.errorRate = errors / 100;

		// Add byte[] into a list by split byte to smaller chunks byte
		List<byte[]> byteList = byteArrayToChunks(bytes, packet_size);

		// Create a datagram socket
		try (DatagramSocket socket = new DatagramSocket(port)) {
			socket.setSoTimeout(timeout);

			// Receiver address
			InetAddress receiverInetAddress = InetAddress.getByName(receiverIpAddress);

			// Set to slide window size

			// Convert byteList to a List of Packet Object and add into a list
			List<Packet> sent_packet_list = byteListToPacketList(byteList);
			sent_packet_list.forEach(packet -> System.out.println(packet));

			boolean[] isSent = new boolean[sent_packet_list.size() + 1];
			boolean[] hasBeenSentAlready =  new boolean[sent_packet_list.size() + 1];
			while(true){
				// Iterate over sliding window to see which packets need to be sent
				for(int i = nextFrameExpected; i < nextFrameExpected + windowSize; i++){
					if(i < isSent.length && !isSent[i]){
						Packet packet = sent_packet_list.get(i - 1);
						packet.setAckno(nextFrameExpected);
						byte[] data = UDPDataSender.SerializeObject.serializePacketObject(packet);
						DatagramPacket datagramPacket = new DatagramPacket(data, data.length, receiverInetAddress, receiverPort);
						socket.send(datagramPacket);
						log.logSendPacket(hasBeenSentAlready[i], packet, packet_size, NetworkSendLogger.DatagramCondition.SENT);
						isSent[i] = true;
						hasBeenSentAlready[i] = true;

					}else{
						// We already sent this packet so do nothing
					}
				}
				DatagramPacket receiveDatagramPacket = new DatagramPacket(new byte[1024], 1024);
				try {
					socket.receive(receiveDatagramPacket);
					Packet receivePacket = UDPDataSender.SerializeObject.deserializePacketObject(receiveDatagramPacket.getData());
					log.logReceiveAck(receivePacket, getAckStatus(receivePacket));
					nextFrameExpected = receivePacket.getAckno();
					if(nextFrameExpected > sent_packet_list.size()){
						return;
					}
				} catch (SocketTimeoutException e) {
					// Need to resend the first packet we are waiting on
					isSent[nextFrameExpected] = false;
				} catch (ClassNotFoundException e) {
					// Could not deserialize.  Could mean a network error.  Just eat error and keep waiting for another packet.
					System.out.println("Warning, could not deserialize packet!");
				}
			}
		}catch (IOException e) {
			System.err.println();
			e.printStackTrace();
		}

	}

	/**
	 * Split byte[] to smaller chunks
	 */
	private List<byte[]> byteArrayToChunks(byte[] bytes, int packet_size) {

		List<byte[]> byteList = new ArrayList<byte[]>();

		byte[] chunk_bytes;
		for (int i = 0; i < bytes.length; i += packet_size) {
			if(i + packet_size > bytes.length){
				chunk_bytes = Arrays.copyOfRange(bytes,  i,  bytes.length);
			}else{
				chunk_bytes = Arrays.copyOfRange(bytes,  i,  i + packet_size);
			}
			byteList.add(chunk_bytes);
		}

		return byteList;
	}

	// Convert byteList to a List<Packet Object>
	private static List<Packet> byteListToPacketList(List<byte[]> byteList) {
		List<Packet> packet_list = new ArrayList<Packet>();
		for (int i = 0; i < byteList.size(); i++) {
			Packet packet = new Packet();
			byte[] bytes = byteList.get(i);
			packet.setCksum((short) 0);
			packet.setLen((short) (bytes.length + 12));
			packet.setSeqno(i + 1);
			packet.setData(bytes);
			packet_list.add(packet);
		}

		// Add final packet with no data to let receiver know we are done.
		Packet packet = new Packet();
		packet.setCksum((short)0);
		packet.setLen((short)12);
		packet.setSeqno(packet_list.size() + 1);
		packet.setData(new byte[0]);
		packet_list.add(packet);

		return packet_list;
	}

	private NetworkSendLogger.AckStatus getAckStatus(Packet receivePacket){
	    //Check if error
	    if(receivePacket.getCksum() == 1){
	        return NetworkSendLogger.AckStatus.ErrAck;
        }

        // Check if move window
        if(receivePacket.getAckno() >= nextFrameExpected){
            return NetworkSendLogger.AckStatus.MoveWnd;
        }

        // Check if duplicate
        if(receivePacket.getAckno() < nextFrameExpected){
            return NetworkSendLogger.AckStatus.DuplAck;
        }
//        if(receivePacket.getAckno() + 1 < isAckReceived.length && isAckReceived[receivePacket.getAckno()]){
//            return NetworkSendLogger.AckStatus.DuplAck;
//        }

        // If we get here then just an Ack, but are not moving the window forward
        System.err.println("Should never get here actually.  Error 1001.");
        return NetworkSendLogger.AckStatus.NoMoveWnd;
    }


	// Private class for Serializing Object
	private static class SerializeObject {

		// Convert an object to byte array
		private static byte[] serializePacketObject(Packet packet) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = null;
			try {
				oos = new ObjectOutputStream(bos);
			} catch (IOException e1) {

				e1.printStackTrace();
			}
			try {
				oos.writeObject(packet);
			} catch (IOException e) {

				e.printStackTrace();
			}

			try {
				oos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte[] bytes = bos.toByteArray();

			try {
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bytes;
		}

		private static Packet deserializePacketObject(byte[] ackPacket) throws ClassNotFoundException, IOException {
			Packet packet;
			ByteArrayInputStream bais;
			ObjectInputStream ois = null;
			try {
				bais = new ByteArrayInputStream(ackPacket);
				ois = new ObjectInputStream(bais);

				packet = (Packet) ois.readObject();
			} finally {
				if(ois != null){
					ois.close();
				}
			}
			return packet;
		}
	}

    private Packet corruptDelayDropPacket(Packet packet){
        System.out.println(Math.random());
        if(Math.random() < errorRate){
            // We are going to mess up the packet
            double error = Math.random();
            if(error < .33){
                // Drop
                return null;
            }else if(error < .66){
                // Corrupt
                packet.setCksum((short) 1);
            }else{
                // Delay
                int min = 1;
                int max = 3;
                try {
                    TimeUnit.SECONDS.sleep((int)(Math.random()*((max-min)+1))+min);
                } catch (InterruptedException e) {
                    System.out.println("System was interrupted while delaying packet with sequence number :" + packet.getSeqno());
                    e.printStackTrace();
                }
            }
        }
        return packet;
    }
}
