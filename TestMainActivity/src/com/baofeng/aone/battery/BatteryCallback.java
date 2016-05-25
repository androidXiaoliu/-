package com.baofeng.aone.battery;

import com.baofeng.aone.AndroidCallback;

public interface BatteryCallback extends AndroidCallback {
    /**
     * monitor the battery status
     *
     * @param status
     *            battery status 1 unknown ;
     *                           2 charging ;
     *                           3 discharging ;
     *                           4 not charging ;
     *                           5 full
     * @param batteryLevel
     *            current battery value
     * @param batterySum
     *            total battery value
     */
    void onBatteryStatusChanged(int status, int batteryLevel, int batterySum);
}
