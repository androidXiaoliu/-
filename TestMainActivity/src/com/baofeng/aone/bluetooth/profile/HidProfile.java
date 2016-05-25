package com.baofeng.aone.bluetooth.profile;

import java.util.List;

import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
//import android.bluetooth.BluetoothInputDevice;
import android.content.Context;
import android.util.Log;

public class HidProfile implements LocalBluetoothProfile {

    public static final String NAME = "HID";
    private static BluetoothProfile mService;
    private static boolean mIsProfileReady;

    private static BluetoothManager mManager;
    private static CachedBluetoothDeviceManager mDeviceManager;
    private static LocalBluetoothProfileManager mProfileManager;

    // Order of this profile in device profiles list
    private static final int ORDINAL = 3;
    // These callbacks run on the main thread.
    public class InputDeviceServiceListener implements BluetoothProfile.ServiceListener {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
        	    mService = proxy;
        	    Log.d("HidProfile", "----mService = "+mService.toString());
            mProfileManager = LocalBluetoothProfileManager.getInstance();
            // We just bound to the service, so refresh the UI for any connected
            // HID devices.
            List<BluetoothDevice> deviceList = (List<BluetoothDevice>) ReflectUtil.invoke( mService, "getConnectedDevices");
            Log.d("HidProfile", "---devicelist = "+deviceList);
//            List<BluetoothDevice> deviceList = mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = mDeviceManager.findDevice(nextDevice);
                // we may add a new device here, but generally this should not
                // happen
                if (device == null) {
                    device = mDeviceManager.addDevice(mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(HidProfile.this, BluetoothProfile.STATE_CONNECTED);
                device.refresh();
            }
            mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            mIsProfileReady = false;
        }
    }

    public HidProfile() {
        mDeviceManager = CachedBluetoothDeviceManager.getInstance();
        mManager = BluetoothManager.getAndroidManager();
        mManager.getProfileProxy(Device.Major.PERIPHERAL + "", new InputDeviceServiceListener());
    }

    public String connectToDevice(BluetoothDevice device) {
        return connect(device) ? "true" : "false";
    }

    @Override
    public boolean connect(BluetoothDevice device) {
        if (mService == null)
            return false;
        boolean result =(boolean)ReflectUtil.invoke( mService, "connect", device);
        Log.d("HidProfile", "----connect result = "+result);
        return result;
    }

    public String disconnectToDevice(BluetoothDevice device) {
        return disconnect(device) ? "true" : "false";
    }

    @Override
    public boolean disconnect(BluetoothDevice device) {
        if (mService == null)
            return false;
        boolean result =(boolean)ReflectUtil.invoke( mService, "disconnect", device);
        return result;
//        return mService.disconnect(device);
    }

    @Override
    public int getConnectionStatus(BluetoothDevice device) {
        if (mService == null) {
            return BluetoothProfile.STATE_DISCONNECTED;
        }
        List<BluetoothDevice> deviceList =(List<BluetoothDevice>)ReflectUtil.invoke( mService, "getConnectedDevices", device);

//        List<BluetoothDevice> deviceList = mService.getConnectedDevices();
       int state = (int) ReflectUtil.invoke( mService, "getConnectionState", device);

        return !deviceList.isEmpty() && deviceList.get(0).equals(device) ? state
                : BluetoothProfile.STATE_DISCONNECTED;
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
        int getPriority = (int)ReflectUtil.invoke( mService, "getPriority", device);
        int PRIORITY_OFF = (int)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        return getPriority > PRIORITY_OFF;
    }

    @Override
    public int getPreferred(BluetoothDevice device) {
          int PRIORITY_OFF = (int)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        if (mService == null)
            return PRIORITY_OFF;
        int getPriority = (int)ReflectUtil.invoke( mService, "getPriority", device);
        return getPriority;
    }

    @Override
    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (mService == null)
            return;
        int getPriority = (int)ReflectUtil.invoke( mService, "getPriority", device);
        int PRIORITY_OFF = (int)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_OFF");
        int PRIORITY_ON = (int)ReflectUtil.getField(BluetoothProfile.class, "PRIORITY_ON");
        if (preferred) {
            if (getPriority < PRIORITY_ON) {
            	ReflectUtil.invoke( mService, "setPriority", device, PRIORITY_ON);
//                mService.setPriority(device, PRIORITY_ON);
            }
        } else {
        	ReflectUtil.invoke( mService, "setPriority", device, PRIORITY_OFF);
//            mService.setPriority(device, PRIORITY_OFF);
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
