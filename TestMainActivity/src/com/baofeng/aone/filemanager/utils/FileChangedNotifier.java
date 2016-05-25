package com.baofeng.aone.filemanager.utils;

import java.io.File;
import java.util.Vector;

import android.content.Context;

/**
 * 
 * @author alexdoer.yu
 * 
 *         [summary] Below classes are used to collect the information of file
 *         change, including add, delete and update, during the file operation.
 *         The observer pattern and the singleton pattern are used in them.
 * 
 *         [introduction] FileChangedInfo: data model to record file
 *         modification information FileChangedObserver: the interface to define
 *         the abstract observer for all components which we need notify.
 *         MediaObserver: the observer class for media, use singleton pattern
 *         LeReaderObserver: the observer class for lereader, use singleton
 *         pattern FileChangedNotifier: the subject class in the observer
 *         pattern
 * 
 *         [note]: > Some public APIs of the classes MediaObserver and
 *         LeReaderObserver can been used Independently in your code > Do not
 *         remove the commented code, which maybe used in future, and please
 *         refer the comment when you want to use commented code. > Nowadays,
 *         use FileChangedNotifier as a static tool class, no need to create an
 *         object when you use it just. Maybe change it to a common subject
 *         class in the observer pattern
 * 
 */

class FileChangedInfo {
    private static final String TAG = "FileChangedInfo";

    public static final int INVALID_VALUE = -1;
    public static final int OP_TYPE_ADD = 1;
    public static final int OP_TYPE_DELETE = 2;
    public static final int OP_TYPE_UPDATE = 3;

    private File mFile = null;
    private int miOpType = INVALID_VALUE;

    public FileChangedInfo(File file, int opType) {
        if (null == file) {
            Log.e(TAG, "file in null in FileChangedInfo");
            return;
        }

        if (opType != OP_TYPE_ADD && opType != OP_TYPE_DELETE
                && opType != OP_TYPE_UPDATE) {
            Log.e(TAG, "invalid op type in FileChangedInfo");
            return;
        }

        mFile = file;
        miOpType = opType;
    }

    public File getFile() {
        return mFile;
    }

    public int getOpType() {
        return miOpType;
    }
}

interface FileChangedObserver {
    public void onOpStart(Context context);

    public void onOpEnd(Context context);

    public void onFileChanged(Context context, File file, int opType);
}

public class FileChangedNotifier {
    private static final String TAG = "FileChangedObservable";

    private static Vector<FileChangedObserver> sObservers = null;

    public static final int OP_TYPE_ADD = 1;
    public static final int OP_TYPE_DELETE = 2;
    public static final int OP_TYPE_UPDATE = 3;

    private FileChangedNotifier() {
    }

    private static void initialize(Context context) {
        if (null == sObservers) {
            sObservers = new Vector<FileChangedObserver>();

            if (null != MediaObserver.getInstance(context))
                sObservers.addElement(MediaObserver.getInstance(context));
            else
                Log.e(TAG, "can not get the instance of MediaObserver");

            if (null != LeReaderObserver.getInstance(context))
                sObservers.addElement(LeReaderObserver.getInstance(context));
            else
                Log.e(TAG, "can not get the instance of LeReaderObserver");
        }
    }

    // maybe use them in future
    // public void addObserver(FileChangedObserver fco){
    // if (null == fco)
    // return;
    // else{
    // if (!sObservers.contains(fco))
    // sObservers.addElement(fco);
    // }
    // }
    //
    // public void deleteObserver(FileChangedObserver fco) {
    // if (null == fco)
    // return;
    // else{
    // sObservers.removeElement(fco);
    // }
    // }
    //
    // public void deleteObservers(){
    // sObservers.removeAllElements();
    // }

    public static void notifyOpStart(Context context) {
        if (null == context) {
            Log.e(TAG, "context is null in notifyOpStart");
            return;
        }

        if (null == sObservers)
            initialize(context);

        if (null != sObservers && !sObservers.isEmpty()) {
            for (FileChangedObserver fco : sObservers) {
                fco.onOpStart(context);
            }
        }
    }

    public static void notifyOpEnd(Context context) {
        if (null == context) {
            Log.e(TAG, "context is null in notifyOpEnd");
            return;
        }

        if (null == sObservers)
            initialize(context);

        if (null != sObservers && !sObservers.isEmpty()) {
            for (FileChangedObserver fco : sObservers) {
                fco.onOpEnd(context);
            }
        }
    }

    public static void notifyFileChanged(Context context, File file, int opType) {
        if (null == context
                || null == file
                || (opType != FileChangedInfo.OP_TYPE_ADD
                        && opType != FileChangedInfo.OP_TYPE_DELETE && opType != FileChangedInfo.OP_TYPE_UPDATE)) {
            Log.e(TAG, "invalid parameter in notifyFileChanged");
            return;
        }

        if (null == sObservers)
            initialize(context);

        if (null != sObservers && !sObservers.isEmpty()) {
            for (FileChangedObserver fco : sObservers) {
                fco.onFileChanged(context, file, opType);
            }
        }
    }
}