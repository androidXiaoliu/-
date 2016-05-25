package com.baofeng.aone.wifi.receiver;

import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.wifi.ResultBean;
import com.baofeng.aone.wifi.callback.WifiSystemUICallback;
import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class WifiSystemUIReceiver extends BroadcastReceiver {
    private static WifiSystemUIReceiver mReceiver;
    private WifiSystemUICallback callback;
    private static Context mContext;
    private NetWorkBean<ResultBean> preBean;

    private WifiSystemUIReceiver(WifiSystemUICallback callback) {
        this.callback = callback;
    }

    public synchronized static WifiSystemUIReceiver getInstance(
            WifiSystemUICallback callback) {
        if (mReceiver == null) {
            mReceiver = new WifiSystemUIReceiver(callback);
        }
        mContext = LauncherApplication.getInstance();
        return mReceiver;
    }

    private String getJson(boolean isNetworkEnable) {
        NetWorkBean<ResultBean> mBean;

        if (isNetworkEnable) {
            mBean = new NetWorkBean<ResultBean>();
            mBean.setNetworkEnable(isNetworkEnable);
            WifiManager manager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();

            if (info == null) {
                return null;
            }
            ResultBean bean = new ResultBean();
            bean.setSSID(info.getSSID());
            int level = info.getRssi();
            int signalLevel = com.baofeng.aone.wifi.WifiManager
                    .calculateSignalLevel(level);
            bean.setLevel(signalLevel);
            mBean.setBean(bean);
            preBean = mBean;
        }else{
            if (preBean == null) {
                preBean = new NetWorkBean<ResultBean>();
            }
            preBean.setNetworkEnable(isNetworkEnable);
            mBean = preBean;
        }
        return new Gson().toJson(mBean);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
            switch (state) {
            case WifiManager.WIFI_STATE_DISABLING:
                callback.onWifiChanged(0 + "");
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                callback.onWifiChanged(1 + "");
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                callback.onWifiChanged("3");
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                callback.onWifiChanged("2");
                break;
            default:
                break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent
                .getAction())) {
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (null != networkInfo) {
                State state = networkInfo.getState();
                if (state == State.CONNECTED) {
                    callback.onNetworkChanged(getJson(true));
                } else {
                    callback.onNetworkChanged(getJson(false));
                }
            }
        }
    }

    private class NetWorkBean<T> {
        private boolean isNetworkEnable;
        private T bean;

        public boolean isNetworkEnable() {
            return isNetworkEnable;
        }

        public void setNetworkEnable(boolean isNetworkEnable) {
            this.isNetworkEnable = isNetworkEnable;
        }

        public T getBean() {
            return bean;
        }

        public void setBean(T bean) {
            this.bean = bean;
        }

    }
}
