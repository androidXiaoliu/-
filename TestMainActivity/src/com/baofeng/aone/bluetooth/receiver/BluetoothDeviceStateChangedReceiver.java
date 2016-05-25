package com.baofeng.aone.bluetooth.receiver;

import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.bluetooth.callback.DeviceStateChangedCallback;
import com.baofeng.aone.bluetooth.utils.Utils;
import com.google.gson.Gson;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothDeviceStateChangedReceiver extends BroadcastReceiver {
    private static BluetoothDeviceStateChangedReceiver mReceiver;
    private static DeviceStateChangedCallback callback;

    private BluetoothDeviceStateChangedReceiver(DeviceStateChangedCallback cb) {
        callback = cb;
    }

    public static synchronized BluetoothDeviceStateChangedReceiver getInstance(DeviceStateChangedCallback cb) {
        if (mReceiver == null) {
            mReceiver = new BluetoothDeviceStateChangedReceiver(cb);
        }
        return mReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String newName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            callback.onDeviceNameChanged(new Gson().toJson(Utils.fillData(device, newName)));
        } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String json = new Gson().toJson(Utils.fillData(device));
            int currentState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
            if (currentState == BluetoothDevice.BOND_NONE) {
                //unpaired
            	String EXTRA_REASON = (String)ReflectUtil.getField(BluetoothDevice.class, "EXTRA_REASON");
                int reason = intent.getIntExtra(EXTRA_REASON, 0);
                callback.onPairStateChanged(1+"", json,reason+"");
            }else if (BluetoothDevice.BOND_BONDING == currentState){
                //pairing
                callback.onPairStateChanged(2+"",new Gson().toJson(Utils.fillData(device)),0+"");
            }else if (BluetoothDevice.BOND_BONDED == currentState) {
                //paired
                callback.onPairStateChanged(3+"", new Gson().toJson(Utils.fillData(device)),0+"");
            }
        } else if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //pairing
            callback.onPairStateChanged("2", new Gson().toJson(Utils.fillData(device)),0+"");
        } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
            int currentState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Gson gson = new Gson();
            String json = gson.toJson(Utils.fillData(device));
            if(BluetoothAdapter.STATE_DISCONNECTED == currentState){
                //disconnected
                callback.onConnectionStateChanged(0+"",json);
            }else if (BluetoothAdapter.STATE_CONNECTING == currentState){
                //connecting
                callback.onConnectionStateChanged(1+"",json);
            }else if(BluetoothAdapter.STATE_CONNECTED == currentState) {
                //connected
                callback.onConnectionStateChanged(2+"",json);
            }else if(BluetoothAdapter.STATE_DISCONNECTING == currentState){
                //disconnecting
                callback.onConnectionStateChanged(3+"",json);
            }
        }
    }
}
