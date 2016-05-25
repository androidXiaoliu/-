package com.baofeng.aone.filemanager.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.filemanager.bean.FileItem;
import com.baofeng.aone.filemanager.bean.Location;
import com.baofeng.aone.filemanager.bean.LocationHistory;
import com.baofeng.aone.filemanager.bean.StorageManager;
import com.baofeng.aone.filemanager.filebrowser.operation.FileMan;
import com.baofeng.aone.filemanager.utils.Constant;
import com.baofeng.aone.filemanager.utils.FileInfoCache;
import com.baofeng.aone.filemanager.utils.FileUtil;
import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.utils.ResultUtils;
import com.baofeng.aone.filemanager.volume.DiskVolume4_4;
import com.baofeng.aone.filemanager.volume.DiskVolumeMgr;
import com.baofeng.aone.filemanager.volume.Storage;
import com.google.gson.Gson;

public class FileManager extends AndroidManager {

    private static final String TAG = "FileManager";
    private List<FileItem> mListDir = new ArrayList<FileItem>();
    private List<FileItem> mListFile = new ArrayList<FileItem>();
    private List<FileItem> mListSys = new ArrayList<FileItem>();
    private List<FileItem> mlitCurDirEntries = new ArrayList<FileItem>();
    private StorageManager msManager = null;
    private LocationHistory mLocHistory = new LocationHistory();

    // status
    private static final int STATUS_BROWSE_DISK = 0;
    private static final int STATUS_BROWSE_DIR = 1;
    private int miStatus;

    // for dir/disk state
    private File mfCurDir = null;
    private long mlTimeStamp = 0;
    private File[] mCurDirFiles;

    private BrowserHandler mBrowserHandler = null;
    private HandlerThread mHandlerThread;

    private BrowseThread mBrowseToThread = null;
    private boolean browsetoThreadFlag = false;
    private static final int BROWSETO_RESULT_MSG = 3;
    private Context mContext;
    private static FileManager mFileManager;
    private DiskVolumeMgr mDiskVolumeMgr;

    private FileManager(Context context) {
        mContext = context;
        mDiskVolumeMgr = DiskVolumeMgr.getInstance();
        mDiskVolumeMgr.initContext(mContext);
        mDiskVolumeMgr.start();
        init();
    }

    public static FileManager getAndroidManager() {
        return getFileManager(LauncherApplication.getInstance());
    }

    private static synchronized FileManager getFileManager(Context context) {
        if (mFileManager == null) {
            mFileManager = new FileManager(context);
        }
        return mFileManager;
    }

    private void init() {
        FileUtil.setAppContext(mContext);
        mHandlerThread = new HandlerThread("imageloadThread");
        mHandlerThread.start();
        mBrowserHandler = new BrowserHandler(mHandlerThread.getLooper());
        msManager = StorageManager.getInstance(mContext);
        msManager.initStorages();
        // msManager.setStorageStateListener(msscListener);
        msManager.updateLocale();

    }

    public void registerVolumeChangeCallback(VolumeChangeCallback callback) {
        DiskVolume4_4.mVolumeChangeCallback = callback;
    }

    public void getFileList(String mstrInitDir, FileManagerCallback callback) {
        mLocHistory.clearAll();
        // initialize the filebrowser state
        if (null == mstrInitDir) {
            if (null == msManager || null == msManager.getValidStorageRoots()) {
                Log.e(TAG, "can not get valid storage root");
                callback.onBrowseFileFail(mstrInitDir,
                        ResultUtils.BROWSE_ERROR_NO_VALID_STORAGE);
            } else {
                File[] roots = null;
                roots = msManager.getValidLocalStorageRoots();

                if (null != roots) {
                    browseDisk(roots[0], true, false, callback);
                } else {
                    Log.i(TAG, "there is no valid local storage");
                    roots = msManager.getValidExternalStorageRoots();
                    if (null == roots) {
                        Log.i(TAG, "there is no valid external storage");
                        callback.onBrowseFileFail(mstrInitDir,
                                ResultUtils.BROWSE_ERROR_NO_VALID_STORAGE);
                    } else {
                        browseDisk(roots[0], true, false, callback);
                    }
                }
            }
        } else {
            File user = new File(mstrInitDir);

            // check the storage status
            if (msManager.isInValidStorage(user)) {
                if (!user.exists()) {
                    Log.e(TAG, "there is no valid fb_msg_nonexistent_dir");
                    callback.onBrowseFileFail(mstrInitDir,
                            ResultUtils.BROWSE_ERROR_NONEXISTENT_DIR);
                } else {
                    if (!Constant.isForUser(user)) {
                        Log.e(TAG,
                                "there is no valid fb_msg_no_right_to_browse");
                        callback.onBrowseFileFail(mstrInitDir,
                                ResultUtils.BROWSE_ERROR_NO_RIGHT_TO_BROWSE);
                    } else {
                        if (user.isFile())
                            user = user.getParentFile();
                        if (msManager.isStorageRoot(user)) {
                            browseDisk(user, true, false, callback);
                        } else {
                            browseDir(user, true, false, callback);
                        }
                    }
                }
            } else {
                String state = msManager.getStorageStatus(user);

                if (null == state || 0 == state.length()) {
                    Log.e(TAG, "state is unvalid");
                    callback.onBrowseFileFail(mstrInitDir,
                            ResultUtils.BROWSE_ERROR_NO_GET_STORAGE_STATE);
                } else {
                    if (state.equals(Environment.MEDIA_UNMOUNTABLE)) {
                        Log.e(TAG, "state is MEDIA_UNMOUNTABLE");
                    } else if (state.equals(Environment.MEDIA_UNMOUNTED)) {
                        Log.e(TAG, "state is MEDIA_UNMOUNTED");
                    } else if (state.equals(Environment.MEDIA_BAD_REMOVAL)) {
                        Log.e(TAG, "state is MEDIA_BAD_REMOVAL");
                    } else if (state.equals(Environment.MEDIA_NOFS)) {
                        Log.e(TAG, "state is MEDIA_NOFS");
                    } else if (state.equals(Environment.MEDIA_CHECKING)) {
                        Log.e(TAG, "state is MEDIA_CHECKING");
                    } else if (state.equals(Environment.MEDIA_REMOVED)) {
                        Log.e(TAG, "state is MEDIA_REMOVED");
                    } else if (state.equals(Environment.MEDIA_SHARED)) {
                        Log.e(TAG, "state is MEDIA_SHARED");
                    } else {
                        Log.e(TAG, "unknown state");
                    }
                    callback.onBrowseFileFail(mstrInitDir,
                            ResultUtils.BROWSE_ERROR_SD_NOT_AVAILABLE);
                }
            }
        }
    }

    private void browseDisk(File root, boolean needAddHistory,
            boolean needUpdateUI, FileManagerCallback callback) {
        Log.i(TAG, "==> browseDisk");

        if (null == root || !root.exists() || !root.isDirectory()
                || null == msManager || !msManager.isStorageRoot(root)) {
            // [improve] update the filemanager status
            Log.e(TAG, "invalid root in browseDisk");
            return;
        } else {
            // [improve] move it to updateUI
            if (needAddHistory) {
                if (null != mLocHistory.getCurLoc()) {
                    if (Constant.LOC_TYPE_ROOT != mLocHistory.getCurLoc()
                            .getType()
                            || !mLocHistory.getCurLoc().getData()
                                    .equals(root.getAbsolutePath()))
                        mLocHistory
                                .pushLoc(new Location(Constant.LOC_TYPE_ROOT,
                                        root.getAbsolutePath()));
                } else {
                    mLocHistory.pushLoc(new Location(Constant.LOC_TYPE_ROOT,
                            root.getAbsolutePath()));

                }
                mLocHistory.display();
            }

            miStatus = STATUS_BROWSE_DISK;

            getDirContent(root, needUpdateUI, callback);
        }
    }

    private void browseDir(final File file, boolean needAddHistory,
            boolean needUpdateUI, FileManagerCallback callback) {
        Log.i(TAG, "==> browseDir");

        miStatus = STATUS_BROWSE_DIR;

        getDirContent(file, needUpdateUI, callback);

        if (needAddHistory) {
            mLocHistory.pushLoc(new Location(Constant.LOC_TYPE_FOLDER, file
                    .getAbsolutePath()));
            mLocHistory.display();
        }
    }

    private void getDirContent(final File dir, final boolean needUpdateUI,
            final FileManagerCallback callback) {
        Log.i(TAG, "==> getDirContent");

        if (null == dir || !dir.exists() || !dir.isDirectory()) {
            Log.e(TAG, "getDirContent dir is invalid");
            callback.onBrowseFileFail(null,
                    ResultUtils.BROWSE_ERROR_NONEXISTENT_DIR);
            return;
        }

        mfCurDir = dir;
        mlTimeStamp = dir.lastModified();

        // start thread to get folder content
        // this if statement is used to avoid more than one thread to run
        // simultaneously.
        // add by xiangzm1 2012-08-08 to fix bugID : 856, 857, 860,797
        if (null == mBrowseToThread || !browsetoThreadFlag) {
            mBrowseToThread = new BrowseThread(mContext) {
                public void run() {
                    Log.d(TAG, "==> enter run() of BrowseThread");

                    browsetoThreadFlag = true;
                    updateDirContent(dir, callback);
                    if (null != mBrowseToThread)
                        mBrowseToThread.onBrowseToResult();
                }
            };
            mBrowseToThread.mbNeedUpdateUI = needUpdateUI;
            mBrowseToThread.start();
        }

    }

    private class BrowseThread extends Thread {
        Context mContext;
        boolean mStop = false;
        // boolean mbNeedAddHistory = false;
        boolean mbNeedUpdateUI = false;

        public BrowseThread(Context context) {
            // mStop = false;
            mContext = context;
        }

        public void toStop() {
            // mResume = true;
            mStop = true;
        }

        private void onBrowseToResult() {
            Log.i(TAG, "send message");
            mBrowserHandler.sendEmptyMessage(BROWSETO_RESULT_MSG);

        }
    }

    private class BrowserHandler extends Handler {

        public BrowserHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == BROWSETO_RESULT_MSG) {
                Log.i(TAG, "get BROWSETO_RESULT_MSG from browse thread");
                if (null != mBrowseToThread) {
                    mBrowseToThread = null;
                }
                browsetoThreadFlag = false;
            }
        }
    }

    // [improve] maybe add it in browse thread
    private void updateDirContent(File dir, FileManagerCallback callback) {
        Log.d(TAG, "==> updateDirContent");
        mlitCurDirEntries.clear();
        mCurDirFiles = new File[0];
        mCurDirFiles = dir.listFiles();

        if (null == mCurDirFiles) {
            return;
        } else {
            Log.i(TAG, "add file amount info in cache");
            FileInfoCache.addFileInfo(mfCurDir,
                    FileInfoCache.INFO_KEY_FILE_AMOUNT_IN_FOLDER,
                    mCurDirFiles.length);
        }

        mListDir.clear();
        mListFile.clear();
        mListSys.clear();

        for (File file : mCurDirFiles) {
            if (null != mBrowseToThread) {
                if (browsetoThreadFlag && mBrowseToThread.mStop)
                    break;
            }
            if (file.isHidden()) {
                continue;
            }

            if (file.isDirectory()) {
                if (!FileUtil.isInvalidDir(file)) {
                    // if (!Constant.isSysOrReservedDir(file)) {
                    // FileItem fi = new FileItem(file,
                    // FileUtil.getFileTypeString(mContext, file), false);
                    // mListDir.add(fi);
                    // } else {
                    // mListSys.add(new FileItem(file, FileUtil
                    // .getFileTypeString(mContext, file), true));
                    // }

                    mListSys.add(new FileItem(file, FileUtil.getFileTypeString(
                            mContext, file)));
                }
            } else if (file.isFile()) {
                FileItem it = new FileItem(file, FileUtil.getFileTypeString(
                        mContext, file));
                mListFile.add(it);

            } else {
                Log.e(TAG, "currentfile is not a file or dir");
            }
        }

        mlitCurDirEntries.addAll(mListSys);
        mlitCurDirEntries.addAll(mListDir);
        mlitCurDirEntries.addAll(mListFile);
        // add callback
        // String msg = JSON.toJSONString(mlitCurDirEntries);//fastjson
        // Log.d(TAG, msg);
        Gson gson = new Gson();
        String msg = gson.toJson(mlitCurDirEntries);
        callback.onBrowseFileSuccess(dir.getPath(), msg);

    }

    /*
     * [delete]
     */
    public void doDelete(String filePath, FileManagerCallback callback) {
        File file = new File(filePath);
        if (null == file || !file.exists()) {
            Log.e(TAG, "file is invalid in onDelete");
            callback.onDeleteFileFail(filePath,
                    ResultUtils.OPERATION_ERRO_FILE_NOT_EXIST);
            return;
        } else {
            // if (Constant.isSysFile(file)) {
            // Log.d(TAG, "file is sysFile ,can not delete!");
            // } else {

            FileMan fileMan = new FileMan(mContext, callback);
            fileMan.doDelete(Constant._DELETE, Constant.FROM_PATH, filePath);

            // }
        }
    }

    public void doOpenFile(String filePath, FileManagerCallback callback) {
        File file = new File(filePath);
        if (null == file || !file.exists()) {
            callback.onOpenFileFail(filePath,
                    ResultUtils.OPERATION_ERRO_FILE_NOT_EXIST);
            return;
        } else {
            if (file.isDirectory()) {
                browseDir(file, true, true, callback);
            } else if (file.isFile()) {
                FileUtil.openFile(mContext, filePath, callback);
            }
        }
    }

    public void unregisterVolumeChangeCallback() {
        DiskVolumeMgr.getInstance().stop();
    }

    public String getExternalSdcardState() {
        String state = Environment.MEDIA_UNMOUNTED;
        List<Storage> storages = mDiskVolumeMgr.getAllVolumes();
        for (Storage storage : storages) {
            // Log.d(TAG, "===path :"+storage.getRoot().getAbsolutePath()
            // +"  type : " + storage.getType());
            if (storage.getType() == Storage.STORAGE_TYPE_LOCAL) {
                state = storage.getState(false);
                return state;
            }
        }
        return state;
    }

    public String getExternalSdcardPath() {
        String path = null;
        List<Storage> storages = mDiskVolumeMgr.getAllVolumes();
        for (Storage storage : storages) {
            if (storage.getType() == Storage.STORAGE_TYPE_LOCAL) {
                path = storage.getRoot().getAbsolutePath();
                return path;
            }
        }
        return path;
    }
}
