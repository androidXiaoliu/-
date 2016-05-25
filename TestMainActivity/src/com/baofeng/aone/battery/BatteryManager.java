package com.baofeng.aone.battery;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;

public class BatteryManager extends AndroidManager {
    private static BatteryManager mBatteryManager;
    private static Context mContext;

    public static BatteryManager getAndroidManager() {
        return getSingleton();
    }

    public static synchronized BatteryManager getSingleton() {
        if (mBatteryManager == null) {
            mBatteryManager = new BatteryManager();
        }
        mContext = LauncherApplication.getInstance();
        return mBatteryManager;
    }

    public void registerCallback(BatteryCallback callback) {
        BatteryReceiver mBatteryReceiver = new BatteryReceiver(callback);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(mBatteryReceiver, filter);
    }
}
