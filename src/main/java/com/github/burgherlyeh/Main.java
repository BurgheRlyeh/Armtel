package com.github.burgherlyeh;

import com.github.burgherlyeh.model.Model;
import com.github.burgherlyeh.view.TableView;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        var model = new Model();
        var viewController = new TableView(model);

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            viewController.updateTableView();
        }
    }
}

class My {

    List<Integer> list;

    public My() {
        list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
    }

    public List<Integer> getList() {
        return list;
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
}