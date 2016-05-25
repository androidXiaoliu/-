package com.baofeng.aone.fota;

import com.baofeng.aone.AndroidCallback;

public interface OTACallback extends AndroidCallback{

    public void onOTAStart(int status);
    public void onOTAPause(boolean result);
    public void onOTAResume(boolean result);
    public void onOTACancel(boolean result);
    public void onGetOTAStatus(int status);
}
