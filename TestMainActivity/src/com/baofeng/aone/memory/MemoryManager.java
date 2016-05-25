package com.baofeng.aone.memory;

import java.lang.reflect.Method;
import java.util.List;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.util.Log;

public class MemoryManager extends AndroidManager{

    public static final String TAG = MemoryManager.class.getSimpleName();
    public static final boolean DEBUG_LOG = true;
    private static MemoryManager mMemManager;
    private boolean mIsCleaning = false;
    private Context mContext;

    private MemoryManager() {
        mContext = LauncherApplication.getInstance().getApplicationContext();
    }

    public static AndroidManager getAndroidManager() {
        return getInstance();
    }

    /**
     * get MemoryManager instance
     * @return MemoryManager
     */
    private static synchronized MemoryManager getInstance() {
        if (mMemManager == null) {
            mMemManager = new MemoryManager();
        }
        return mMemManager;
    }

    public void memoryClear(MemoryCallback memoryCallback){
        if(!mIsCleaning) {
            new memoryClearThread(memoryCallback).start();
        }
    }

    private class memoryClearThread extends Thread {

        MemoryCallback callback;

        public memoryClearThread(MemoryCallback memoryCallback){
            this.callback = memoryCallback;
        }

        @Override
        public void run() {
            super.run();
            mIsCleaning = true;
            startClear(callback);
            mIsCleaning = false;
        }

    }

    private void startClear(MemoryCallback memoryManager) {
        long beforeMem = getAvailMemory();
        long afterMem = beforeMem;
        int clearProcessCount = 0;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = null;

        try {
            method = Class.forName(am.getClass().getName()).getMethod("forceStopPackage", String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<RunningAppProcessInfo> infoList = am.getRunningAppProcesses();

        if (infoList != null) {
            for (int i = 0; i < infoList.size(); ++i) {
                RunningAppProcessInfo appProcessInfo = infoList.get(i);
                if (DEBUG_LOG)
                Log.d(TAG, "process name : " + appProcessInfo.processName + "  importance : " + appProcessInfo.importance);

                if (appProcessInfo.importance > RunningAppProcessInfo.IMPORTANCE_VISIBLE) {
                    //get all package name in this process
                    String[] pkgList = appProcessInfo.pkgList;
                    for (int j = 0; j < pkgList.length; ++j) {
                        try {
                            if (method != null) {
                                method.invoke(am, pkgList[j]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        clearProcessCount++;
                    }
                }
            }
            afterMem = getAvailMemory();
            memoryManager.onMemoryClean(clearProcessCount, (afterMem - beforeMem));
        }
    }

    /**
     * get avail memory 
     * @param context
     * @return long avail memory size
     */
    private long getAvailMemory() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.availMem / (1024 * 1024);
    }

}
