package com.baofeng.aone.shutdown;

import android.content.Intent;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;

public class ShutdownManager extends AndroidManager{

    private static ShutdownManager mManager;
    private boolean mShowShutdownDialog = true;

    public static AndroidManager getAndroidManager(){
        return getInstance();
    }

    private static ShutdownManager getInstance(){
        if(mManager == null) {
            mManager = new ShutdownManager();
        }
        return mManager;
    }

    public void shutdown() {
    	String ACTION_REQUEST_SHUTDOWN = (String)ReflectUtil.getField(Intent.class, "ACTION_REQUEST_SHUTDOWN");
    	String EXTRA_KEY_CONFIRM = (String)ReflectUtil.getField(Intent.class, "EXTRA_KEY_CONFIRM");
        Intent shutdown = new Intent(ACTION_REQUEST_SHUTDOWN);
        shutdown.putExtra(EXTRA_KEY_CONFIRM, mShowShutdownDialog);
        shutdown.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        LauncherApplication.getInstance().startActivity(shutdown);
    }
}
