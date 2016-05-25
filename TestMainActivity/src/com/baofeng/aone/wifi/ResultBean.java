package com.baofeng.aone.wifi;

public class ResultBean {
    private String SSID;
    private int level;
    private String security; // which way of encryption
    private int frequency;
    private boolean isConnected; // whether connected
    private boolean encryption; // whether encryption
    private boolean isForget; // whether forget the net
    private String type;           //the type of login 0:无密码；1；wep;2:wpa;3:eap(待定)

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isForget() {
        return isForget;
    }

    public void setForget(boolean isForget) {
        this.isForget = isForget;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public boolean isEncryption() {
        return encryption;
    }

    public void setEncryption(boolean encryption) {
        this.encryption = encryption;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String sSID) {
        SSID = sSID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

  
}
