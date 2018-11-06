package com.metrostate.ics460.project2;

import com.metrostate.ics460.project2.packet.Packet;

public class NetworkLogger {
    
    
    public void logSendPacket(boolean hasAlreadyBeenSent, Packet packet, int packetSize, DatagramCondition condition){
        String status = hasAlreadyBeenSent ? "ReSend " : "SENDing ";
        Long startOffset = getStartOffset(packet, packetSize);
        Long endOffset = getEndOffset(startOffset, packetSize);
        System.out.println(status + " " + packet.getSeqno() + " " + startOffset + ":" + endOffset + " " + System.currentTimeMillis() + " " + condition);
    }
    
    public void logReceivePacket(boolean isPacketAlreadyReceived, Packet packet, int packetSize){
        String status = isPacketAlreadyReceived ? "DUPL " : "RECV ";
        Long startOffset = getStartOffset(packet, packetSize);
        Long endOffset = getEndOffset(startOffset, packetSize);
        System.out.println(status + " " + packet.getSeqno() + " " + startOffset + ":" + endOffset + " " + System.currentTimeMillis());
    }

    public enum DatagramCondition{
        SENT,
        DROP,
        ERROR;
    }

    private long getStartOffset(Packet packet, int packetSize){
        return (long)((packet.getSeqno() - 1) * packetSize);
    }

    private long getEndOffset(long startOffset, int packetSize){
        return startOffset + packetSize - 1;
    }
}
