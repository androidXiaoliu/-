package com.baofeng.aone.bluetooth.receiver;

import com.baofeng.aone.bluetooth.bean.DeviceBean;
import com.baofeng.aone.bluetooth.callback.DiscoveryCallback;
import com.baofeng.aone.bluetooth.utils.Utils;
import com.google.gson.Gson;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DiscoveryReceiver extends BroadcastReceiver {

    private DiscoveryCallback cb;
    private static DiscoveryReceiver receiver;

    private DiscoveryReceiver(DiscoveryCallback cb) {
        this.cb = cb;
    }

    public synchronized static DiscoveryReceiver getDiscoveryReceiver(DiscoveryCallback cb) {
        if (receiver == null) {
            receiver = new DiscoveryReceiver(cb);
        }
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) {
                return;
            }
            DeviceBean bean = new DeviceBean();
            bean.setAddress(device.getAddress());
            bean.setName(device.getName());
            int type = device.getBluetoothClass().getMajorDeviceClass();
            bean.setType(Utils.getType(type));
            cb.onResult(new Gson().toJson(bean));
        }
    }
}
