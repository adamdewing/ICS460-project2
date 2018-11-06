package com.metrostate.ics460.project2.receiver;

import com.metrostate.ics460.project2.packet.Packet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class UDPDataReceiver implements DataReceiver {

    private final static NetworkReceiverLogger logger = new NetworkReceiverLogger();
    private final static int MAX_BUFFER_SIZE = 1024;
    private final static int ERROR_FACTOR = 3;

    private Map<Integer, byte[]> byteMap = new HashMap<>();
    private int errorRate = 0;
    private int lastPacketSequenceNumber = 0;

    @Override
    public byte[] receiveData(String receiverIpAddress, int receiverPort, int windowSize, int errorRate) {
        System.out.println("+ =========================================================== +");
        System.out.println("\t\tServer Started To Recieved Data");
        System.out.println("+ =========================================================== +");
        this.errorRate = errorRate;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(receiverPort);
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[MAX_BUFFER_SIZE], MAX_BUFFER_SIZE);
                socket.receive(datagramPacket);
                Packet packet = deserializePacket(datagramPacket);
                int packetSize = 0;
                if(packet.getData() != null){
                    packetSize = packet.getData().length;
                }
                if (isValidPacket(packet)) {
                    if (!isPacketAlreadyReceived(packet)) {
                        logger.logReceivePacket(false, packet, isPacketReceivedOutOfSequence(packet));
                        addPacketToList(packet);
                    } else {
                        logger.logReceivePacket(true, packet, isPacketReceivedOutOfSequence(packet));
                        // We already received this packet so just throw it away
                    }
                    if (isLastPacket(packet)) {
                        lastPacketSequenceNumber = packet.getSeqno();
                    }
                    sendAckPacket(socket, datagramPacket);

                } else {
                    // Receiver only sends positive Acks, so don't send Acks for corrupted Packets
                }
                if(isAllPacketsReceived()){
                    // We are all done!
                    break;
                }
            }
        } catch (SocketException e) {
            System.err.println("Error opening up DatagramSocket!");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            System.err.println("Error receiving DatagramPacket from DatagramSocket!");
            e.printStackTrace();
            return null;
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        return mergeByteList();
    }

    private boolean isPacketAlreadyReceived(Packet packet) {
        if (byteMap.get(packet.getSeqno()) == null) {
            return false;
        }
        ;
        return true;
    }

    /**
     * Sends an Ack DatagramPacket letting the sender know what Packet we need next.
     *
     * @param socket
     */
    private void sendAckPacket(DatagramSocket socket, DatagramPacket receivePacket) {
        Packet ackPacket = new Packet();
        ackPacket.setCksum((short) 0);
        ackPacket.setLen((short) 8);
        ackPacket.setAckno(getNextSeqnoNeeded());
        byte[] bytes = serializePacket(ackPacket);
        DatagramPacket datagramPacket = null;
        datagramPacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(), receivePacket.getPort());
        logger.logAckPacket(ackPacket, NetworkReceiverLogger.DatagramCondition.SENT);
        try {
            socket.send(datagramPacket);
        } catch (IOException e) {
            // If we can't send the Ack, then just log the error and continue processing
            e.printStackTrace();
        }
    }

    /**
     * Deserializes the payload of a DatagramPacket into a Packet object.
     *
     * @param datagramPacket
     * @return
     */
    private Packet deserializePacket(DatagramPacket datagramPacket) {
        ObjectInputStream ois = null;
        Packet packet;
        try {
            ois = new ObjectInputStream(new ByteArrayInputStream(datagramPacket.getData()));
            packet = (Packet) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UDPDataReceiverException("Could not create an ObjectInputStream while trying to deserialize the payload from a DatagramPacket!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new UDPDataReceiverException("Could not deserialize the payload for a DatagramPacket!");
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    // Do nothing
                }
            }
        }
        return packet;
    }

    /**
     * Serialize a packet into a DatagramPacket
     *
     * @param packet
     * @return
     */
    private byte[] serializePacket(Packet packet) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput oos = null;
        try {
            oos = new ObjectOutputStream(outputStream);
            oos.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
            throw new UDPDataReceiverException("Error trying to write a Packet to an ObjectOutputStream");
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                // Do nothing, just mention in logs
                System.out.println("Warning: There was a problem with closing an ObjectOutputStream while serializing a Packet object.");
            }
        }
        return outputStream.toByteArray();
    }

    private boolean introduceError() {

        if (Math.random() < errorRate / 100) {
            return true;
        }
        return false;
    }

    /**
     * Adds a Packet's data to the List of Packet data
     *
     * @param packet
     */
    private void addPacketToList(Packet packet) {
        if (packet.getData() == null && packet.getLen() != 0) {
            System.err.println("Got a null data!!");
        }

        if (packet.getLen() - 12 != packet.getData().length) {
            System.out.println("WARNING:  Size of the Packet does not match the sent Packet length.");
        }
        int dataSize = packet.getData().length;
        byte[] bytes = new byte[dataSize];
        System.arraycopy(packet.getData(), 0, bytes, 0, dataSize);
        byteMap.put(packet.getSeqno(), bytes);
    }

    /**
     * Finds the next sequence number that we are waiting on.
     *
     * @return
     */
    private int getNextSeqnoNeeded() {
        int nextSeqnoNeeded = 1;
        while (byteMap.get(nextSeqnoNeeded) != null) {
            nextSeqnoNeeded++;
        }
        return nextSeqnoNeeded;
    }

    /**
     * Determines if the data packet is within the sliding window.
     *
     * @param packet                 the data packet to check.
     * @param firstPacketNotReceived the first data packet we are waiting on.
     * @param windowSize             the size of the sliding window.
     * @return
     */
    private boolean isPacketInWindow(Packet packet, int firstPacketNotReceived, int windowSize) {
        return packet.getSeqno() >= firstPacketNotReceived && packet.getSeqno() < firstPacketNotReceived + windowSize;
    }

    /**
     * Determines if a data Packet is a valid(good) or corrupted(bad).
     *
     * @param packet the data packet check
     * @return
     */
    private boolean isValidPacket(Packet packet) {
        return packet.getCksum() == 0 ? true : false;
    }

    /**
     * Determines if the data Packet is the last data Packet being sent.
     *
     * @param packet the data packet to check
     * @return
     */
    private boolean isLastPacket(Packet packet) {
        return packet.getData() == null || packet.getData().length == 0 ? true : false;
    }


    private boolean isAllPacketsReceived(){
        if(lastPacketSequenceNumber == 0){
            // we haven't got the last packet yet
            return false;
        }
        // we have gotten the last packet, but are we missing any packet's
        for (int i = 1; i <= byteMap.size(); i++) {
            // Check to see if we are missing a packet
            if(byteMap.get(i) == null){
                return false;
            }
        }
        return true;
    }

    /**
     * This method takes the List of byte arrays and combines them into a single byte array.
     *
     * @return
     */
    private byte[] mergeByteList() {
        // Find the total length of all the data combined
        int totalLength = 0;
        for (int i = 1; i <= byteMap.size(); i++) {
            totalLength += byteMap.get(i).length;
        }

        byte[] bytes = new byte[totalLength];
        int currentPosition = 0;
        for (int i = 1; i <= byteMap.size(); i++) {
            System.arraycopy(byteMap.get(i), 0, bytes, currentPosition, byteMap.get(i).length);
            currentPosition += byteMap.get(i).length;
        }
        return bytes;
    }

    private boolean isPacketReceivedOutOfSequence(Packet packet) {
        if (packet.getSeqno() == 1) {
            return false;
        }
        for (int i = 1; i < packet.getSeqno(); i++) {
            if (byteMap.get(i) == null) {
                return true;
            }
        }
        return false;
    }

    public class UDPDataReceiverException extends RuntimeException {

        UDPDataReceiverException(String s) {
            super(s);
        }
    }


}
