package com.baofeng.aone.wifi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;
import android.util.Log;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.wifi.callback.WifiSettingsCallback;
import com.baofeng.aone.wifi.callback.WifiSystemUICallback;
import com.baofeng.aone.wifi.receiver.WifiSystemUIReceiver;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class WifiManager extends AndroidManager {
    /* no password */
    private static final int TYPE_NO_PASSWD = 0;
    /* WEP encryption */
    private static final int TYPE_WEP = 1;
    /* WPA encryption */
    private static final int TYPE_WPA = 2;
    /* EAP encryption */
    private static final int TYPE_EAP = 3;

    private static WifiManager mWifiManager;
    private static android.net.wifi.WifiManager mWfMgr;
    private static Context mContext;
    private static WifiInfo mWifiInfo;
    private List<ScanResult> mWfList;
    private String json;
    /* history of already connected previously */
    private List<WifiConfiguration> mWfCons;
    private WifiSettingsCallback callback;
    private WifiSystemUICallback cb;
    private static ArrayList<ResultBean> resultList;
    private WifiSystemUIReceiver receiver;
    /* to remember the pre-connected wifi ssid */
    private String preConnectedSSID;

    public static WifiManager getAndroidManager() {
        return getInstance();
    }
    /**
     * 注册wifi状态监听
     * @param cb
     */
    public void registSystemUICallback(WifiSystemUICallback cb) {
        this.cb = cb;
         receiver = WifiSystemUIReceiver.getInstance(cb);
        IntentFilter filter = new IntentFilter();
        filter.addAction(
                android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(
                android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(receiver, filter);
    }

    /**
     * regist the callback,this method must be invoked before other methods,but
     * after getAndroidManager() when user goes into WifiSettings,invoke this
     * method
     *注册回调 监听wifi的断开 连接 忘记密码的事件
     * @param cb
     */
    public void registWifiSettingsCallback(WifiSettingsCallback cb) {
        callback = cb;
        if (mWfMgr == null) {
            mWfMgr = (android.net.wifi.WifiManager) mContext
                    .getSystemService(Context.WIFI_SERVICE);
        }
    }

    /**
     * unregist the callback,this method must be invoked after closeWifi() user
     * leave WifiSettings,invoke this method.
     */
    public void unregistWifiSettingsCallback() {
    	if(receiver!=null&&mContext!=null){
    		mContext.unregisterReceiver(receiver);
    	}
        callback = null;
        json = "";
        mWfCons = null;
        mWfMgr = null;
        mWifiInfo = null;
        resultList = null;
    }

    public static synchronized WifiManager getInstance() {
        if (mWifiManager == null) {
            mWifiManager = new WifiManager();
        }
        mContext = LauncherApplication.getInstance();
        mWfMgr = (android.net.wifi.WifiManager) mContext
                .getSystemService(Context.WIFI_SERVICE);
        resultList = new ArrayList<>();
        return mWifiManager;
    }

    private WifiManager() {
    }

    /**
     * judge the wifi whether open
     *
     * @return
     */
    public String isWifiOpen() {
        return mWfMgr.isWifiEnabled() ? "true" : "false";
    }

    /**
     * open the wifi and start to scan
     */
    public void openWifi() {
        if (callback == null) {
            return;
        }
        Log.d("login", "----start open---");
        if (!mWfMgr.isWifiEnabled()) {
            boolean r = mWfMgr.setWifiEnabled(true);
            Log.d("login", "----end open---r = "+r);
        }
        startScan();
    }

    /**
     * close the wifi
     */
    public void closeWifi() {
        if (mWfMgr.isWifiEnabled()) {
            mWfMgr.setWifiEnabled(false);
        }
    }

    /**
     * get the connected wifi information
     *
     * @return the Wifi information
     */
    private WifiInfo getWifiApInfo() {
        mWifiInfo = mWfMgr.getConnectionInfo();
        return (mWifiInfo == null) ? null : mWifiInfo;
    }

    /**
     * disconnect the specifed wifi
     *
     * @param iNetId
     */
    public void disconnectWifiAp(String strSSID) {
        if (callback == null) {
            return;
        }
        int netId = isExsitConfig(strSSID).networkId;
        boolean b1 = mWfMgr.disableNetwork(netId);
        boolean b2 = mWfMgr.disconnect();
        callback.onDisconnected(b1 && b2, sortResultList(json));
    }

    /**
     * connect the specifed wifi
     *
     * @param strSSID
     *            the wifi name
     */
    public void connectWifiAp(String strSSID) {
        if (callback == null) {
            return;
        }
        int netId = isExsitConfig(strSSID).networkId;
        boolean b = mWfMgr.enableNetwork(netId, true);
        callback.onConnected(b ? 1 : 2, sortResultList(json));

    }

    /**
     * whether the specified SSID is connected
     *
     * @param Ssid
     *            the specified wifi ssid
     * @return true if connected or already remember the password,else false
     */
    public String whetherConnected(String ssid) {

        WifiConfiguration con = isExsitConfig(ssid);
        if (con == null) {
            return "false";
        }
        int netID = con.networkId;
        int connectID = getWifiApInfo().getNetworkId();
        return netID == connectID && isSaved(ssid) ? "true" : "false";
    }

    /**
     * depend on the password and encryption type to connect the network
     *
     * @param strSSID
     *            the wifi's ssid
     * @param strPsd
     *            the secret
     * @param iType
     *            the encryption type
     */
    public void addNetWork(String strSSID, String strPsd, String type) {
        if (callback == null) {
            return;
        }
        int iType = Integer.parseInt(type);
        WifiConfiguration wifiCon = createWifiConfig(strSSID, strPsd, iType);
        boolean b = false;
        if (wifiCon != null) {
            int iNetId = mWfMgr.addNetwork(wifiCon);
            if (-1 != iNetId) {
                b = mWfMgr.enableNetwork(iNetId, true);
            }
        }
        startScan();
        callback.onConnected(b ? 3 : 4,
                new Gson().toJson(sortResultList(resultList)));
    }

    /**
     * forget the specify network
     *
     * @param strSSID
     * @param callback
     */
    public void removeNetWork(String strSSID) {
        if (callback == null) {
            return;
        }
        mWifiInfo = getWifiApInfo();
        WifiConfiguration tempConfig = isExsitConfig(strSSID);
        if (resultList == null || resultList.isEmpty()) {
            startScan();
        }
        for (ResultBean bean : resultList) {
            if (bean.getSSID().contains(strSSID)) {
                bean.setConnected(false);
                bean.setForget(true);
            }
        }
        if (tempConfig != null) {
            boolean b = mWfMgr.removeNetwork(tempConfig.networkId);
            callback.onForgeted(b,
                    new Gson().toJson(sortResultList(resultList)));
        }
    }

    public void startScan() {
        if (mWfMgr.startScan()) {
            mWfList = mWfMgr.getScanResults();
            mWfCons = mWfMgr.getConfiguredNetworks();
        }
        getAllNetworkList();
    }

    private String calculateType(String encryption) {
        int result = -1;
        if (encryption.contains("WPA")) {
            result = 2;
        } else if (encryption.contains("WEP")) {
            result = 1;
        } else if (encryption.contains("EAP")) {
            result = 3;
        } else {
            result = 0;
        }
        return result + "";
    }

    /**
     * judge the wifi's secret has already been saved
     *
     * @param ssid
     * @return
     */
    private boolean isSaved(String ssid) {
        return isExsitConfig(ssid) == null ? false : true;
    }

    private void getAllNetworkList() {
        HashMap<String, ScanResult> map = null;
        mWifiInfo = mWfMgr.getConnectionInfo();
        if (mWfList == null) {
            callback.onScanResult(false, "");
        } else {
            // duplicate removal
            map = new HashMap<>();
            for (ScanResult result : mWfList) {
                String ssid = result.SSID;
                if (map.isEmpty() || !map.containsKey(ssid)) {
                    map.put(ssid, result);
                }
            }
            json = new Gson().toJson(filterData(map.values()));
            callback.onScanResult(true, sortResultList(json));
        }
    }

    private ArrayList<ResultBean> filterData(
            Collection<ScanResult> collection) {
        ArrayList<ResultBean> list = new ArrayList<>();
        for (Iterator<ScanResult> iterator = collection.iterator(); iterator
                .hasNext();) {
            ScanResult next = iterator.next();
            ResultBean bean = new ResultBean();
            String sSID = next.SSID;
            bean.setSSID(sSID);
            boolean b = false;
            for (WifiConfiguration config : mWfCons) {
                if (config.SSID.equals("\"" + sSID + "\"")) {
                    b = true;
                    break;
                }
            }
            if ((b && mWifiInfo.getSSID().equals("\"" + sSID + "\""))
                    || (b && sSID.equals(preConnectedSSID))) {
                bean.setConnected(true);
                preConnectedSSID = bean.getSSID();
                bean.setForget(false);
            } else {
                bean.setConnected(false);
                bean.setForget(true);
            }
            bean.setFrequency(next.frequency);
            bean.setLevel(calculateSignalLevel(next.level));
            String security = getSecurity(next.capabilities);
            if (TextUtils.isEmpty(security)) {
                bean.setEncryption(false);
            } else {
                bean.setEncryption(true);
            }
            bean.setType(calculateType(security));
            bean.setSecurity(security);
            if (bean.isConnected() && !bean.isForget()) {
                list.add(0, bean);
            } else {
                list.add(bean);
            }
        }
        resultList.clear();
        resultList.addAll(list);
        return sortResultList(list);

    }

    /**
     * sort the scan result
     *
     * @param json
     *            the scan result
     * @return
     */
    private String sortResultList(String json) {
        Gson gson = new Gson();
        ArrayList<ResultBean> list = gson.fromJson(json,
                new TypeToken<ArrayList<ResultBean>>() {
                }.getType());
        list = sortResultList(list);
        return gson.toJson(list);

    }

    private ArrayList<ResultBean> sortResultList(ArrayList<ResultBean> list) {
        if (mWifiInfo != null && TextUtils.isEmpty(mWifiInfo.getSSID())
                && list.get(0).isConnected()) {
            Collections.sort(list.subList(1, list.size()),
                    new Comparator<ResultBean>() {
                        @Override
                        public int compare(ResultBean lhs, ResultBean rhs) {
                            return rhs.getLevel() - lhs.getLevel();
                        }
                    });
        } else {
            Collections.sort(list, new Comparator<ResultBean>() {
                @Override
                public int compare(ResultBean lhs, ResultBean rhs) {
                    return rhs.getLevel() - lhs.getLevel();
                }
            });
        }
        return list;
    }

    private String getSecurity(String capabilities) {
        String encryption = "";
        if (capabilities.contains("PSK") || capabilities.contains("psk")) {

            if ((capabilities.contains("WPA") || capabilities.contains("wpa"))
                    && (capabilities.contains("WPA2")
                            || capabilities.contains("wpa2"))) {
                encryption = "WPA/WPA2 PSK";
            } else if (capabilities.contains("WPA2")
                    || capabilities.contains("wpa2")) {
                encryption = "WPA2 PSK";
            } else {
                encryption = "WPA PSK";
            }
        } else if (capabilities.contains("WEP")
                || capabilities.contains("wep")) {
            encryption = "WEP";
        } else if (capabilities.contains("EAP")
                || capabilities.contains("eap")) {
            encryption = "EAP";
        } else if (capabilities.contains("WPA")
                || capabilities.contains("wpa")) {
            if ((capabilities.contains("WAP2") || capabilities.contains("wpa2"))
                    && (capabilities.contains("WPA")
                            || capabilities.contains("wpa"))) {
                encryption = "WAP/WPA2";
            } else if (capabilities.contains(capabilities.concat("WAP2"))
                    || capabilities.contains("wap2")) {
                encryption = "WPA2";
            } else {
                encryption = "WPA";
            }
        }
        return encryption;
    }

    /**
     * calculate the singnal level
     *
     * @param level
     * @return
     */
    public static int calculateSignalLevel(int level) {
        int singnal = 0;
        if (level >= -55) {
            singnal = 4;
        } else if (level >= -70) {
            singnal = 3;
        } else if (level >= -85) {
            singnal = 2;
        } else if (level >= -100) {
            singnal = 1;
        }
        return singnal;
    }

    /**
     * judge the WifiConfiguration whether the SSID contained
     *
     * @param strSSID
     * @return
     */
    private WifiConfiguration isExsitConfig(String strSSID) {
        mWfCons = mWfMgr.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : mWfCons) {
            if (existingConfig.SSID.equals("\"" + strSSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createNewConfig() {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        return config;
    }

    /**
     * set the configuration without password
     *
     * @param config
     */
    private void setNoPsdConfig(WifiConfiguration config) {
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.wepTxKeyIndex = 0;
    }

    /**
     * set the configuration with WEP encryption
     *
     * @param config
     * @param strPsd
     */
    private void setWepConfig(WifiConfiguration config, String strPsd) {
        config.hiddenSSID = true;
        config.wepKeys[0] = "\"" + strPsd + "\"";
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.SHARED);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        config.wepTxKeyIndex = 0;
    }

    /**
     * set the configuration with WPA encryption
     *
     * @param config
     * @param strPsd
     */
    private void setWpaConfig(WifiConfiguration config, String strPsd) {
        config.preSharedKey = "\"" + strPsd + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;
    }

    /**
     * set the configuration with EAP encryption
     *
     * @param config
     * @param strPsd
     */
    private void setEapConfig(WifiConfiguration config, String strPsd) {
        // TODO
    }

    private WifiConfiguration createWifiConfig(String strSSID, String strPsd,
            int iType) {
        WifiConfiguration config = createNewConfig();
        config.SSID = "\"" + strSSID + "\"";
        WifiConfiguration tempConfig = isExsitConfig(strSSID);
        if (tempConfig != null) {
            mWfMgr.removeNetwork(tempConfig.networkId);
        }
        switch (iType) {
        case TYPE_NO_PASSWD:
            setNoPsdConfig(config);
            break;
        case TYPE_WEP:
            setWepConfig(config, strPsd);
            break;
        case TYPE_WPA:
            setWpaConfig(config, strPsd);
            break;
        // case TYPE_EAP:
        // setEapConfig(config, strPsd);
        // break;
        default:
            config = null;
            break;
        }
        return config;
    }
}