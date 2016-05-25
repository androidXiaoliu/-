package com.baofeng.aone.bluetooth.receiver;

import com.baofeng.aone.bluetooth.callback.BluetoothStateChangedCallback;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothStateChangedReceiver extends BroadcastReceiver {
    private BluetoothStateChangedCallback cb;
    private static BluetoothStateChangedReceiver mReceiver;

    private BluetoothStateChangedReceiver(BluetoothStateChangedCallback cb) {
        this.cb = cb;
    }

    public static synchronized BluetoothStateChangedReceiver getInstance(BluetoothStateChangedCallback cb) {
        if (mReceiver == null) {
            mReceiver = new BluetoothStateChangedReceiver(cb);
        }
        return mReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
            cb.onStateChanged(4 + "");
        } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
            cb.onStateChanged(5 + "");
        } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
            case BluetoothAdapter.STATE_OFF:
                cb.onStateChanged(0 + "");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                cb.onStateChanged(1 + "");
                break;
            case BluetoothAdapter.STATE_ON:
                cb.onStateChanged(2 + "");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                cb.onStateChanged(3 + "");
                break;
            default:
                cb.onStateChanged(BluetoothAdapter.ERROR + "");
                break;
            }
        }
    }
}