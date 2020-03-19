package com.twofuse.trover.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;

/**
 * Created by dmoffett on 1/6/18.
 */

public class MulticastPublisher {
    private DatagramSocket socket;
    private InetAddress group;
    private SocketAddress socketAddress;
    private byte[] buf;
    private NetworkInterface multicastInterface = null; // TFUtils.getMulticastInterface();


    public void multicast(String multicastMessage) {
        try {
            DatagramPacket packet = null;
            try {
                socket = new DatagramSocket();
                group = InetAddress.getByName("230.0.0.0");
                buf = multicastMessage.getBytes();
                if(multicastInterface != null){
                    socketAddress = new InetSocketAddress("230.0.0.0", 4446);
                    packet = new DatagramPacket(buf, buf.length, socketAddress);
                } else {
                    packet = new DatagramPacket(buf, buf.length, group, 4446);
                }

                socket.send(packet);
            } finally {
                socket.close();
            }
        } catch(Exception x){
            TFLog.e("Error trying to send multicast packet.");
        }
    }
}