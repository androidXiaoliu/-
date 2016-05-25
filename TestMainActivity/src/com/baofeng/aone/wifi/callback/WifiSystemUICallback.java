package com.baofeng.aone.wifi.callback;

import com.baofeng.aone.AndroidCallback;

public interface WifiSystemUICallback extends AndroidCallback {
    /**
     * wifi state has changed
     * @param state 0:wifi模块正在关闭
     *              1:wifi模块已关闭
     *              2:wifi模块正在打开
     *              3:wifi模块已打开
     */
    void onWifiChanged(String state);
    /**
     * network connection has changed
     * @param json a string of json ,that contains ssid and signalLevel
     *        if will return null when the network is not connected
     */
    void onNetworkChanged(String json);
}
