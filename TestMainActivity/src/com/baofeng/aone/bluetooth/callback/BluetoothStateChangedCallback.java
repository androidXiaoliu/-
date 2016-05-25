package com.baofeng.aone.bluetooth.callback;

import com.baofeng.aone.AndroidCallback;

public interface BluetoothStateChangedCallback extends AndroidCallback {
    /**
     * Bluetooth state changed
     * @param state
     *    -2147483648:错误或默认状态
     *              0:蓝牙模块处于关闭状态
     *              1:蓝牙模块正在打开
     *              2:蓝牙模块处于开启状态
     *              3:蓝牙模块正在关闭
     *              4:开始扫描周围蓝牙设备
     *              5:扫描完成
     */
    void onStateChanged(String state);
}
