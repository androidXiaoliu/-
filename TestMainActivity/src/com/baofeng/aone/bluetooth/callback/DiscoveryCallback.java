package com.baofeng.aone.bluetooth.callback;

import com.baofeng.aone.AndroidCallback;

public interface DiscoveryCallback extends AndroidCallback {
    public void onResult(String json);
}
