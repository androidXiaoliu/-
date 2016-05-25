package com.baofeng.aone.filemanager.utils;

public final class Log {
    private static final String TAG_PREFIX = "GE_FB_";

    private static final boolean isDebug = true;

    public static int v(String tag, String msg) {
        return isDebug ? android.util.Log.v(TAG_PREFIX + tag, msg) : -1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        return isDebug ? android.util.Log.v(TAG_PREFIX + tag, msg, tr) : -1;
    }

    public static int d(String tag, String msg) {
        return isDebug ? android.util.Log.d(TAG_PREFIX + tag, msg) : -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        return isDebug ? android.util.Log.d(TAG_PREFIX + tag, msg, tr) : -1;
    }

    public static int i(String tag, String msg) {
        return isDebug ? android.util.Log.i(TAG_PREFIX + tag, msg) : -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        return isDebug ? android.util.Log.i(TAG_PREFIX + tag, msg, tr) : -1;
    }

    public static int w(String tag, String msg) {
        return isDebug ? android.util.Log.w(TAG_PREFIX + tag, msg) : -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        return isDebug ? android.util.Log.w(TAG_PREFIX + tag, msg, tr) : -1;
    }

    public static int w(String tag, Throwable tr) {
        return isDebug ? android.util.Log.w(TAG_PREFIX + tag, tr) : -1;
    }

    public static int e(String tag, String msg) {
        return isDebug ? android.util.Log.e(TAG_PREFIX + tag, msg) : -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        return isDebug ? android.util.Log.e(TAG_PREFIX + tag, msg, tr) : -1;
    }
}