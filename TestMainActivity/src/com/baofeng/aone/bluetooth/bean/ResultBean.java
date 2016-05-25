package com.baofeng.aone.bluetooth.bean;

import java.util.ArrayList;

public class ResultBean<T> {
    private boolean hasDatas;
    private ArrayList<T> list;

    public boolean isHasDatas() {
        return hasDatas;
    }

    public void setHasDatas(boolean hasDatas) {
        this.hasDatas = hasDatas;
    }

    public ArrayList<T> getList() {
        return list;
    }

    public void setList(ArrayList<T> list) {
        this.list = list;
    }

}
