package com.github.burgherlyeh.model;

public interface IModel {
    /**
     * Stop or continue receiving status packets
     */
    void switchOfflineMode();

    /**
     * Stop or continue receiving packets from device
     * @param uid Unique ID of device to stop or continue receiving packets from
     */
    void switchDeviceOfflineMode(String uid);

    /**
     * @return 2d array of objects to present for user
     */
    Object[][] getTableData();
}
