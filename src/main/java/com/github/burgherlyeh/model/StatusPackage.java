package com.github.burgherlyeh.model;

import jakarta.xml.bind.DatatypeConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

// Status Packet Class
public final class StatusPackage {
    static int UDP_SIZE = 2000;

    long time_t;
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

    int sip_len;
    public int getSip_len() {
        return sip_len;
    }
    public void setSip_len(int sip_len) {
        this.sip_len = sip_len;
    }

    private String sip;
    public String getSip() {
        return sip;
    }
    public void setSip(String sip) {
        this.sip = sip;
    }

    String aux;
    public String getAux() {
        return aux;
    }
    public void setAux(String aux) {
        this.aux = aux;
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

    private int status;
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Creates StatusPackage from DatagramPacket
     * @param packet DatagramPacket
     */
    public StatusPackage(DatagramPacket packet) {
        byte[] state = packet.getData();

        time_t = System.currentTimeMillis();
        marker = new String(packet.getData(), 0, 5);
        ip = packet.getAddress();
        pkrPort = packet.getPort();

        sip_len = state[5];
        sip = sip_len == 0 ? "" : new String(state, 6, sip_len);

        int aux_len = state[11 + shift];
        aux = aux_len == 0 ? "" : new String(state, 12 + shift, aux_len);

        //for compatibility with old status
        if ((int) state[6 + sip_len] == 12) {
            shift = 15 + sip_len;
            mac = new String(state, 6 + sip_len + 1, 12); //get sent MAC
            hwid = new String(state, 6 + sip_len + 14, 4); //get sent HWID = HW type + version byte
            hwtp = new String(state, 6 + sip_len + 14, 3); //get HW type
        }
        status = state[10 + shift];
    }


    /**
     * @param socket Multicast socket to receive the packet
     * @return Status package if packet received, null otherwise
     * @throws SocketException if socket is closed
     */
    public static StatusPackage readFromMCSocket(@NotNull MulticastSocket socket) throws SocketException {
        // exception if socket closed
        if (socket.isClosed()) {
            throw new SocketException("Socket is closed");
        }
        System.out.println("Socket isn't closed");

        try {
            DatagramPacket packet = new DatagramPacket(new byte[UDP_SIZE], UDP_SIZE);
            System.out.println("DatagramPacket before receive: " + packet);
            socket.receive(packet);
            System.out.println("DatagramPacket after receive: " + packet);
            if (packet.getLength() != 0) {
                return new StatusPackage(packet);
            }
            System.out.println("DatagramPacket is 0 length");
        } catch (IOException ignored) {
            System.out.println("IOException");
        } catch (Exception e) {
            System.out.println("Exception");
        }

        return null;
    }

    public static int channelAmount(String type) {
        if (type == null)
            return 1;
        var ipnDeviceType = IpnDeviceType.fromInt(Integer.parseInt(type));
        if (ipnDeviceType == IpnDeviceType.IPN_8U || ipnDeviceType == IpnDeviceType.IPN_LEX) {
            return 8;
        }
        return 1;

        // orig
//        return switch (type) {
//            case IpnDataTypes.IPN_8U_CODE, IpnDataTypes.IPN_LEX_CODE -> 8;
//            default -> 1;
//        };
    }

    public int channelAmount() {
        return channelAmount(getType());
    }

    public static boolean isMultiChannel(String type) {
        // suppose type = type number
//        return type != null
//                && (
//                        type.contains(IpnDeviceType.IPN_8U.toStringNumber())
//                                || type.contains(IpnDeviceType.IPN_LEX.toStringNumber())
//                );

        // suppose type = type string
        return type != null && (type.contains(IpnDeviceType.IPN_8U.toString()) || type.contains(IpnDeviceType.IPN_LEX.toString()));

        // orig
//        return type != null && (type.contains(IpnDataTypes.IPN_8U_CODE) || type.contains(IpnDataTypes.IPN_LEX_CODE));
    }

    public boolean isMultiChannel() {
        return isMultiChannel(getType());
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

            StringBuilder tmp = new StringBuilder();
            for (String par : params) {
                tmp.append(par);
            }

            dig.update(tmp.toString().getBytes());
            myChecksum = DatatypeConverter.printHexBinary(dig.digest()).toUpperCase();
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