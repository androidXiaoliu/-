//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.baofeng.aone.packagemanager;

import android.content.pm.PackageManager;
//import android.content.pm.IPackageInstallObserver.Stub;
//import android.content.pm.IPackageDeleteObserver;
//import android.app.PackageInstallObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.packagemanager.callback.packageOperationCallback;

public class AppPackageChangeObserver {
    private static final String TAG = "AppPackageChangeObserver";
//    AppPackageChangeObserver.AppPackageInstallObserver installObserver;
//    AppPackageChangeObserver.AppPackageDeleteObserver deleteObserver;
    private final int UNINSTALL_COMPLETE = 0;
    private final int INSTALL_COMPLETE = 1;
    packageOperationCallback mListener;
    static Class mPackageInstallObserver;
    static{
    	  mPackageInstallObserver = ReflectUtil.getClassFromName("android.app.PackageInstallObserver");
    }

    public AppPackageChangeObserver(packageOperationCallback mListener) {
//        this.mListener = mListener;
//        installObserver = new AppPackageInstallObserver();
//        deleteObserver = new AppPackageDeleteObserver();
    }

//    class AppPackageInstallObserver extends mPackageInstallObserver {
//
//        public void onPackageInstalled(String basePackageName, int returnCode,
//                String msg, Bundle extras) {
//            if (mListener != null) {
//                AppPackageChangeObserver.this.mListener
//                        .onInstallResult(returnCode);
//            }
//        }
//    }

    /*
     * 
     * class PackageInstallObserver extends IPackageInstallObserver.Stub {
     * PackageInstallObserver() { }
     * 
     * public void packageInstalled(String packageName, int returnCode) {
     * Message msg = AppPackageChangeObserver.this.mHandler.obtainMessage(1);
     * msg.arg1 = returnCode; msg.obj = packageName; //
     * AppPackageChangeObserver.this.mHandler.sendMessage(msg);
     * AppPackageChangeObserver.this.mListener.onInstallResult(msg.arg1); } }
     */
//    class AppPackageDeleteObserver extends IPackageDeleteObserver.Stub {
//
//        public void packageDeleted(String packageName, int returnCode) {
//             if(mListener != null) {
//            AppPackageChangeObserver.this.mListener
//                    .onUninstallResult(returnCode);
//             }
//        }
//    }

}
