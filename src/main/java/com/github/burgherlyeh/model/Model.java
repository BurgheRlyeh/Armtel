package com.github.burgherlyeh.model;

import com.github.burgherlyeh.model.IPN_Config;

public class Model implements IModel {
    HWLoader hwLoader;

    public Model() {
        hwLoader = new HWLoader(new IPN_Config(){});
    }

    @Override
    public void switchOfflineMode() {
        hwLoader.switchOfflineMode();
    }
    @Override
    public void switchDeviceOfflineMode(String uid) {
        System.out.println("Offline add: " + uid);
        hwLoader.switchDeviceOfflineMode(uid);
    }

    @Override
    public Object[][] getTableData() {
        return hwLoader
                .getStatuses()
                .values()
                .stream()
                .map(sp -> new Object[] {
                        sp.getUid(),
                        sp.getMac(),
                        sp.getIp(),
                        sp.getType(),
                        sp.getSip(),
                        sp.getPkrPort(),
                        sp.getAge()
                })
                .toArray(Object[][]::new);
    }
}
