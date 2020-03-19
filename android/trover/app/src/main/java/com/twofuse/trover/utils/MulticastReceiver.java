package com.twofuse.trover.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Created by dmoffett on 1/6/18.
 */

public class MulticastReceiver extends Thread {
    protected MulticastSocket socket = null;
    protected byte[] buf = new byte[256];
    private NetworkInterface multicastInterface = null; // TFUtils.getMulticastInterface();

    public void run() {
        try {
            InetAddress group = null;
            InetSocketAddress socketAddress = null;
            try {
                socket = new MulticastSocket(4446);
                if(multicastInterface != null) {
                    socketAddress = new InetSocketAddress("230.0.0.0", 4446);
                    socket.joinGroup(socketAddress, multicastInterface);
                } else {
                    group = InetAddress.getByName("230.0.0.0");
                    socket.joinGroup(group);
                }
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    TFLog.d(received);
                    if ("end".equals(received)) {
                        break;
                    }
                }
            } finally {
                if(group != null && socket != null) {
                    if(socketAddress != null)
                        socket.leaveGroup(socketAddress, multicastInterface);
                    if(group != null)
                        socket.leaveGroup(group);
                    socket.close();
                }
            }
        }  catch (IOException iox) {
            TFLog.e("Error trying to open multicast socket.");
            if(iox.getMessage() != null)
                TFLog.e(iox.getMessage());
        }
    }
}