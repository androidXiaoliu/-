package com.baofeng.aone.userdata;

import com.baofeng.aone.AndroidCallback;

public interface UserDataCallback extends AndroidCallback{

    public void onQuery(String msg);
    public void onInsert(String result);
    public void onUpdate(String result);

}
