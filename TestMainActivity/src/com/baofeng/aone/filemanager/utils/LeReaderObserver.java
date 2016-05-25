package com.baofeng.aone.filemanager.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baofeng.aone.filemanager.bean.StorageManager;

import android.content.Context;
import android.content.Intent;

public class LeReaderObserver implements FileChangedObserver {
    private static final String TAG = "LeReaderObserver";

    private static LeReaderObserver sInstance = null;
    private static List<FileChangedInfo> sfciList = null;
    private static Map<File, File> smapDirs = null;
    private static StorageManager ssManager = null;

    private LeReaderObserver(Context context) {
        if (null == ssManager)
            ssManager = StorageManager.getInstance(context);

        if (null == sfciList)
            sfciList = new ArrayList<FileChangedInfo>();

        if (null == smapDirs)
            smapDirs = new HashMap<File, File>();
    }

    public static LeReaderObserver getInstance(Context context) {
        if (null == context) {
            Log.e(TAG, "context is null in getInstance");
            return null;
        }

        if (null == sInstance)
            sInstance = new LeReaderObserver(context);

        if (null == sInstance) {
            Log.e(TAG, "create LeReaderObserver object failed");
        }

        return sInstance;
    }

    // implement the interface FileChangedObserver
    public void onOpStart(Context context) {
        if (null == sfciList)
            sfciList = new ArrayList<FileChangedInfo>();
        else
            sfciList.clear();

        if (null == smapDirs)
            smapDirs = new HashMap<File, File>();
        else
            smapDirs.clear();
    }

    public void onFileChanged(Context context, File file, int opType) {
        if (null == context
                || null == file
                || (opType != FileChangedInfo.OP_TYPE_ADD
                        && opType != FileChangedInfo.OP_TYPE_DELETE && opType != FileChangedInfo.OP_TYPE_UPDATE)) {
            Log.e(TAG, "invalid parameter in onFileChanged");
            return;
        }

        notifyLeReaderScanFileIfNeed(context, file);

        // below code is used to work out the common parent directory for all
        // changed files.
        // if (needNotifyLeReader(context, file)){
        // sfciList.add(new FileChangedInfo(file, opType));
        //
        // // comment below code, just send message for file change one by one
        // updateChangedDirs(file);
        // if (1 == sfciList.size()){
        // notifyLeReaderScanStart(context);
        // }
        //
        // }
    }

    public void onOpEnd(Context context) {
        if (null == sfciList || 0 == sfciList.size())
            return;
        else {

            // below code is used to notify lereader the content of some
            // directories had been changed
            // if (1 == sfciList.size()){
            // File file = sfciList.get(0).getFile();
            //
            // notifyLeReaderScanFile(context, file);
            // }else{
            // int size = smapDirs.size();
            // if (0 == size){
            // Log.e(TAG, "get changed storage failed in onOpEnd");
            // return;
            // }else{
            // File[] dirs = new File[size];
            // smapDirs.keySet().toArray(dirs);
            //
            // for(File dir : dirs){
            // notifyLeReaderScanDir(context, dir);
            // }
            // }
            // }
        }
    }

    // get the common parent directory for changed files
    // private void updateChangedDirs(File file) {
    // File root = ssManager.getStorageRootByFile(file);
    //
    // File dir = smapDirs.get(root);
    //
    // if (null == dir){
    // smapDirs.put(root, file.getParentFile());
    // }else{
    // if (dir.equals(root)){
    // return;
    // }else {
    // File parent = file.getParentFile();
    // if (parent.equals(root)){
    // smapDirs.put(root, root);
    // }else{
    // File temp = dir;
    // String newPath = parent.getAbsolutePath();
    //
    // while (!newPath.startsWith(temp.getAbsolutePath())){
    // temp = temp.getParentFile();
    // }
    //
    // if (!temp.equals(dir)){
    // smapDirs.put(root, temp);
    // }
    // }
    // }
    // }
    // }

    // public tool APIs
    public static boolean needNotifyLeReader(Context context, File file) {
        if (null == file) {
            Log.e(TAG, "invalid parameter in needNotifyLeReader");
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

        return false;
    }

    public static void notifyLeReaderScanStart(Context context) {
        Log.i(TAG, "==> notifyLeReaderScanStart");

        if (null == context) {
            Log.e(TAG, "context is null in notifyLeReaderScanStart");
        }

        Intent intent = new Intent();
        Log.i(TAG, "[notify lereader] scan start");
        intent.setAction("com.lenovo.gemini.ebook.intent.EBOOK_SCANNER_SCAN_STARTED");
        context.sendBroadcast(intent);
    }

    public static void notifyLeReaderScanFinish(Context context) {
        Log.i(TAG, "==> notifyLeReaderScanFinish");

        if (null == context) {
            Log.e(TAG, "context is null in notifyLeReaderScanFinish");
        }

        Intent intent = new Intent();
        Log.i(TAG, "[notify lereader] scan finish");
        intent.setAction("com.lenovo.gemini.ebook.intent.EBOOK_SCANNER_SCAN_FINISHED");
        context.sendBroadcast(intent);
    }

    public static void notifyLeReaderScanDir(Context context, File file) {
        if (null == context || null == file)
            return;

        Intent intent = new Intent();
        intent.putExtra("path", file.getAbsolutePath());
        intent.setAction("com.lenovo.gemini.ebook.intent.EBOOK_SCANNER_SCAN_DIRECTORY");

        if (file.exists()) {
            Log.i(TAG, "[notify lereader] add dir: " + file.getAbsolutePath());
            intent.putExtra("operate", 1);
        } else {
            Log.i(TAG,
                    "[notify lereader] delete dir: " + file.getAbsolutePath());
            intent.putExtra("operate", 2);
        }

        context.sendBroadcast(intent);
    }

    public static void notifyLeReaderScanFile(Context context, File file) {
        if (null == context || null == file)
            return;

        Intent intent = new Intent();
        intent.putExtra("path", file.getAbsolutePath());
        intent.setAction("com.lenovo.gemini.ebook.intent.EBOOK_SCANNER_SCAN_FILE");

        if (file.exists()) {
            Log.i(TAG, "[notify lereader] add file: " + file.getAbsolutePath());
            intent.putExtra("operate", 1);
        } else {
            Log.i(TAG,
                    "[notify lereader] delete file: " + file.getAbsolutePath());
            intent.putExtra("operate", 2);
        }

        context.sendBroadcast(intent);
    }

    public static void notifyLeReaderScanFileIfNeed(Context context, File file) {
        if (needNotifyLeReader(context, file)) {
            notifyLeReaderScanFile(context, file);
        }
    }
}
