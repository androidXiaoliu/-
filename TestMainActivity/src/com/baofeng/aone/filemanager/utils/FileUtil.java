package com.baofeng.aone.filemanager.utils;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.baofeng.aone.filemanager.bean.StorageManager;
import com.baofeng.aone.filemanager.filebrowser.FileManagerCallback;

public class FileUtil {
    private static final String TAG = "FileUtils";

    // for lereader
    // private static final String EBOOK_DIR = "/mnt/sdcard/ebook";
    private static final String INTENT_KEY_VIDEO_TITLE = "video_title";

    private static String ssLocalStoragePath = null;
    private static String ssExternalStoragePath = null;

    private static Context mContext;

    private static final FileFilter sffHidden = new FileFilter() {
        public boolean accept(File pathname) {
            // TODO Auto-generated method stub
            if (pathname.isHidden())
                return false;
            else
                return true;
        }
    };

    /**
     * Whether the URI is a local one.
     * 
     * @param uri
     * @return
     */
    public static boolean isLocal(String uri) {
        if (uri != null && !uri.startsWith("http://")) {
            return true;
        }
        return false;
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     * 
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     *         null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null || 0 == uri.length()) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension
            return "";
        }
    }

    // add by PH 091118
    /**
     * Gets the file name without the Extension of a file name.
     * 
     * @param filename
     * @return The file name without the Extension including the dot(".");
     *         Filename if there is no extension; null if filename was null.
     */
    public static String getFileNameWithoutExt(String filename) {
        if (filename == null) {
            return null;
        }

        int dot = filename.lastIndexOf(".");
        if (dot >= 0) {
            return filename.substring(0, dot);
        } else {
            // No extension.
            return filename;
        }
    }

    // end PH 091118

    /**
     * Returns true if uri is a media uri.
     * 
     * @param uri
     * @return
     */
    public static boolean isMediaUri(String uri) {
        if (uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Convert File into Uri.
     * 
     * @param file
     * @return uri
     */
    public static Uri getUri(File file) {
        return Uri.parse("file://" + file.getAbsolutePath());
    }

    /**
     * Convert Uri into File.
     * 
     * @param uri
     * @return file
     */
    public static File getFile(Uri uri) {
        if (uri != null) {
            String filepath = uri.toString();
            if (filepath.startsWith("file://")) {
                filepath = filepath.substring(7);
            }
            return new File(filepath);
        }
        return null;
    }

    /**
     * Returns the path only (without file name).
     * 
     * @param file
     * @return
     */
    public static File getPathWithoutFilename(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                // no file to be split off. Return everything
                return file;
            } else {
                String filename = file.getName();
                String filepath = file.getAbsolutePath();

                // Construct path without file name.
                String pathwithoutname = filepath.substring(0,
                        filepath.length() - filename.length());
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0,
                            pathwithoutname.length() - 1);
                }
                return new File(pathwithoutname);
            }
        }
        return null;
    }

    /**
     * Constructs a file from a path and file name.
     * 
     * @param curdir
     * @param file
     * @return
     */
    public static File getFile(String curdir, String file) {
        String separator = "/";
        if (curdir.endsWith("/")) {
            separator = "";
        }
        File clickedFile = new File(curdir + separator + file);
        return clickedFile;
    }

    public static File getFile(File curdir, String file) {
        return getFile(curdir.getAbsolutePath(), file);
    }

    public static boolean isInvalidDir(File dir) {
        return dir.getName().toUpperCase().equals(Constant.invalidDir);
    }

    private static String getLocalStoragePath() {
        if (null == ssLocalStoragePath) {
            File local = Constant.getLocalStorageDirectory();
            if (null == local) {
                Log.i(TAG, "can not get local storage in getLocalStoragePath");
                return null;
            } else {
                String root = local.getAbsolutePath();
                if (!root.endsWith("/"))
                    root = root + "/";

                ssLocalStoragePath = root;
                return ssLocalStoragePath;
            }
        } else
            return ssLocalStoragePath;
    }

    private static String getExternalStoragePath() {
        if (null == ssLocalStoragePath) {
            File external = Constant.getExternalStorageDirectory();
            if (null == external) {
                Log.i(TAG, "can not get local storage in getLocalStoragePath");
                return null;
            } else {
                String root = external.getAbsolutePath();
                if (!root.endsWith("/"))
                    root = root + "/";

                ssExternalStoragePath = root;
                return ssExternalStoragePath;
            }
        } else
            return ssExternalStoragePath;
    }

    public static String getFilePathString(Context context, File file) {
        if (null == file || null == context) {
            Log.i(TAG, "file is null in getFilePathString");
            return null;
        } else {
            String path = file.getAbsolutePath();
            if (null == path || 0 == path.length()) {
                Log.e(TAG, "the path is empty in getFilePathString");
                return null;
            } else {
                StorageManager sm = StorageManager.getInstance(context);
                path = path.replaceFirst(sm.getStorageRootByFile(file)
                        .getAbsolutePath(), sm.getStorageNameByFile(file));

                return path;

                // if (path.startsWith(getLocalStoragePath())){
                // return path.replaceFirst(getLocalStoragePath(),
                // context.getString(R.string.menu_localstorage) + "/");
                // }else if (path.startsWith(getExternalStoragePath())){
                // return path.replaceFirst(getExternalStoragePath(),
                // context.getString(R.string.menu_sdcard) + "/");
                // }else
                // return path;
            }
        }
    }

    public static String getFileSizeString(Context context, long lSize) {
        if (null == context || lSize < 0) {
            Log.e(TAG, "invalid parameter in getFileSizeString");
            return null;
        } else {
            return Formatter.formatFileSize(context, lSize);
        }
    }

    public static String getFileSizeString(Context context, File file) {
        if (null == file || !file.exists() || null == context)
            return null;

        return getFileSizeString(context, file.length());
    }

    public static String getFileModifiedTimeString(Context context, long time) {
        if (0 > time)
            return null;
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d HH:mm");
            return sdf.format(time);
        }
    }

    public static String getFileModifiedTimeString(Context context, File file) {
        if (null == file || !file.exists())
            return null;

        return getFileModifiedTimeString(context, file.lastModified());
    }

    public static void openFile(final Context context, final String path,
            FileManagerCallback callback) {
        Log.v(TAG, "==>openFile");
        File aFile = new File(path);
        if (null == aFile || !aFile.exists()) {
            callback.onOpenFileFail(path,
                    ResultUtils.OPERATION_ERRO_FILE_NOT_EXIST);
            return;
        }
        Intent intent = new Intent();
        final String fileName = aFile.getName().toLowerCase();
        Uri data = FileUtil.getUri(aFile);
        Log.v(TAG, "==> FileUri = " + data.toString());

        if (fileName.endsWith(".ics")) {
            intent.setAction("com.android.calendar.import");
            intent.putExtra("Operation", "add");
            intent.putExtra("Path", Uri.fromFile(aFile).toString());
            context.sendBroadcast(intent);
            // add by PH 091022
            /*
             * if (context instanceof FileBrowser) { FileBrowser my =
             * (FileBrowser) context; my.browseDir(new File("/sdcard")); }
             */
            // end PH 091022
            /*
             * } else if (fileName.endsWith(".p12") || fileName.endsWith(".crt")
             * || fileName.endsWith(".cer")) { //
             * Credentials.getInstance().installFromSdCardWithPath(context, //
             * aFile.getPath()); } else if (FileUtil.isCompressFile(aFile)) {
             * decompressFile(context, aFile);
             */
        } else {
            String type = getMimeTypeOfFile(aFile);

            // String type = getFileTypeString(mContext, aFile);

            Log.v(TAG, "the mimetype is " + type);

            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(data, type);
            // if (FileUtil.isVideoFile(context, aFile)) {
            // intent.putExtra(INTENT_KEY_VIDEO_TITLE, aFile.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (Exception ex) {
                Log.v(TAG, "can not open this file" + ex.toString());
                callback.onOpenFileFail(path, ResultUtils.OPEN_ERROR_NO_VALID_TOOLS);

            }
        }

    }

    private static String getMimeTypeOfFile(File file) {
        Log.v(TAG, "==> getMimeTypeOfFile ");
        if (null == file || !file.exists()) {
            Log.e(TAG, "invalid file in getMimeTypeOfFile");
            return null;
        } else {
            if (file.exists() && file.isDirectory()) {
                Log.i(TAG, "can not get mimetype for a folder");
                return null;
            } else
                return getMimeTypeOfFile(file.getName());
        }
    }

    private static String getMimeTypeOfFile(String name) {
        Log.v(TAG, "==> getMimeTypeOfname");
        if (null == name || 0 == name.length()) {
            Log.e(TAG, "name is invalid in getMimeTypeOfFile");
            return null;
        }
        if (!ResUtils.isLoadedTypeMap) {
            ResUtils.init();
            ResUtils.isLoadedTypeMap = true;
        }

        return getMimeType(name.toLowerCase());
    }

    private static String getMimeType(String filename) {
        String extension = FileUtil.getExtension(filename);
        Log.v(TAG, "=========================" + extension);
        String mimetype = ResUtils.typeMap.get(extension);
        // add by xieqm 110124
        if (mimetype == null) {
            if (extension != null)
                mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        extension.toString().toLowerCase());
            else
                mimetype = "fileBrowserNoMimetype";
        }
        Log.v(TAG, "=========================" + mimetype);
        //
        return mimetype;
    }

    private static boolean checkEndsWithInStringArray(String checkItsEnd,
            String[] fileEndings) {
        // Log.d(TAG, "==> checkEndsWithInStringArray");
        for (String aEnd : fileEndings) {
            if (checkItsEnd.toLowerCase().endsWith(aEnd))
                return true;
        }
        return false;
    }

    public static String getFileTypeString(Context context, File file) {
        if (null == file || !file.exists())
            return null;

        if (file.isDirectory()) {
            if (Constant.isSysOrReservedDir(file))
                return ResUtils.filetype_sysfolder;
            else
                return ResUtils.filetype_folder;
        } else if (file.isFile()) {
            String name = file.getName().toLowerCase();

            String strId = ResUtils.unknownfile;
            if (checkEndsWithInStringArray(name, ResUtils.fileEndingImage)) {
                strId = ResUtils.picturefile;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingWebText)) {
                strId = ResUtils.htmlfile;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingPackage)) {
                strId = ResUtils.compressfile;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingAudio)) {
                strId = ResUtils.audiofile;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingVideo)) {
                strId = ResUtils.videofile;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingOffice)) {
                strId = ResUtils.Document;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingAndroid)) {
                strId = ResUtils.android_file;
            } else if (FileUtil.getExtension(name).equals(".txt")) {
                strId = ResUtils.txt_file;
            } else if (checkEndsWithInStringArray(name,
                    ResUtils.fileEndingEbook)) {
                strId = ResUtils.fi_type_ebook;
            } else if (checkEndsWithInStringArray(name, ResUtils.fileEndingCert)) {
                strId = ResUtils.fi_type_cert;
            } else if (FileUtil.getExtension(name).equals(".vcf")) {
                strId = ResUtils.vcf_file;
            } else if (FileUtil.getExtension(name).equals(".csv")) {
                strId = ResUtils.csv_file;
            }

            return strId;
        } else
            return null;

    }

    // [improve]
    public static boolean isNameUseable(String name, boolean isDirectory) {
        // Log.d(TAG, "==> isNameUseable");

        // /\:?<>"|*.'
        // && name.startsWith(" ") == false
        // && ((isDerectory==true)?(name.contains(".") == false):true)
        // && name.contains("'") == false
        // remove above condition, for we can create a folder/file with
        boolean ret = false;
        if (name.length() > 0) {
            if (name.startsWith(".") == false && name.contains("/") == false
                    && name.contains("\\") == false
                    && name.contains(":") == false
                    && name.contains("?") == false
                    && name.contains("\uff1f") == false
                    && name.contains("<") == false
                    && name.contains(">") == false
                    && name.contains("\"") == false
                    && name.contains("\t") == false
                    && name.contains("|") == false
                    && name.contains("*") == false
                    && name.contains("\n") == false
                    && name.contains("\r") == false) {
                ret = true;
            }
        }

        return ret;
    }

    public static String getDefaultFolderName(final File parent, String name) {
        String dir = parent.getAbsolutePath();

        File file = new File(dir + "/" + name);

        int i = 0;
        while (file.exists()) {
            i++;
            file = new File(dir + "/" + name + "(" + String.valueOf(i) + ")");
        }

        return file.getName();

    }

    public static String getDefaultFileName(final File parent, String name,
            String ext) {
        String dir = parent.getAbsolutePath();

        File file = new File(dir + "/" + name + ext);

        int i = 0;
        while (file.exists()) {
            i++;
            file = new File(dir + "/" + name + "(" + String.valueOf(i) + ")"
                    + ext);
        }

        return getFileNameWithoutExt(file.getName());

    }

    public static void newFolder(final Activity activity, final File parent,
            final Handler handler) {

        if (null == activity || null == parent || !parent.isDirectory())
            return;
        final Runnable reOpen = new Runnable() {
            // @Override
            public void run() {
                Log.i(TAG, "==> run");
                // TODO Auto-generated method stub
                FileUtil.newFolder(activity, parent, handler);
            }
        };
    }

    public static void decompressFile(Context context, File file) {
        Log.i(TAG, "==> decompressFile");
        if (null == file || !file.exists() || !FileUtil.isCompressFile(file))
            return;

        String zipfile = file.getAbsolutePath();

        Bundle bundle = new Bundle();
        bundle.putInt("request_code", Constant._DECOMPRESS);

        bundle.putString("toDir", file.getParentFile().getPath());
        bundle.putString("filename", file.getName());

        if (file.getName().toLowerCase().endsWith(".zip")) {
            bundle.putBoolean("ZIP", true);
            zipfile = file.getAbsolutePath();
            bundle.putString("zipfile", zipfile);
        } else {
            bundle.putBoolean("ZIP", false);
        }
        /*
         * Intent intent = new Intent(); intent.setClass(context,
         * FileCompressor.class); intent.putExtras(bundle); ((Activity)
         * context).startActivityForResult(intent, Constant._DECOMPRESS);
         */
    }

    // [improve] maybe add it to FileCompress
    public static boolean isCompressFile(File file) {
        if (null == file || !file.exists()) {
            Log.e(TAG, "the file is invalid in isCompressFile");
            return false;
        } else {
            String name = file.getName().toLowerCase();
            if (name.endsWith(".zip") || name.endsWith(".gz"))
                return true;
            else
                return false;
        }

    }

    // [improve] support for sdcard
    public static void notifyMediaScanner(Context context, File file) {
        Log.i(TAG, "==> notifyMediaScanner");

        if (null == context)
            return;

        if (null != file) {
            context.sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } else {
            // [improve] use storage manager
            String state = Environment.getExternalStorageState();
            if (state.equals(Environment.MEDIA_MOUNTED)
                    || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                File ext = Environment.getExternalStorageDirectory();
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.fromFile(ext)));
            }
        }
    }

    public static int dipToPx(Context context, float dip) {
        if (null == context || dip < 0.0) {
            Log.e(TAG, "invalid parameter in dipToPx");
            return -1;
        } else {
            final float scale = context.getResources().getDisplayMetrics().density;

            return (int) (dip * scale + 0.5f);
        }
    }

    public static void setAppContext(Context context) {
        mContext = context;
    }
}
