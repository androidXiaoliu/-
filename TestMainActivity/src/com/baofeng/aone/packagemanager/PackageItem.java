package com.baofeng.aone.packagemanager;

public class PackageItem {

    private String mPackageName;
    private String mAppName;
    private String mActivityname;
    private int mAppFlag;

    // private IconData mData;

    public PackageItem(String mPackageName, String mAppName, byte[] mIcon,
            int mAppFlag) {
        this.mPackageName = mPackageName;
        this.mAppName = mAppName;
        this.mAppFlag = mAppFlag;
    }

    public PackageItem(String mPackageName, String mAppName,
            String activityName, /* IconData data, */int mAppFlag) {
        this.mPackageName = mPackageName;
        this.mAppName = mAppName;
        this.mActivityname = activityName;
        // this.mData = data;
        this.mAppFlag = mAppFlag;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public void setPackageName(String mPackageName) {
        this.mPackageName = mPackageName;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String mAppName) {
        this.mAppName = mAppName;
    }

    public int getAppFlag() {
        return mAppFlag;
    }

    public void setAppFlag(int mAppFlag) {
        this.mAppFlag = mAppFlag;
    }

    public String getActivityname() {
        return mActivityname;
    }

    public void setActivityname(String mActivityname) {
        this.mActivityname = mActivityname;
    }

    // public IconData getData() {
    // return mData;
    // }
    //
    // public void setData(IconData mData) {
    // this.mData = mData;
    // }

}
