package com.baofeng.aone.bluetooth.profile;

import java.util.ArrayList;
import java.util.List;

import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.bluetooth.BluetoothManager;

import android.app.LauncherActivity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.os.ParcelUuid;

public class A2dpProfile implements LocalBluetoothProfile {

    private static boolean mIsProfileReady;
    private static BluetoothA2dp mService;

    private static BluetoothManager mManager;
    private static CachedBluetoothDeviceManager mDeviceManager;

    static final ParcelUuid[] SINK_UUIDS = { BluetoothUuid.AudioSink, BluetoothUuid.AdvAudioDist, };

    public static final String NAME = "A2DP";
    private static LocalBluetoothProfileManager mProfileManager;

    private static final int ORDINAL = 1;

    public class A2dpServiceListener implements BluetoothProfile.ServiceListener {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            mService = (BluetoothA2dp) proxy;
            mProfileManager = LocalBluetoothProfileManager.getInstance();
            // We just bound to the service, so refresh the UI for any connected
            // A2DP devices.
            List<BluetoothDevice> deviceList = mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = mDeviceManager.findDevice(nextDevice);
                // we may add a new device here, but generally this should not
                // happen
                if (device == null) {
                    device = mDeviceManager.addDevice(mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(A2dpProfile.this, BluetoothProfile.STATE_CONNECTED);
                device.refresh();
            }
            mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            mIsProfileReady = false;
        }
    }

    public A2dpProfile() {
        mManager = BluetoothManager.getAndroidManager();
        mDeviceManager = CachedBluetoothDeviceManager.getInstance();
        mManager.getProfileProxy(Device.Major.AUDIO_VIDEO + "", new A2dpServiceListener());
    }

    public String connectToDevice(BluetoothDevice device) {
        return connect(device) ? "true" : "false";
    }

    @Override
    public boolean connect(BluetoothDevice device) {
        if (mService == null) {
            return false;
        }
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
            	ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "disconnect", sink);
                
            }
        }
        Boolean result = (Boolean)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "connect", device);
        return result.booleanValue();
    }

    public String disconnectToDevice(BluetoothDevice device) {
        return disconnect(device) ? "true" : "false";
    }

    @Override
    public boolean disconnect(BluetoothDevice device) {
        if (mService == null)
            return false;
        // Downgrade priority as user is disconnecting the headset.
        int result = (Integer)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "getPriority", device);
        int priority_on = (Integer)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_ON");
        if (result >priority_on) {
        	ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "setPriority", device, priority_on);
        }
        Boolean disconnectResult = (Boolean)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "disconnect", device);
        return disconnectResult.booleanValue();
    }

    @Override
    public int getConnectionStatus(BluetoothDevice device) {
        if (mService == null) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
        return mService.getConnectionState(device);
    }

    boolean isA2dpPlaying() {
        if (mService == null)
            return false;
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (!sinks.isEmpty()) {
            if (mService.isA2dpPlaying(sinks.get(0))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isConnectable() {
        return true;
    }

    @Override
    public boolean isAutoConnectable() {
        return true;
    }

    @Override
    public boolean isPreferred(BluetoothDevice device) {
        if (mService == null)
            return false;
        int result = (Integer)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "getPriority", device);
        int priority_off = (Integer)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        return result > priority_off;
    }

    @Override
    public int getPreferred(BluetoothDevice device) {
         int priority_off = (Integer)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        if (mService == null)
            return priority_off;
        int result = (Integer)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "getPriority", device);
        return result;
    }

    @Override
    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (mService == null)
            return;
        int result = (Integer)ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "getPriority", device);
        int priority_off = (Integer)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        int priority_on = (Integer)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_ON");
        if (preferred) {
            if (result < priority_on) {
            	ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "setPriority", device, priority_on);
            }
        } else {
        	ReflectUtil.invorkMethod(BluetoothA2dp.class, mService, "setPriority", device, priority_off);
        }
    }

    @Override
    public boolean isProfileReady() {
        return mIsProfileReady;
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }

    @Override
    public String toString() {
        return NAME;
    }
}
