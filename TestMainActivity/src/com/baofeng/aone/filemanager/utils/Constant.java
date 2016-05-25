package com.baofeng.aone.filemanager.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

// [improve]
public class Constant {
    private static final String TAG = "Constant";

    public final static int CALL_FILEMAN = 0;
    public final static int CALL_EDIT = 1;
    public final static int CALL_SEARCH = 2;

    // menu operations
    public final static int _ATTRIDISPLAY = 0;
    public final static int _COPY = 1;
    public final static int _CUT = 2;
    public final static int _DELETE = 3;
    public final static int _RENAME = 4;
    public final static int _COMPRESS = 5;
    public final static int _DECOMPRESS = 6;
    public final static int _SEARCH = 7;
    public final static int _CREATEDIR = 8;
    public final static int _CREATEFILE = 9;
    public final static int _DONE = 10;

    // activity State
    public final static int _LIST = 11;
    public final static int _EDIT = 12;
    public final static int _OPEAETION = 13;

    // source from
    public final static int FROM_PATH = 13;
    public final static int FROM_CLIP = 14;

    // for edit activity
    public final static int FROM_BROWSER = 0;
    public final static int FROM_SEARCH = 1;

    // thumbnail width and height;
    public final static int THUMBNAIL_WIDTH = 60;
    public final static int THUMBNAIL_HEIGHT = 60;

    // constant
    public final static int FILE_NAME_NUM_MAX = 85;
    public final static int LOC_STRING_LENGTH_MAX = 21;
    public final static String ZIPENDSTRING = ".zip";

    // history
    public static final int LOC_TYPE_ROOT = 0;
    public static final int LOC_TYPE_FOLDER = 1;
    public static final int LOC_TYPE_SEARCH_RESULT = 2;

    public static final String LOC_ROOT_DATA_ES = "es";

    // simulation disk
    public final static String root = "/";
    public final static String invalidDir = "LOST.DIR";

    public static final String LEZONE_INTENT = "com.lenovo.leos.intent.LEZONE_UPDATE_FINISHED";

    // root
    public static final String usbRoot = "/data/usb/sda1";// add by su
    public static final String LOCAL_STORAGE_PATH = "/data";
    public static String sdroot = "/mnt/sdcard";
    public static final File[] realRoots = { new File(sdroot),
            new File("/mnt/sdcard1") };

    public static final File GPS_MAP_DIR = new File(sdroot + "/gpsmap");
    public static final File SYSDIR_ROOT = new File(sdroot + "/LeZone Library");
    public final static String SYSDIR[] = { "Music", "Videos", "Photos",
            "E-books", "Office", "Copyback" };

    public static String SYSDIR_CN[] = null;

    public static String textConvert(Context context, String name) {
        return name;
    }

    public static boolean isRealRoot(File file) {
        for (File f : realRoots)
            if (file.equals(f))
                return true;
        return false;
    }

    public static boolean isSysFile(final File file) {
        if (null == file) {
            Log.e(TAG, "file is invalid in isSysFile");
            return false;
        } else {
            if (file.equals(SYSDIR_ROOT)
                    || file.getAbsolutePath().startsWith(
                            SYSDIR_ROOT.getAbsolutePath() + "/"))
                return true;
            else
                return false;
        }
    }

    public static boolean isSysOrReservedDir(final File file) {
        if (null == file || !file.isDirectory()) {
            Log.e(TAG, "file is null or not a dir in isSysOrReservedDir");
            return false;
        } else {
            if (!isSysFile(file))
                return false;
            else {
                if (file.equals(SYSDIR_ROOT))
                    return true;
                else {
                    if (!file.getParentFile().equals(SYSDIR_ROOT))
                        return false;
                    else {
                        for (String str : SYSDIR) {
                            if (file.getName().equals(str))
                                return true;
                        }

                        return false;
                    }
                }
            }
        }
    }

    public static boolean isForUser(File file) {
        if (null == file || !file.exists())
            return false;
        else {
            // [improve] check whether it is in real root.
            return true;
        }
    }

    private static File localRoot = new File("/mnt/sdcard/local");

    public static File getLocalStorageDirectory() {
        File file = Environment.getExternalStorageDirectory();
        if (null == file || !file.exists() || !file.isDirectory()) {
            Log.e(TAG, "can not get local storage root");
            return null;
        } else
            return file;
    }

    private static File externalRoot = null;

    public static File getExternalStorageDirectory() {
        // File file = Environment.getExtendedStorageDirectory();
        File file = externalRoot;
        System.out.println("getExternalStorageDirectory" + Constant.sdroot
                + "--------------------------------------");
        if (null == file || !file.exists() || !file.isDirectory()) {
            Log.e(TAG, "can not get external storage root");
            return null;
        } else
            return file;
    }

}
