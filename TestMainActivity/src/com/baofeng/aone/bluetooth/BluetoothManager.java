package com.baofeng.aone.bluetooth;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.text.TextUtils;
import android.util.Log;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.bluetooth.bean.DeviceBean;
import com.baofeng.aone.bluetooth.bean.ResultBean;
import com.baofeng.aone.bluetooth.callback.BondedDevicesCallback;
import com.baofeng.aone.bluetooth.callback.DeviceStateChangedCallback;
import com.baofeng.aone.bluetooth.callback.DiscoveryCallback;
import com.baofeng.aone.bluetooth.callback.BluetoothStateChangedCallback;
import com.baofeng.aone.bluetooth.profile.HidProfile;
import com.baofeng.aone.bluetooth.profile.LocalBluetoothProfileManager;
import com.baofeng.aone.bluetooth.profile.A2dpProfile;
import com.baofeng.aone.bluetooth.profile.BluetoothUuid;
import com.baofeng.aone.bluetooth.profile.CachedBluetoothDeviceManager;
import com.baofeng.aone.bluetooth.receiver.DiscoveryReceiver;
import com.baofeng.aone.bluetooth.receiver.BluetoothDeviceStateChangedReceiver;
import com.baofeng.aone.bluetooth.receiver.BluetoothStateChangedReceiver;
import com.baofeng.aone.bluetooth.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class BluetoothManager extends AndroidManager {

    private static Context mContext;
    private static BluetoothManager mManager;
    private static BluetoothAdapter mAdapter;
    private static LocalBluetoothProfileManager mProfileManager;
    private int mState = BluetoothAdapter.ERROR;
    private DeviceStateChangedCallback devicesCallback;
    private BluetoothStateChangedReceiver stateReceiver;
    private DiscoveryReceiver discoverReceiver;
    private BluetoothDeviceStateChangedReceiver deviceReceiver;
    private DiscoveryCallback discoveryCallback;

    public static BluetoothManager getAndroidManager() {
        return getInstance();
    }

    private synchronized static BluetoothManager getInstance() {
        if (mManager == null) {
            mManager = new BluetoothManager();
        }
        mContext = LauncherApplication.getInstance();
        mAdapter = BluetoothAdapter.getDefaultAdapter();

        return mManager;
    }

    public void registBluetoothStateChangedCallback(BluetoothStateChangedCallback callback) {
        // this.callback = callback;
        stateReceiver = BluetoothStateChangedReceiver.getInstance(callback);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        mContext.registerReceiver(stateReceiver, filter);
    }

    public void registDiscoveryCallback(DiscoveryCallback callback) {
        discoverReceiver = DiscoveryReceiver.getDiscoveryReceiver(callback);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(discoverReceiver, filter);

    }

    public void registDeviceStateChangedCallback(DeviceStateChangedCallback callback) {
        deviceReceiver = BluetoothDeviceStateChangedReceiver.getInstance(callback);
        devicesCallback = callback;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        mContext.registerReceiver(deviceReceiver, filter);
    }

    public void unregistDeviceStateChangedCallback() {
        mContext.unregisterReceiver(deviceReceiver);
    }

    public void unregistBluetoothStateChangedCallback() {
        mContext.unregisterReceiver(stateReceiver);
    }

    public void unregistDiscoveryCallback() {
        mContext.unregisterReceiver(discoverReceiver);
    }

    /**
     * judge Bluetooth is enabled and ready for use
     *
     * @return true if the local adapter is turned on
     */
    public String isEnabled() {
        String result = "false";
        if (!isAdapterNull()) {
            result = mAdapter.isEnabled() ? "true" : "false";
        }
        return result;
    }

    /**
     * turn on Bluetooth
     *
     * @return true if Bluetooth is opened else return false;
     */
    public String enable() {
        String result = "fasle";
        if (!isAdapterNull()) {
            result = mAdapter.enable() ? "true" : "false";
        }
        return result;
    }

    /**
     * turn off bluetooht
     *
     * @return true if Bluetooth is shutdown else return false;
     */
    public String disable() {
        String result = "fasle";
        if (!isAdapterNull()) {
            result = mAdapter.disable() ? "true" : "false";
        }
        return result;
    }

    /**
     * Start the remote device discovery process.
     *
     * @return true on success, false on error
     */
    public String startDiscovery() {
        if (!isAdapterNull()) {
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }
            return mAdapter.startDiscovery() ? "true" : "false";
        }
        return "false";
    }

    /**
     * Cancel the current device discovery process
     *
     * @return true on success, false on error
     */
    public void cancelDiscovery() {
        if (!isAdapterNull()) {
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }
        }
    }

    public void pair(String address) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        BluetoothDevice remoteDevice = null;
        try {
            remoteDevice = mAdapter.getRemoteDevice(address);
        } catch (Exception e) {
            Log.e("dhj", "bluetooth device address is invalid");
        }
        switch (remoteDevice.getBondState()) {
        case BluetoothDevice.BOND_NONE:
            remoteDevice.createBond();
            break;
        default:
            break;
        }
    }

    public void setPin(String address, String pin) {
        if (TextUtils.isEmpty(address)) {
            return;
        }
        BluetoothDevice remoteDevice = null;
        try {
            remoteDevice = mAdapter.getRemoteDevice(address);
        } catch (Exception e) {
            Log.e("dhj", "address is invalid");
        }
        if (!TextUtils.isEmpty(pin)) {
            byte[] bytes = pin.getBytes();
            remoteDevice.setPin(bytes);
        }
    }

    public String unpair(String address) {
        BluetoothDevice remoteDevice = null;
        try {
            remoteDevice = mAdapter.getRemoteDevice(address);
        } catch (Exception e) {
            Log.e("dhj", "address is invalid");
        }
        if (null != remoteDevice) {
        	try{
        	Method remove = BluetoothDevice.class.getMethod("removeBond");
        	remove.setAccessible(true);
        	Boolean result = (Boolean)remove.invoke(remoteDevice);
        	return result.booleanValue() ? "true" : "false";
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        	
        }
        return "false";
    }

    public void connect(String address) {
        mProfileManager = LocalBluetoothProfileManager.getInstance();
        final BluetoothDevice device = getDevice(address);
        if (null != device) {
            switch (device.getBluetoothClass().getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.PERIPHERAL:
                HidProfile hid = (HidProfile) mProfileManager.getProfileByName(HidProfile.NAME);
                if (hid != null) {
                    boolean connect = hid.connect(device);
                }
                break;
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                A2dpProfile a2dp = (A2dpProfile) mProfileManager.getProfileByName(A2dpProfile.NAME);
                if (a2dp != null) {
                    boolean connect = a2dp.connect(device);
                }
                break;
            default:
                break;
            }
        }
    }

    public void disconnect(String address) {
        mProfileManager = LocalBluetoothProfileManager.getInstance();
        final BluetoothDevice device = getDevice(address);
        if (null != device) {
            switch (device.getBluetoothClass().getMajorDeviceClass()) {
            case BluetoothClass.Device.Major.PERIPHERAL:
                HidProfile hid = (HidProfile) mProfileManager.getProfileByName(HidProfile.NAME);
                if (hid != null) {
                    boolean disconnect = hid.disconnect(device);
                }
                break;
            case BluetoothClass.Device.Major.AUDIO_VIDEO:
                A2dpProfile a2dp = (A2dpProfile) mProfileManager.getProfileByName(A2dpProfile.NAME);
                if (a2dp != null) {
                    boolean disconnect = a2dp.disconnect(device);
                }
                break;
            default:
                break;
            }
        }
    }

    private BluetoothDevice getDevice(String address) {
        Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
        if (bondedDevices == null && bondedDevices.isEmpty()) {
            return null;
        }
        for (BluetoothDevice bluetoothDevice : bondedDevices) {
            if (address.equals(bluetoothDevice.getAddress())) {
                return bluetoothDevice;
            }
        }
        return null;
    }

    public void getProfileProxy(String type,
            BluetoothProfile.ServiceListener listener) {
        int profile = 0;
        if (type.equals((Device.Major.AUDIO_VIDEO) + "")) {
            profile = BluetoothProfile.A2DP;
        } else if (type.equals((Device.Major.PERIPHERAL) + "")) {
        	try{
        	Field filed = BluetoothProfile.class.getField("INPUT_DEVICE");
        	filed.setAccessible(true);
        	profile = filed.getInt(null);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        boolean b = mAdapter.getProfileProxy(LauncherApplication.getInstance(),listener, profile);
    }

    /**
     * depends on the callback to return the bonded devices as a string of json
     */
    public void getBondedDevices(BondedDevicesCallback callback) {
        Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
        ResultBean<DeviceBean> mBean = new ResultBean<DeviceBean>();
        ArrayList<DeviceBean> bondedList = new ArrayList<DeviceBean>();
        if (bondedDevices != null && !bondedDevices.isEmpty()) {
            for (Iterator<BluetoothDevice> iterator = bondedDevices.iterator(); iterator.hasNext();) {
                BluetoothDevice bluetoothDevice = (BluetoothDevice) iterator.next();
                DeviceBean bean = new DeviceBean();
                bean.setName(bluetoothDevice.getName());
                bean.setAddress(bluetoothDevice.getAddress());
                bean.setIsConnected(getBtIsConnected(bluetoothDevice) ? "true" : "false");
                int majorDeviceClass = bluetoothDevice.getBluetoothClass().getMajorDeviceClass();
                bean.setType(Utils.getType(majorDeviceClass));
                bondedList.add(bean);
            }
            mBean.setHasDatas(true);
        } else {
            mBean.setHasDatas(false);
        }
        mBean.setList(bondedList);
        Type type = new TypeToken<ResultBean<DeviceBean>>() {
        }.getType();
        callback.onResult(new Gson().toJson(mBean, type));
    }
    
    private boolean getBtIsConnected(BluetoothDevice bluetoothDevice){
    	try{
    		Method method = BluetoothDevice.class.getMethod("isConnected");
    		method.setAccessible(true);
    		Boolean result = (Boolean)method.invoke(bluetoothDevice);
    		return result.booleanValue();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	
    	return false;
    }

    /**
     * get the Bluetooth name of loacl bluetooth adapter
     *
     * @return
     */
    public String getName() {
        return mAdapter.getName();
    }

    /**
     * Set the friendly Bluetooth name of the local Bluetooth adapter
     *
     * @param name
     */
    public void setName(String name) {
        mAdapter.setName(name);
    }

    private static boolean isAdapterNull() {
        return mAdapter == null;
    }

    private Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }

    public ParcelUuid[] getUuids() {
    	try{
       	 Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("getUuids");
            setDiscoverableTimeout.setAccessible(true);
            ParcelUuid[] result = (ParcelUuid[]) setDiscoverableTimeout.invoke(mAdapter);
            return result;
       	}catch(Exception e){
       		e.printStackTrace();
       	}
        return null;
    }

    public boolean isDiscovering() {
        return isAdapterNull() ? false : mAdapter.isDiscovering();
    }

    public int getState() {
        return isAdapterNull() ? BluetoothAdapter.ERROR : mAdapter.getState();
    }

    public void setDiscoverableTimeout(int timeout) {
        if (!isAdapterNull()) {
        	try{
        	 Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
             setDiscoverableTimeout.setAccessible(true);
             setDiscoverableTimeout.invoke(mAdapter, timeout);
        	}catch(Exception e){
        		e.printStackTrace();
        	}
           
        }
    }

    public boolean setScanMode(int mode, int duration) {
        if (!isAdapterNull()) {
        	try{
        	  Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
              setScanMode.setAccessible(true);
 
              Boolean result = (Boolean)setScanMode.invoke(mAdapter, mode,duration);
            return result.booleanValue();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        return false;
    }

    public boolean setScanMode(int mode) {
    	try{
    	Method method = BluetoothAdapter.class.getMethod("setScanMode", int.class);
    	method.setAccessible(true);
    	Boolean result = (Boolean)method.invoke(mAdapter, mode);
    	return result.booleanValue();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return false;
    }

    public int getScanMode() {
        return isAdapterNull() ? BluetoothAdapter.SCAN_MODE_NONE : mAdapter.getScanMode();
    }

    public synchronized int getBluetoothState() {
        // Always sync state, in case it changed while paused
        syncBluetoothState();
        return mState;
    }

    boolean syncBluetoothState() {
        int currentState = mAdapter.getState();
        if (currentState != mState) {
            setBluetoothStateInt(mAdapter.getState());
            return true;
        }
        return false;
    }

    synchronized void setBluetoothStateInt(int state) {
        mProfileManager = LocalBluetoothProfileManager.getInstance();
        mState = state;
        if (state == BluetoothAdapter.STATE_ON) {
            // if mProfileManager hasn't been constructed yet, it will
            // get the adapter UUIDs in its constructor when it is.
            if (mProfileManager != null) {
                mProfileManager.setBluetoothStateOn();
            }
        }
    }
}
