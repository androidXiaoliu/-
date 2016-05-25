package com.baofeng.aone.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
    private BatteryCallback callback;

    public BatteryReceiver(BatteryCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            // battery status
            // BatteryManager.BATTERY_STATUS_UNKNOWN 1 未知
            // BatteryManager.BATTERY_STATUS_CHARGING 2 充电状态
            // BatteryManager.BATTERY_STATUS_DISCHARGING 3 放电中
            // BatteryManager.BATTERY_STATUS_NOT_CHARGING 4 未充电
            // BatteryManager.BATTERY_STATUS_FULL 5 电池满

            int status = intent.getIntExtra("status",android.os.BatteryManager.BATTERY_STATUS_UNKNOWN);
            // current battery value
            int batteryLevel = intent.getIntExtra("level", 0);
            // the total battery value
            int batterySum = intent.getIntExtra("scale", 100);
            callback.onBatteryStatusChanged(status, batteryLevel, batterySum);
        }
    }
}
