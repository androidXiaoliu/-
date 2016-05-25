package com.baofeng.aone.bluetooth.callback;

import com.baofeng.aone.AndroidCallback;

/**
 * when the device's state has changed,it will call this
 *
 * @author donghuajie
 *
 */
public interface DeviceStateChangedCallback extends AndroidCallback {
    /**
     * device name changed
     *
     * @param json
     */
    void onDeviceNameChanged(String json);

    /**
     * device pari state changed
     *
     * @param state
     *            String    1 未配对
     *                      2 配对中
     *                      3 已配对
     * @param json  devicebean include some message
     *
     * @param unbondedReason:
     *          0：     默认
     *          1:  UNBOND_REASON_AUTH_FAILED           pins码不匹配或远端设备未及时应答
     *          2:  UNBOND_REASON_AUTH_REJECTED         远端设备拒绝
     *          3:  UNBOND_REASON_AUTH_CANCELED         取消配对过程
     *          4:  UNBOND_REASON_REMOTE_DEVICE_DOWN    无法与远端设备通信
     *          5:  UNBOND_REASON_DISCOVERY_IN_PROGRESS 配对时，蓝牙正在扫描
     *          6:  UNBOND_REASON_AUTH_TIMEOUT          认证超时
     *          7:  UNBOND_REASON_REPEATED_ATTEMPTS     重复操作
     *          8:  UNBOND_REASON_REMOTE_AUTH_CANCELED  远程设备取消认证
     *          9:  UNBOND_REASON_REMOVED               解除已有配对
     */
    void onPairStateChanged(String state, String json,String unbondedReason);

    /**
     * device connection state changed
     *
     * @param state
     *              String  0   已断开
     *                      1   正在连接
     *                      2   已连接
     *                      3   正在断开
     * @param json
     */
    void onConnectionStateChanged(String state, String json);
}
