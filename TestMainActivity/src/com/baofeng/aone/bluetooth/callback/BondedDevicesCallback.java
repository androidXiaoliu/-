package com.baofeng.aone.bluetooth.callback;

import com.baofeng.aone.AndroidCallback;

/**
 * get the already paired devices
 *
 * @author donghuajie
 *
 */
public interface BondedDevicesCallback extends AndroidCallback {
    public void onResult(String json);
}
