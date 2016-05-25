package com.baofeng.aone.wifi.callback;

import com.baofeng.aone.AndroidCallback;

public interface WifiSettingsCallback extends AndroidCallback{
    /**
     *  state:true:获取到wifi列表
     *  false：未获取到wifi列表
     */
   public void onScanResult(boolean state,String json);
    /**
     * state :
     *      1:重新连接成功
     *      2：重新连接失败
     *      3：输入密码连接成功
     *      4：输入密码连接失败
     */
    public void onConnected(int state,String json);
    public void onDisconnected(boolean b,String json);
    public void onForgeted(boolean b,String json);
}
