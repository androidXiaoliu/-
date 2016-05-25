package com.baofeng.aone.packagemanager.callback;

import com.baofeng.aone.AndroidCallback;
import com.baofeng.aone.packagemanager.IconData;

/*
 * applist get Icon image callback
 */
public interface IconCallback extends AndroidCallback {
    public void onApplicationIconBytes(IconData data);
}
