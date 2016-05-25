package com.baofeng.aone.packagemanager.callback;

import com.baofeng.aone.AndroidCallback;

/*
 * register on applist page
 */
public interface PackageChangeCallback extends AndroidCallback {
    public void onInstallPackageInfo(String msg);

    public void onUninstallPackageInfo(String msg);

    public void onPackageUpdateInfo(String msg);
}
