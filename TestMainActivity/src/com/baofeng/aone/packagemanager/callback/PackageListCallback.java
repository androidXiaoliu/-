package com.baofeng.aone.packagemanager.callback;

import com.baofeng.aone.AndroidCallback;

/*
 * getAppPackageList
 */
public interface PackageListCallback extends AndroidCallback {
    public void onPackageListResult(String msg);
}
