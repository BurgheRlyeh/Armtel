package com.github.burgherlyeh.model;

import com.github.burgherlyeh.model.IPN_Config;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class HWLoader implements Runnable {
    private final IPN_Config ipnConfig;
    final static String IP = "224.0.1.11";  // administration multicast IP
    final static int mc_Port = 21332;       // administration multicast Port
    MulticastSocket mc_Socket = null;       // administration socket IP:mc_Port
    private final Map<String, StatusPackage> statusMap = new LinkedHashMap<>();

    public synchronized Map<String, StatusPackage> getStatuses() {
        return statusMap;
    }

    private synchronized void setStatus(StatusPackage status) {
        statusMap.put(status.getUid(), status);
    }

    // offline flag
    private boolean isOffline = false;

    public boolean isOfflineMode() {
        return isOffline;
    }

    // Stop or continue receiving status packets
    public void switchOfflineMode() {
        if (isOffline) {
            offlineDevices.clear();
        }
        isOffline = !isOffline;
    }

    private final List<String> offlineDevices = new ArrayList<>();

    public void switchDeviceOfflineMode(String uid) {
        if (!offlineDevices.contains(uid)) {
            offlineDevices.add(uid);
        } else {
            offlineDevices.remove(uid);
        }
    }

    public HWLoader(IPN_Config ipnConfig) {
        this.ipnConfig = ipnConfig;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                if (isOfflineMode()) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(900, 1000));
                    continue;
                }
                while (get_HW_msg()) ;
                load_mc_Socket(IP, mc_Port, -1); // join group for proper port
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        clearSocket();
    }

    // get the UDP packet queue and check state changes
    public boolean get_HW_msg() {
        boolean queue = true;

        // Slow down reading messages while working with configurations to avoid races and freezes
        if (ipnConfig.isRedrawBlocked()) {
            wait_run(ThreadLocalRandom.current().nextInt(150, 200));
        }

        // check HW status messages
        try {
            if (mc_Socket == null) {
                return (queue = false);
            }
            if (mc_Socket.isClosed()) {
                mc_Socket = null;
                return (queue = false);
            }
            StatusPackage statusPackage = StatusPackage.readFromMCSocket(mc_Socket);
            if (statusPackage == null) {
                return (queue = false);
            }
            if (!offlineDevices.contains(statusPackage.getUid())) {
                setStatus(statusPackage);
            }


        } catch (Exception e) {
            return (queue = false);
        } finally {
            return queue;
        }
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

    //create multicast socket for allocate and status messages
    private void load_mc_Socket(String IP, int Port, int ipIndex) {
        try {
            if (mc_Socket != null) {
                mc_Socket.close();
            }
            mc_Socket = new MulticastSocket(Port);  // open port for receiving multicast UDP packets
            mc_Socket.setSoTimeout(3000);           // set max waiting for new packets
            InetAddress mc_group;

            if (System.getProperty("os.name").equals("Linux")) {
                mc_group = InetAddress.getByName(IP);   // create multicast group by this address
                if (mc_group.isMulticastAddress()) {
                    mc_Socket.joinGroup(mc_group);      // join to this group
                }
            } else {
                String net_int = "";
                int i_0 = 0;
                InetAddress[] addresses = (InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()));
                int i_n = addresses.length;
                if (ipIndex >= 0) {
                    i_0 = ipIndex;
                    i_n = i_0 + 1;
                }
                for (int i = i_0; i < i_n; i++) {
                    mc_Socket.setInterface(addresses[i]);
                    if (net_int.equals(mc_Socket.getNetworkInterface().toString())) continue;
                    net_int = mc_Socket.getNetworkInterface().toString();
                    mc_group = InetAddress.getByName(IP);
                    try {
                        if (mc_group.isMulticastAddress()) {
                            mc_Socket.joinGroup(mc_group);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void clearSocket() {
        mc_Socket.close();
        mc_Socket = null;
    }
}