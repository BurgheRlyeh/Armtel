package com.github.burgherlyeh.model;

// Device types
public enum IpnDeviceType {
    DIS_IP2(201),
    DW_IP2(203),
    ACM_IP2(204),
    TOP_DIS_IP2(205),
    TOP_PAD_IP2(206),
    CCS_IP2(207),
    CCS_S_IP2(208),
    DW_IP2_1(209),
    ACM_IP2_1(210),
    IPN_MODULE(999),
    IPN_8U(652),
    DIS_IP1(653),
    ACM_IP1(654),
    DIS_IP3(301),
    DW_IP3(303),
    CCS_IP3(307),
    CCS_S_IP3(308),
    PLY_300(311),
    IPN_1LE(312),
    NCU_IPN3(313),
    IPN_LEX(314),
    IPN_1ABONENT(100),
    IPN_2ABONENT(200),
    IPN_3ABONENT(300),
    unknown(-1);

    private final int dev_type_number;

    IpnDeviceType(int number) {
        dev_type_number = number;
    }

    static public IpnDeviceType fromInt(int number) {
        for (IpnDeviceType type : IpnDeviceType.values())
            if (type.toInt() == number)
                return type;
        return unknown;
    }

    public int toInt() {
        return this.dev_type_number;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String toStringNumber() {
        return String.valueOf(this.dev_type_number);
    }
}
