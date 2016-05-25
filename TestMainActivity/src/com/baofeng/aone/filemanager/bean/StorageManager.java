package com.baofeng.aone.filemanager.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baofeng.aone.filemanager.utils.FileUtil;
import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.volume.DiskVolumeMgr;
import com.baofeng.aone.filemanager.volume.Storage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

class ESWatcher extends Object {
    private static final String TAG = "ESWatcher";

    interface OnESChangeListener {
        boolean onESChanged(Intent intent);
    }

    private Context mContext = null;
    private BroadcastReceiver mReceiver = null;
    private OnESChangeListener mescListener = null;
    private boolean isRegister = false;

    public ESWatcher(Context context) {
        mContext = context;
        initWatcher();
    }

    private void initWatcher() {
        if (null == mReceiver) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.i(TAG, "es is changed");
                    onESMessage(context, intent);
                }
            };
        }

        if (null != mReceiver) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction("android.intent.action.USB_MOUNTED");
            filter.addAction("android.intent.action.USB_UNMOUNTED");
            filter.addDataScheme("file");

            if (null != mReceiver) {
                mContext.registerReceiver(mReceiver, filter);
                isRegister = true;
            } else
                Log.e(TAG, "can not register sdcard msg receiver");
        } else
            Log.e(TAG, "create sdCardIntentReceiver fail in onCreate");
    }

    public void release() {
        if (null != mReceiver && isRegister) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    private void onESMessage(Context context, Intent intent) {
        Log.i(TAG, "the action is " + intent.getAction());
        if (null != mescListener)
            mescListener.onESChanged(intent);
    }

    public void setESChangedListener(OnESChangeListener listener) {
        mescListener = listener;
    }
}

public class StorageManager extends Object {
    private static final String TAG = "StorageManager";
    private OnStorageStateListener msscListener = null;

    public void setStorageStateListener(OnStorageStateListener listener) {
        Log.v(TAG,
                "setStorageStateListener---------------------------------------------"
                        + listener);
        this.msscListener = listener;
    }

    public interface OnStorageStateListener {
        boolean onStorageStateChanged(StorageManager sm, File root, String msg);
    }

    private class DiskVolumeChangedObserver extends ContentObserver {

        public DiskVolumeChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "usb volumes changed! ");
            super.onChange(selfChange);
            for (Storage stg : mlStorages) {
                if (Storage.STORAGE_TYPE_EXTERNAL_USB == stg.getType()) {
                    mlStorages.remove(stg);
                }
            }
            initUsbStorages();
        }
    }

    private ESWatcher.OnESChangeListener escListener = new ESWatcher.OnESChangeListener() {
        public boolean onESChanged(Intent intent) {
            Log.i(TAG, "==> onESChanged ");

            if (null == intent) {
                Log.e(TAG, "intent is null in onESChanged");
                return false;
            }

            // get storage in storage list
            Uri uri = intent.getData();

            File esRoot = null;
            if (null != uri) {
                esRoot = FileUtil.getFile(uri);
                // if (null == esRoot || !esRoot.isDirectory()){
                if (null == esRoot) {
                    Log.e(TAG, "invalid esRoot gotten from uri");
                    return false;
                } else
                    Log.i(TAG, "esRoot is " + esRoot.getAbsolutePath());
            }

            Storage es = getStorageByFile(esRoot);

            // get message
            String action = intent.getAction();

            if (null == action || 0 == action.length()) {
                Log.e(TAG, "action is empty in onESChanged");
                return false;
            } else
                Log.i(TAG, "action is " + action);

            // initialize the message
            String msg = null;
            if (null == es) {
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    es = new Storage(esRoot, Storage.STORAGE_TYPE_EXTERNAL);
                    mlStorages.add(es);
                    es.dump();

                    msg = Intent.ACTION_MEDIA_MOUNTED;
                } else if (action
                        .equalsIgnoreCase("android.intent.action.USB_MOUNTED")) {
                    String title = DiskVolumeMgr.getInstance().getVolumeTitle(
                            mContext, esRoot.getAbsolutePath());
                    es = new Storage(esRoot, Storage.STORAGE_TYPE_EXTERNAL_USB,
                            title, Environment.MEDIA_MOUNTED);
                    mlStorages.add(es);
                    es.dump();

                    msg = "android.intent.action.USB_UNMOUNTED";
                }
            } else {
                // modify start by caifangjue -- update usb storage state
                // accoding to action type
                if (es.getType() == Storage.STORAGE_TYPE_EXTERNAL_USB) {
                    String newState = null;
                    if (action
                            .equalsIgnoreCase("android.intent.action.USB_MOUNTED"))
                        newState = Environment.MEDIA_MOUNTED;
                    if (action
                            .equalsIgnoreCase("android.intent.action.USB_UNMOUNTED"))
                        newState = Environment.MEDIA_UNMOUNTED;
                    msg = action;
                    if (newState != null)
                        es.setState(newState);
                } else {
                    if (es.updateState())
                        msg = action;
                }
                // modify end by caifangjue
            }

            if (msg == null)
                msg = action;
            // send message when necessary
            if (null != msscListener) {
                if (null != msg && null != esRoot) {
                    return msscListener.onStorageStateChanged(
                            StorageManager.this, esRoot, msg);
                } else {
                    Log.i(TAG, "esRoot is null");
                    return false;
                }
            } else {
                Log.i(TAG, "msscListener is null");
                return false;
            }
        }
    };

    private static final long LONG_RESERVED_SPACE_SIZE = 41943040; // 40M

    private static StorageManager sInstance = null;

    private Context mContext = null;
    private ESWatcher mWatcher = null;
    private List<Storage> mlStorages = null;

    private StorageManager(Context context) {
        Log.i(TAG, "==> StorageManager");

        mContext = context.getApplicationContext();
        if (null == mlStorages) {
            mlStorages = new ArrayList<Storage>();
        }

        mWatcher = new ESWatcher(mContext);
        mWatcher.setESChangedListener(escListener);

        // initStorages();
    }

    public void initStorages() {
        Log.i(TAG, "==> initStorages");
        mlStorages = DiskVolumeMgr.getInstance().getAllVolumes();
        for (Storage storage : mlStorages) {
            storage.dump();
        }
    }

    private void initUsbStorages() {
        List<Storage> list = DiskVolumeMgr.getInstance().getVolumesByType(
                mContext, 1);
        Storage es = null;
        for (int i = 0; i < list.size(); i++) {
            es = list.get(i);
            es.setType(Storage.STORAGE_TYPE_EXTERNAL_USB);
            mlStorages.add(es);
        }
    }

    public boolean isStorageRoot(File file) {
        if (null == file || !file.exists())
            return false;
        else {
            if (null == mlStorages || 0 == mlStorages.size()) {
                Log.e(TAG, "stroage list is empty in getStorageByFile");
                return false;
            } else {
                String path = file.getAbsolutePath();
                if (null == path || 0 == path.length())
                    return false;
                else {
                    if (file.isFile()) {
                        return false;
                    } else {
                        if (!path.endsWith("/"))
                            path = path + "/";
                        for (Storage stg : mlStorages) {
                            String root = stg.getRoot().getAbsolutePath();
                            if (null != root && 0 != root.length()
                                    && !root.endsWith("/"))
                                root = root + "/";

                            if (path.equals(root))
                                return true;
                        }
                        return false;
                    }
                }
            }
        }
    }

    public File getStorageRootByFile(File file) {
        Storage storage = getStorageByFile(file);

        if (null != storage)
            return storage.getRoot();
        else
            return null;
    }

    public File getStorageRootByFile(String path) {
        if (null == path || 0 == path.length()) {
            Log.e(TAG, "invalid path in getStorageRootByFile");
            return null;
        } else {
            File file = new File(path);
            if (null == file)
                return null;
            else
                return getStorageRootByFile(file);
        }
    }

    public boolean isInValidStorage(File file) {
        Storage storage = getStorageByFile(file);

        if (null == storage) {
            Log.i(TAG, "can not get storage by file");
            return false;
        } else {
            // [issue] maybe to update state here is better
            // but this function will be used for much time,
            // moreover, the storage state should be updated
            // only when StorageManager receives message
            return storage.isValid();
        }
    }

    public boolean isInThisStorage(File root, File file) {
        Log.i(TAG, "==> isInThisStorage");
        // if (null == root || !root.isDirectory()){
        if (null == root) {
            return false;
        } else {
            Storage storage = getStorageByFile(file);
            if (null == storage)
                return false;
            else {
                if (storage.getRoot().equals(root))
                    return true;
                else
                    return false;
            }
        }

    }

    public long getStorageTotalSizeByFile(File file) {
        Storage storage = getStorageByFile(file);

        if (null == storage || !storage.isValid()) {
            return -1;
        } else {
            return storage.getTotalSize();
        }
    }

    public long getStorageFreeSizeByFile(File file) {
        Storage storage = getStorageByFile(file);

        if (null == storage || !storage.isValid()) {
            return -1;
        } else {
            return storage.getFreeSize();
        }
    }

    public int getStorageTypeByFile(File file) {
        Storage storage = getStorageByFile(file);

        if (null != storage)
            return storage.getType();
        else
            return -1;
    }

    public String getStorageNameByFile(File file) {
        Log.i(TAG, "==> getStorageNameByFile");
        Storage storage = getStorageByFile(file);

        if (null != storage) {
            int rid = storage.getNameRID();
            if (0 < rid) {
                return mContext.getString(rid);
            } else {
                return storage.getName();
            }
        } else
            return null;
    }

    public void setStorageNameByFile(File file, String name) {
        if (null == name || 0 == name.length()) {
            return;
        }

        Storage storage = getStorageByFile(file);
        if (null != storage)
            storage.setName(name);
    }

    public void setStorageNameRIDByFile(File file, int rid) {
        if (0 >= rid) {
            return;
        }

        Storage storage = getStorageByFile(file);
        if (null != storage)
            storage.setNameRID(rid);
    }

    // just for testing, change private to public
    public Storage getStorageByFile(File file) {
        if (null == file)
            return null;
        else {
            if (null == mlStorages || 0 == mlStorages.size()) {
                Log.e(TAG, "storage list is empty in getStorageByFile");
                return null;
            } else {
                String path = file.getAbsolutePath();
                if (null == path || 0 == path.length()) {
                    return null;
                } else {
                    // if (file.isDirectory() && !path.endsWith("/"))
                    if (!path.endsWith("/"))
                        path = path + "/";

                    for (Storage stg : mlStorages) {
                        String root = stg.getRoot().getAbsolutePath();
                        if (!root.endsWith("/"))
                            root = root + "/";
                        if (path.startsWith(root))
                            return stg;
                    }
                    return null;
                }
            }
        }
    }

    private File[] getRootsForStorages(List<Storage> ls) {
        if (null == ls || 0 == ls.size())
            return null;
        else {
            File[] roots = new File[ls.size()];
            for (int i = 0; i < ls.size(); i++) {
                roots[i] = ls.get(i).getRoot();
            }
            return roots;
        }
    }

    public File[] getStorageRoots() {
        if (null == mlStorages || 0 == mlStorages.size()) {
            Log.e(TAG, "stroage list is empty in getStorageRoots");
            return null;
        } else {
            return getRootsForStorages(mlStorages);
        }
    }

    public File[] getValidStorageRoots() {
        if (null == mlStorages || 0 == mlStorages.size()) {
            Log.e(TAG, "stroage list is empty in getStorageRoots");
            return null;
        } else {
            List<Storage> lsValid = new ArrayList<Storage>();

            for (Storage stg : mlStorages) {
                if (stg.isValid()) {
                    lsValid.add(stg);
                }
            }
            return getRootsForStorages(lsValid);
        }
    }

    public File[] getExternalStorageRoots() {
        List<Storage> ls = getStoragesByType(Storage.STORAGE_TYPE_EXTERNAL,
                false);
        return getRootsForStorages(ls);
    }

    public File[] getLocalStorageRoots() {
        List<Storage> ls = getStoragesByType(Storage.STORAGE_TYPE_LOCAL, false);
        return getRootsForStorages(ls);
    }

    public File[] getValidLocalStorageRoots() {
        List<Storage> ls = getStoragesByType(Storage.STORAGE_TYPE_LOCAL, true);
        return getRootsForStorages(ls);
    }

    public File[] getValidExternalStorageRoots() {
        List<Storage> ls = getStoragesByType(Storage.STORAGE_TYPE_EXTERNAL,
                true);
        return getRootsForStorages(ls);
    }

    private List<Storage> getStoragesByType(int type, boolean onlyValid) {
        if (type != Storage.STORAGE_TYPE_EXTERNAL
                && type != Storage.STORAGE_TYPE_LOCAL
                && type != Storage.STORAGE_TYPE_EXTERNAL_USB) {
            return null;
        } else {
            if (null == mlStorages || 0 == mlStorages.size()) {
                Log.e(TAG, "mlStorages is empty or null");
                return null;
            } else {
                List<Storage> ls = new ArrayList<Storage>();
                for (Storage stg : mlStorages) {
                    if (type == stg.getType()) {
                        if (!onlyValid || onlyValid && stg.isValid())
                            ls.add(stg);
                    }
                }

                if (0 == ls.size()) {
                    return null;
                } else
                    return ls;
            }
        }
    }

    public boolean hasEnoughSpace(File file, long size) {
        Storage storage = getStorageByFile(file);

        if (null == storage) {
            Log.e(TAG, "can not find storage in getStorageStatus");
            return false;
        } else {
            long free = storage.getFreeSize();

            if ((free - LONG_RESERVED_SPACE_SIZE) > size)
                return true;
            else {
                Log.i(TAG, "the free space is :" + String.valueOf(free));
                return false;
            }
        }
    }

    public String getStorageStatus(File file) {
        Storage storage = getStorageByFile(file);

        if (null == storage) {
            Log.e(TAG, "can not find storage in getStorageStatus");
            return null;
        } else {
            return storage.getState(false);
        }
    }

    public synchronized static StorageManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new StorageManager(context);
        }
        return sInstance;
    }

    public synchronized static void releaseInstance() {
        if (null != sInstance) {
            sInstance.mWatcher.release();
        }
    }

    public void updateLocale() {
        for (Storage s : mlStorages) {
            String newName = DiskVolumeMgr.getInstance().getVolumeTitle(
                    mContext, s.getRoot().getAbsolutePath());
            s.setName(newName);
        }
    }
}
