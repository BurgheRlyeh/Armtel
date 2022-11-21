package com.github.burgherlyeh.model;

import jakarta.xml.bind.DatatypeConverter;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class StatusPackage {
    static int UDP_SIZE = 2000;

    long time_t = 0;

    public long getTime_t() {
        return time_t;
    }

    public void setTime_t(long time_t) {
        this.time_t = time_t;
    }

    String marker;

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    InetAddress ip;

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    int pkrPort;

    public int getPkrPort() {
        return pkrPort;
    }

    public void setPkrPort(int pkrPort) {
        this.pkrPort = pkrPort;
    }

    int sip_len = 0;

    public int getSip_len() {
        return sip_len;
    }

    public void setSip_len(int sip_len) {
        this.sip_len = sip_len;
    }

    int shift = 0;

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    String mac = "";

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    private String hwid = "";

    public String getHwid() {
        return hwid;
    }

    public void setHwid(String hwid) {
        this.hwid = hwid;
    }

    private String hwtp = "";

    public String getHwtp() {
        return hwtp;
    }

    public void setHwtp(String hwtp) {
        this.hwtp = hwtp;
    }

    int status = 0;

    public int getStatus() {
        return status;
    }

    public void getStatus(int status) {
        this.status = status;
    }

    String sip = "";

    public String getSip() {
        return sip;
    }

    public void setSip(String sip) {
        this.sip = sip;
    }

    String aux = "";

    public String getAux() {
        return aux;
    }

    public void setAux(String aux) {
        this.aux = aux;
    }


    public StatusPackage(DatagramPacket packet) {
        time_t = System.currentTimeMillis();
        marker = new String(packet.getData(), 0, 5);
        ip = packet.getAddress();
        pkrPort = packet.getPort();

        byte[] state = packet.getData();
        sip_len = state[5];
        if ((int) state[6 + sip_len] == 12) //for compatibility with old status
        {
            shift = 15 + sip_len;
            mac = new String(state, 6 + sip_len + 1, 12); //get sent MAC
            hwid = new String(state, 6 + sip_len + 14, 4); //get sent HWID = HW type + version byte
            hwtp = new String(state, 6 + sip_len + 14, 3); //get HW type
        }
        status = state[10 + shift];

        if (sip_len != 0) {
            sip = new String(state, 6, sip_len);
        }
        int aux_len = state[11 + shift];

        if (aux_len != 0) {
            aux = new String(state, 12 + shift, aux_len);
        }
    }

    public static StatusPackage readFromMCSocket(MulticastSocket socket) {
        StatusPackage sPackage = null;
        try {
            if (socket == null) {
                return null;
            }
            if (socket.isClosed()) {
                return null;
            }
            DatagramPacket packet = new DatagramPacket(new byte[UDP_SIZE], UDP_SIZE);
            socket.receive(packet);
            if (packet.getLength() == 0) {
                return null;
            }
            sPackage = new StatusPackage(packet);
        } catch (Exception ignored) {
        }
        return sPackage;
    }

    public String getType() {
        return getHwtp().isEmpty() ? null : getHwtp();
    }

    public int getTypeInt() {
        return getHwtp().isEmpty() ? -1 : Integer.parseInt(getHwtp());
    }

    public long getAge() {
        return System.currentTimeMillis() - getTime_t();
    }

    public String getUid() {
        return getUid(this.getMac(), String.valueOf(this.getPkrPort()), this.getSip());
    }

    public static String getUid(String... params) {
        String myChecksum;

        try {
            MessageDigest dig = MessageDigest.getInstance("MD5");

            String tmp = "";
            for (String par : params) {
                tmp += par;
            }

            byte[] str = tmp.getBytes();
            dig.update(str);

            byte[] result = dig.digest();
            myChecksum = DatatypeConverter.printHexBinary(result).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return myChecksum;
    }

    @Override
    public String toString() {
        /*String result = "";
        result = getIp().toString() + ":" + getPkrPort() + " " + getSip() + " " + getMac() + " uid: " + getUid(getMac(),getSip(),getIp().toString());*/
        return "";
    }
}
