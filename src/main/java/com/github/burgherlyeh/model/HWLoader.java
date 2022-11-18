package com.github.burgherlyeh.model;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

// Status receiving class
public class HWLoader implements Runnable {
    private final IPN_Config ipnConfig;
    final static int mc_port = 21332;   //administration multicast Port
    private MulticastSocket mc_socket = null;   //administration socket 224.0.1.11:mc_Port
    private final HashMap<String, StatusPackage> statusMap = new HashMap<>();

    /**
     * Create and start new thread that receives and stores multicast packets
     * @param ipnConfig configuration of IPN
     * @throws IllegalArgumentException if passed IPN_Config points to null
     */
    public HWLoader(@NotNull IPN_Config ipnConfig) {
        this.ipnConfig = Objects.requireNonNull(ipnConfig, "Config is null");
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            // sleep if offline mode is on
            if (isOfflineMode()) {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(900, 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            while (get_HW_msg());
            System.out.println("MC_SOCKET = " + mc_socket);
            load_mc_Socket("224.0.1.11", mc_port, -1); //join group for proper port
        }
    }

    private boolean isOfflineMode() {
        return false;
    }

    //get the UDP packet queue and check state changes
    public boolean get_HW_msg() {
        // Slow down reading messages while working with configurations to avoid races and freezes
        if (ipnConfig.isRedrawBlocked()) {
            wait_run(ThreadLocalRandom.current().nextInt(150, 200));
        }

        //check HW status messages
        try {
            // try to get status package from socket
            StatusPackage statusPackage = StatusPackage.readFromMCSocket(mc_socket);
            if (statusPackage == null) {
                return false;
            }
            setStatus(statusPackage);
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void wait_run(long msec) {
        while (ipnConfig.R_flag) {
            try {
                Thread.sleep(msec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void setStatus(StatusPackage status) {
        System.out.println(
                "uid:\t" + status.getUid() + "\t" +
                        "mac:\t" +status.mac + "\t" +
                        "type:\t" + status.getType() + "\t" +
                        "ip:\t" + status.getIp() + "\t" +
                        "sip:\t" + status.getSip() + "\t" +
                        "port:\t" + status.getPkrPort() + "\t" +
                        "age:\t" + status.getAge()
        );

        statusMap.put(status.getUid(), status);
    }

    // create multicast socket for allocate and status messages
    private void load_mc_Socket(String IP, int Port, int ipIndex) {
        if (mc_socket != null) {
            mc_socket.close();
        }

        try {
            mc_socket = new MulticastSocket(Port);  //open port for receiving multicast UDP packets
            mc_socket.setSoTimeout(3000);           //set max waiting for new packets
            InetAddress mc_group;

            if (System.getProperty("os.name").equals("Linux")) {
                System.out.println("Is Linux");
                mc_group = InetSocketAddress.createUnresolved(IP, Port).getAddress();   //create multicast group by this address
                System.out.println("MC_GROUP = " + mc_group);
                if (mc_group.isMulticastAddress()) {
                    System.out.println("MC_GROUP is mutlicast address");
                    mc_socket.joinGroup(mc_group);      // join to this group
                }
                return;
            }

            String net_int = "";
            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());

            int i_0 = Math.max(ipIndex, 0);
            int i_n = ipIndex < 0 ? addresses.length : i_0 + 1;

            for (int i = i_0; i < i_n; i++) {
                mc_socket.setInterface(addresses[i]);
                if (mc_socket.getNetworkInterface().toString().equals(net_int)) {
                    continue;
                }
                net_int = mc_socket.getNetworkInterface().toString();
                mc_group = InetAddress.getByName(IP);
                try {
                    if (mc_group.isMulticastAddress()) {
                        mc_socket.joinGroup(mc_group);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }

//        try {
//            mc_socket = new MulticastSocket(Port);  //open port for receiving multicast UDP packets
//            mc_socket.setSoTimeout(3000);           //set max waiting for new packets
//            InetAddress mc_group;
//
//            if (System.getProperty("os.name").equals("Linux")) {
//                System.out.println("Is Linux");
//                mc_group = InetAddress.getByName(IP);   //create multicast group by this address
//                System.out.println("MC_GROUP = " + mc_group);
//                if (mc_group.isMulticastAddress()) {
//                    System.out.println("MC_GROUP is mutlicast address");
//                    mc_socket.joinGroup(mc_group);      // join to this group
//                }
//                return;
//            }
//
//            String net_int = "";
//            InetAddress[] addresses = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
//
//            int i_0 = Math.max(ipIndex, 0);
//            int i_n = ipIndex < 0 ? addresses.length : i_0 + 1;
//
//            for (int i = i_0; i < i_n; i++) {
//                mc_socket.setInterface(addresses[i]);
//                if (mc_socket.getNetworkInterface().toString().equals(net_int)) {
//                    continue;
//                }
//                net_int = mc_socket.getNetworkInterface().toString();
//                mc_group = InetAddress.getByName(IP);
//                try {
//                    if (mc_group.isMulticastAddress()) {
//                        mc_socket.joinGroup(mc_group);
//                    }
//                } catch (Exception ignored) {
//                }
//            }
//        } catch (Exception ignored) {
//        }
    }

    public void clearSocket() {
        mc_socket.close();
        mc_socket = null;
    }

    public void pause() {
        wait_run(500);
    }

    public synchronized HashMap<String, StatusPackage> getStatuses() {
        return statusMap;
    }
}