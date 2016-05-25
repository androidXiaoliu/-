package com.baofeng.aone.filemanager.bean;

import java.io.Serializable;

//import com.lenovo.leos.filebrowser.config.Constant;

public class Location extends Object implements Serializable {
    private static final String TAG = "Location";

    private static final long serialVersionUID = 1L;

    private int type;
    private String data;
    private String extra;

    public Location(int t, String d, String e) {
        type = t;
        data = d;
        extra = e;
    }

    public Location(int type) {
        this(type, null, null);
    }

    public Location(int type, String data) {
        this(type, data, null);
    }

    public Location clone() {
        return new Location(type, data, extra);
    }

    public void setType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }

    public void setData(String d) {
        data = d;
    }

    public String getData() {
        return data;
    }

    public void setExtra(String e) {
        extra = e;
    }

    public String getExtra() {
        return extra;
    }

    // for test
    // public void display() {
    // if (type == Constant.LOC_TYPE_ROOT){
    // Log.i(TAG, "type is ROOT");
    // Log.i(TAG, "data is " + data);
    // }
    //
    // if (type == Constant.LOC_TYPE_FOLDER){
    // Log.i(TAG, "type is FOLDER");
    // Log.i(TAG, "data is " + data);
    // }
    //
    // if (type == Constant.LOC_TYPE_SEARCH_RESULT){
    // Log.i(TAG, "type is SEARCH_RESULT");
    // Log.i(TAG, "data is " + data);
    // Log.i(TAG, "key is " + extra);
    // }
    // }
}
