package com.metrostate.ics460.project2.receiver;

import client_server.Packet;

public class NetworkReceiverLogger {
    public void logReceivePacket(boolean isPacketAlreadyReceived, Packet packet, boolean isPacketOutOfSequence) {
        String status = isPacketAlreadyReceived ? "DUPL " : "RECV ";
        String packetStatus = "";
        if (packet.getCksum() == 1) {
            packetStatus = "CRPT";
        } else if (isPacketOutOfSequence) {
            packetStatus = "!Seq";
        } else {
            packetStatus = "RECV";
        }
        System.out.println(status + " " + System.currentTimeMillis() + " " + packet.getSeqno() + " " + packetStatus);
    }

    public void logAckPacket(Packet packet, DatagramCondition condition) {
        System.out.println("SENDing ACK " + packet.getAckno() + " " + System.currentTimeMillis() + " " + condition);
    }

    public enum DatagramCondition {
        SENT,
        DROP,
        ERROR;
    }

}
