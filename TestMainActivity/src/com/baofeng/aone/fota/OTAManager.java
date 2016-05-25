package com.baofeng.aone.fota;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.fota.IOTAService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

public class OTAManager extends AndroidManager{
    public static final String TAG = "OTAManager";
    public static final boolean DEBUG = true;
    public static final String OTA_PACKAGE_NAME = "com.baofeng.fota";
    public static final String OTA_CLASS_NAME = "com.baofeng.fota.OTAService";
    public static final String OTA_STATUS= "ota_status";
    public static final int STATUS_NOT_DOWNLOAD = 1;

    //The return value check for updates
    public static final int START_SUCCESSFUL = 0;
    public static final int START_FAILED = 1;
    public static final int START_NO_NEW_VERSION = 2;

    private static OTAManager mManager;
    private IOTAService mService;
    private Context mContext;

    private OTAManager() {
        mContext = LauncherApplication.getInstance();
        Intent intent = new Intent();
        intent.setClassName(OTA_PACKAGE_NAME, OTA_CLASS_NAME);
        mContext.bindService(intent, mConnection, Service.BIND_AUTO_CREATE);
    }

    public static AndroidManager getAndroidManager() {
        return getInstance();
    }

    private static OTAManager getInstance() {
        if (mManager == null) {
            mManager = new OTAManager();
        }
        return mManager;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            if (DEBUG)
                Log.d(TAG, "Service disconnected.");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IOTAService.Stub.asInterface(service);
            if (DEBUG)
                Log.d(TAG, "Service connected.");
        }
    };


    /**
     * pause OTA download
     * @param callback OTACallback
     */
    public void pause(OTACallback callback) {
        boolean result = true;
        if (mService != null) {
            try {
                result = mService.pause();
            } catch (RemoteException e) {
                e.printStackTrace();
                result = false;
            }
            callback.onOTAPause(result);
        }
    }

    /**
     * resume OTA download
     * @param callback OTACallback
     */
    public void resume(OTACallback callback) {
        boolean result = false;
        if (mService != null) {
            try {
                result = mService.resume();
            } catch (RemoteException e) {
                e.printStackTrace();
                result = false;
            }
            callback.onOTAResume(result);
        }
    }

    /**
     * cancel OTA download
     * @param callback OTACallback
     */
    public void cancel(OTACallback callback) {
        boolean result = true;
        if (mService != null) {
            try {
                result = mService.cancel();
            } catch (RemoteException e) {
                e.printStackTrace();
                result = false;
            }
            callback.onOTACancel(result);
        }
    }
    /**
     *  start OTA check for update
     * @param callback OTACallback
     */
    public void start(OTACallback callback) {
        //START_STATUS: 0: success 1: fail 2: not need upgrade
        int status = START_SUCCESSFUL;
        if (mService != null) {
            try {
                status = mService.start();
            } catch (RemoteException e) {
                e.printStackTrace();
                status = START_FAILED;
            }
            callback.onOTAStart(status);
        }
    }

    /**
     * go to upgrade mode
     */
    public void goToUpgradeMode() {
        if (mService != null) {
            try {
                mService.goToUpgradeMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get OTA download status
     * 1:STATUS_NOT_DOWNLOAD
     * 2:STATUS_DOWNLOADING
     * 3:STATUS_DOWNLOADED
     * @param callback
     */
    public void getOTAStatus(OTACallback callback){
        int status = 1;
        try{
            status = Settings.System.getInt(mContext.getContentResolver(), OTA_STATUS, STATUS_NOT_DOWNLOAD);
        }catch (Exception e) {
            Log.e(TAG, "getOTAStatus has exception : " + e.getMessage());
        }
        callback.onGetOTAStatus(status);
    }
}
