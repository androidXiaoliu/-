package com.baofeng.aone.filemanager.volume;

import java.io.File;

import com.baofeng.aone.filemanager.utils.Log;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

public class Storage {
    private static final String TAG = "Storage";

    public static final int STORAGE_TYPE_LOCAL = 0;
    public static final int STORAGE_TYPE_EXTERNAL = 1;
    public static final int STORAGE_TYPE_EXTERNAL_USB = 2;

    public static final int INVALID_INT_VALUE = -1;

    private String msName = null;
    private int miNameRID = 0;

    private int miType = STORAGE_TYPE_LOCAL;
    private File mfRoot = null;
    private String msState = null;

    public Storage(File file, int type) {
        this(file, type, null, null);
    }

    public Storage(Context context, File file, int type, String state) {
        this(file, type, null, state);
    }

    public Storage(File file, int type, String name, String state) {
        if (null == file || !file.isDirectory()) {
            Log.e(TAG, "file is invalid in Storage()");
            return;
        } else {
            if (type != STORAGE_TYPE_LOCAL && type != STORAGE_TYPE_EXTERNAL
                    && type != STORAGE_TYPE_EXTERNAL_USB) {
                Log.e(TAG, "type is invalid in Storage()");
            } else {
                mfRoot = new File(file.getAbsolutePath());
                if (null == name || 0 == name.length())
                    msName = mfRoot.getName();
                else
                    msName = name;
                miType = type;
                if (state != null)
                    msState = state;
                else
                    updateState();
            }
        }
    }

    public int getType() {
        return miType;
    }

    public void setType(int type) {
        miType = type;
    }

    public String getName() {
        return msName;
    }

    public int getNameRID() {
        return miNameRID;
    }

    public void setName(String name) {
        if (null == name || 0 == name.length()) {
            Log.e(TAG, "invalid name in setName");
            return;
        } else {
            msName = name;
        }
    }

    public void setNameRID(int rid) {
        if (0 >= rid) {
            Log.e(TAG, "invalid rid in setNameRID");
        } else {
            miNameRID = rid;
        }
    }

    public File getRoot() {
        return mfRoot;
    }

    public long getTotalSize() {
        if (null == mfRoot || !mfRoot.exists()) {
            Log.e(TAG, "mfRoot is invalid in getTotalSize");
            return INVALID_INT_VALUE;
        } else {
            StatFs mState = null;
            long size = INVALID_INT_VALUE;

            try {
                mState = new StatFs(mfRoot.getAbsolutePath());
                if (null != mState)
                    size = ((long) mState.getBlockSize())
                            * ((long) mState.getBlockCount());
            } catch (Exception e) {
                Log.e(TAG, "can not get storage status");
            }

            return size;
        }
    }

    public long getFreeSize() {
        if (null == mfRoot || !mfRoot.exists()) {
            Log.e(TAG, "mfRoot is invalid in getTotalSize");
            return INVALID_INT_VALUE;
        } else {
            StatFs mState = null;
            long size = INVALID_INT_VALUE;

            try {
                mState = new StatFs(mfRoot.getAbsolutePath());
                if (null != mState)
                    Log.i("ssssssss",
                            "--->block num"
                                    + String.valueOf(((long) mState
                                            .getAvailableBlocks())));
                Log.i("ssssssss",
                        "--->block size"
                                + String.valueOf(((long) mState.getBlockSize())));
                return ((long) mState.getAvailableBlocks())
                        * ((long) mState.getBlockSize());
            } catch (Exception e) {
                Log.e(TAG, "can not get storage status");
            }

            return size;
        }
    }

    public String getState(boolean needUpdate) {
        if (needUpdate)
            updateState();

        return msState;
    }

    public void setState(String newState) {
        msState = newState;
    }

    public boolean isValid() {
        if (Environment.MEDIA_MOUNTED.equals(msState)
                || Environment.MEDIA_MOUNTED_READ_ONLY.equals(msState)) {
            return true;
        } else
            return false;
    }

    public boolean updateState() {
        String newState = Environment.getExternalStorageState();
        if (null == msState) {
            if (null != newState) {
                msState = newState;
                return true;
            } else
                return false;
        } else {
            if (null == newState) {
                Log.e(TAG, "the new State is null, so not to change");
                return false;
            } else {
                if (msState.equals(newState)) {
                    return false;
                } else {
                    msState = newState;
                    Log.i(TAG,
                            "the state of storage [ "
                                    + mfRoot.getAbsolutePath() + " ] "
                                    + "is changed to : " + newState);
                    return true;
                }
            }
        }
    }

    public void dump() {
        Log.i(TAG, "========== Storage Data ========== \n");
        Log.i(TAG, "the type is " + String.valueOf(miType));
        Log.i(TAG, "the root is " + String.valueOf(mfRoot.getAbsolutePath()));
        Log.i(TAG, "the state is " + msState);
        Log.i(TAG, "================================== \n");
    }
}
