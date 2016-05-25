package com.baofeng.aone.download;

import com.baofeng.aone.AndroidCallback;

public interface DownloadListCallback extends AndroidCallback {

    public void onDownloadFile(String content);

}
