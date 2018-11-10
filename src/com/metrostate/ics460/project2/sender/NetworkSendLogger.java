package com.metrostate.ics460.project2.sender;

import client_server.Packet;

public class NetworkSendLogger {

    public void logSendPacket(boolean hasAlreadyBeenSent, Packet packet, int packetSize, DatagramCondition condition){
        String status = hasAlreadyBeenSent ? "ReSend " : "SENDing ";
        Long startOffset = getStartOffset(packet, packetSize);
        Long endOffset = getEndOffset(startOffset, packetSize);
        System.out.println(status + " " + packet.getSeqno() + " " + startOffset + ":" + endOffset + " " + System.currentTimeMillis() + " " + condition);
    }

    public void logReceiveAck(Packet packet, AckStatus ackStatus){
        System.out.println("AckRcvd " + " " + packet.getAckno() + " " + ackStatus);

    }

    public enum DatagramCondition{
        SENT,
        DROP,
        ERROR
    }

    public enum AckStatus{
        DuplAck,
        ErrAck,
        MoveWnd,
        NoMoveWnd
    }

    private long getStartOffset(Packet packet, int packetSize){
        return (long)((packet.getSeqno() - 1) * packetSize);
    }

    private long getEndOffset(long startOffset, int packetSize){
        return startOffset + packetSize - 1;
    }
}
