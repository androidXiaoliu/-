package com.baofeng.aone.packagemanager.callback;

import com.baofeng.aone.AndroidCallback;

public interface packageOperationCallback extends AndroidCallback {

    void onInstallResult(int var1);

    void onUninstallResult(int var1);
}
