package com.baofeng.aone.date;

import com.baofeng.aone.AndroidCallback;


public interface DateCallback extends AndroidCallback{

    public void onUpdateDate(String date);
    public void onGetTime(String time);
    public void onGetDate(String date);
}
