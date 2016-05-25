package com.baofeng.aone.filemanager.utils;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

public class FileThread extends Thread {
    private static final String TAG = "FileThread";

    // [improve] use enum
    public static final int STATUS_NONE = 0;
    public static final int STATUS_INIT = 1;
    public static final int STATUS_READY = 2;
    public static final int STATUS_RUNNING = 3;
    public static final int STATUS_PAUSE = 4;
    public static final int STATUS_RESUME = 5;
    public static final int STATUS_STOP = 6;
    public static final int STATUS_FINISH = 7;

    protected int miStatus = STATUS_NONE;

    private Handler mHandler = null;
    protected PowerManager.WakeLock mWakeLock = null;
    protected Context mContext = null;

    public FileThread(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        init(mContext);

    }

    public FileThread(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        if (null != context) {
            PowerManager powerManager = (PowerManager) mContext
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK
                            | PowerManager.ON_AFTER_RELEASE, TAG);

            miStatus = STATUS_INIT;
        } else {
            Log.e(TAG, "the context is null in initThead");
            return;
        }
    }

    public boolean stopIt() {
        if (STATUS_RUNNING == miStatus) {
            miStatus = STATUS_STOP;
            return true;
        } else
            return false;
    }

    public boolean isStop() {
        return STATUS_STOP == miStatus ? true : false;
    }

    public boolean pauseIt() {
        if (STATUS_RUNNING == miStatus) {
            miStatus = STATUS_PAUSE;
            return true;
        } else
            return false;
    }

    private void pauseInternal() {
        miStatus = STATUS_PAUSE;
        while (miStatus == STATUS_PAUSE) {
            try {
                join(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    protected void pauseNow() {
        if (STATUS_PAUSE == miStatus) {
            while (STATUS_PAUSE == miStatus) {
                try {
                    join(100);
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if (STATUS_RESUME == miStatus) {
                Log.i(TAG, "the thread resume now");
                miStatus = STATUS_RUNNING;
            } else {
                Log.i(TAG, "the thread status is " + String.valueOf(miStatus));
            }
        }
    }

    public boolean resumeIt() {
        if (STATUS_PAUSE == miStatus) {
            miStatus = STATUS_RESUME;
            return true;
        } else
            return false;
    }

    public boolean isPause() {
        return STATUS_PAUSE == miStatus ? true : false;
    }

    public int getStatus() {
        return miStatus;
    }

    public void run() {
        preRun();
        realRun();
        postRun();
    }

    public void preRun() {
        Log.i(TAG, "==> preRun");

        if (null != mWakeLock) {
            mWakeLock.acquire();
            miStatus = STATUS_READY;
        } else {
            Log.e(TAG, "the mWakLock is null in preRun");
        }
    }

    /*
     * [note]: please override this function, not run()
     */
    protected void realRun() {
        miStatus = STATUS_RUNNING;
    }

    public void postRun() {
        Log.i(TAG, "==> postRun");

        if (null != mWakeLock) {
            mWakeLock.release();
            miStatus = STATUS_FINISH;
        } else {
            Log.e(TAG, "the mWakLock is null in postRun");
        }
    }

    public void sendMsg(int msgId) {
        sendMsg(msgId, null);
    }

    public void sendMsg(int msgId, Bundle bundle) {
        if (null != mHandler) {
            Message msg = mHandler.obtainMessage(msgId, 0, 0, null);
            if (null != bundle)
                msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            Log.e(TAG, "the mHandler is null in sendMsg");
        }
    }

    public void sendMsgThenPause(int msgId, Bundle bundle) {
        sendMsg(msgId, bundle);
        pauseIt();
    }

    public void sendMsgThenStop(int msgId, Bundle bundle) {
        sendMsg(msgId, bundle);
        stopIt();
    }

}
