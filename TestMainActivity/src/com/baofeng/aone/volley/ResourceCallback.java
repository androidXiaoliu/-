package com.baofeng.aone.volley;

import com.baofeng.aone.AndroidCallback;

public interface ResourceCallback extends AndroidCallback {

    public void onImagePath(String url, String path);
    public void onImageBytes(String url, ReadData images);
    public void onStringResult(String url, String res);
    public void onJsonResult(String url, String json);
    public void onResultFail(String url, String res);
}
