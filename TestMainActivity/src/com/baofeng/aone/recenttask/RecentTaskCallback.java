package com.baofeng.aone.recenttask;

import com.baofeng.aone.AndroidCallback;

public interface RecentTaskCallback extends AndroidCallback{
    public void onRecentTaskLoadCompleted(String msg);
    public void onRemove();
    public void onRemoveAll();
    public void onLaunchAppCompleted(boolean result, String msg);
}
