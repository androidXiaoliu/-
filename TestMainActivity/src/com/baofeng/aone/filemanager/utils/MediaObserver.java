package com.baofeng.aone.filemanager.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baofeng.aone.filemanager.bean.StorageManager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class MediaObserver implements FileChangedObserver {
    private static final String TAG = "MediaObserver";

    private static MediaObserver sInstance = null;
    private static List<FileChangedInfo> sfciList = null;
    private static StorageManager ssManager = null;
    private static Map<File, File> smapRoots = null;

    private MediaObserver(Context context) {
        if (null == ssManager)
            ssManager = StorageManager.getInstance(context);

        if (null == sfciList)
            sfciList = new ArrayList<FileChangedInfo>();

        if (null == smapRoots)
            smapRoots = new HashMap<File, File>();
    }

    public static MediaObserver getInstance(Context context) {
        if (null == context) {
            Log.e(TAG, "context is null in getInstance");
            return null;
        }

        if (null == sInstance)
            sInstance = new MediaObserver(context);

        if (null == sInstance) {
            Log.e(TAG, "create MediaObserver object failed");
        }

        return sInstance;
    }

    // implement the interface FileChangedObserver
    public void onOpStart(Context context) {
        if (null == sfciList)
            sfciList = new ArrayList<FileChangedInfo>();
        else
            sfciList.clear();

        if (null == smapRoots)
            smapRoots = new HashMap<File, File>();
        else
            smapRoots.clear();
    }

    public void onFileChanged(Context context, File file, int opType) {
        if (null == context
                || null == file
                || (opType != FileChangedInfo.OP_TYPE_ADD
                        && opType != FileChangedInfo.OP_TYPE_DELETE && opType != FileChangedInfo.OP_TYPE_UPDATE)) {
            Log.e(TAG, "invalid parameter in onFileChanged");
            return;
        }

        if (needNotifyMedia(context, file)) {
            sfciList.add(new FileChangedInfo(file, opType));
            updateChangedStorages(file);
        }
    }

    public void onOpEnd(Context context) {
        if (null == sfciList || 0 == sfciList.size())
            return;
        else {
            if (1 == sfciList.size()) {
                File file = sfciList.get(0).getFile();
                notifyMediaScanFile(context, file);
            } else {
                int size = smapRoots.size();
                if (0 == size) {
                    Log.e(TAG, "get changed storage failed in onOpEnd");
                    return;
                } else {
                    File[] roots = new File[size];
                    smapRoots.keySet().toArray(roots);

                    for (File root : roots) {
                        notifyMediaScanStorage(context, root);
                    }
                }
            }
        }
    }

    private void updateChangedStorages(File file) {
        File root = ssManager.getStorageRootByFile(file);
        if (null != smapRoots && !smapRoots.containsKey(root)) {
            smapRoots.put(root, root);
        }
    }

    // useful tool APIs
    public static boolean needNotifyMedia(Context context, File file) {
        if (null == file) {
            Log.e(TAG, "invalid parameter in needNotifyMedia");
            return false;
        }

        String type = FileUtil.getFileTypeString(context, file);
        if (type != null) {
            if (type.equals(ResUtils.picturefile)
                    || type.equals(ResUtils.audiofile)
                    || type.equals(ResUtils.videofile)) {
                return true;
            }
        }

        /*
         * if (type != null) { if (type.startsWith("image") ||
         * type.startsWith("audio") || type.startsWith("video")) { return true;
         * } }
         */

        return false;
    }

    public static void notifyMediaScanFile(Context context, File file) {
        if (null == context || null == file) {
            Log.e(TAG, "invalid parameter in notifyMediaScanFile");
        } else {
            Log.i(TAG,
                    "[notify media] scan the file: " + file.getAbsolutePath());
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

    public static void notifyMediaScanFileIfNeed(Context context, File file) {
        if (needNotifyMedia(context, file))
            notifyMediaScanFile(context, file);
    }

    public static void notifyMediaScanStorage(Context context, File file) {
        if (null == context || null == file) {
            Log.e(TAG, "invalid parameter in notifyMediaScanFile");
        } else {
            if (null == ssManager)
                ssManager = StorageManager.getInstance(context);

            if (null != ssManager) {
                File root = ssManager.getStorageRootByFile(file);
                // Log.i(TAG, "[notify media] scan the storage: " +
                // root.getAbsolutePath());
                // context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                // Uri.fromFile(root)));
            } else {
                Log.e(TAG, "ssManager is null in notifyMediaScanStorage");
            }
        }
    }
}