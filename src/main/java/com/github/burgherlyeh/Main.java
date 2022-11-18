package com.github.burgherlyeh;

import com.github.burgherlyeh.model.HWLoader;
import com.github.burgherlyeh.model.IPN_Config;

import java.io.IOException;
import java.net.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        Thread t = new Thread(new UDPMulticastClient());
        t.start();
        Thread.sleep(10000);
        UDPMulticastClient.UDPMulticastServer.main();
    }
}

class UDPMulticastClient implements Runnable {
    @Override
    public void run() {
        while (true) {
            System.out.println("\n");
            try {
                byte[] buffer = new byte[2000];
                MulticastSocket socket = new MulticastSocket(21332);
                InetAddress group = InetAddress.getByName("224.0.1.11");
                socket.joinGroup(group);
                while (true) {
                    System.out.println("\nWaiting for multicast message...");
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    System.out.println("[Multicast UDP message received]\n" + msg);
                    if ("OK".equals(msg)) {
                        System.out.println("No more message. Exiting : " + msg);
                        break;
                    }
                }
                socket.leaveGroup(group);
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("\n");
        }
    }

    static class UDPMulticastServer {
        public static void sendUDPMessage(String message, String ipAddress, int port) throws IOException {
            DatagramSocket socket = new DatagramSocket();
            InetAddress group = InetAddress.getByName(ipAddress);
            byte[] msg = message.getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, port);
            socket.send(packet);
            socket.close();
        }

        public static void main() throws IOException {
            sendUDPMessage("This is a multicast message", "224.0.1.11", 21332);
            sendUDPMessage("This is the second multicast message", "224.0.1.11", 21332);
            sendUDPMessage("This is the third multicast message", "224.0.1.11", 21332);
            sendUDPMessage("OK", "224.0.1.11", 21332);
        }
    }
}