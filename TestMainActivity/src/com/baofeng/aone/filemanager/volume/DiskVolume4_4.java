package com.baofeng.aone.filemanager.volume;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.baofeng.aone.filemanager.filebrowser.VolumeChangeCallback;
import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.utils.ResUtils;
import com.baofeng.aone.filemanager.volume.DiskVolumeMgr.OnDiskVolumeChangedListener;

public class DiskVolume4_4 implements IDiskVolume {

    public static final String TAG = "DiskVolume4_4";
    private static final String ENV_ANDROID_STORAGE = "ANDROID_STORAGE";
    private String mExternalStorageRoot;
    private BroadcastReceiver mExternalStorageReceiver;
    private Context mContext;
    private OnDiskVolumeChangedListener listener = null;
    private List<Storage> storages = null;
    private StorageComparator mStorageComparator = null;
    public static VolumeChangeCallback mVolumeChangeCallback;

    public DiskVolume4_4(Context context) {
        this.mContext = context;
        storages = new ArrayList<Storage>();
        mStorageComparator = new StorageComparator();
    }

    @Override
    public void start() {
        Log.i(TAG, "start");
        startWatchingExternalStorage();
    }

    @Override
    public List<Storage> getVolumesByType(Context context, int type) {
        Log.i(TAG, "getVolumesByType " + type);
        List<Storage> datas = new ArrayList<Storage>();
        for (Storage storage : storages) {
            if (storage.getType() == type) {
                datas.add(storage);
            }
        }
        return datas;
    }

    @Override
    public String getVolumeTitle(Context context, String path) {
        Log.i(TAG, "getVolumeTitle " + path);
        for (Storage storage : storages) {
            if (storage.getRoot().getAbsolutePath().equals(path)) {
                return storage.getName();
            }
        }
        return "unknown";
    }

    @Override
    public void setOnDiskVolumnChangedListener(
            OnDiskVolumeChangedListener listener) {
        this.listener = listener;
    }

    private void updateExternalStorageList() {
        storages = new ArrayList<Storage>();
        File allStorage = new File(mExternalStorageRoot);

        for (File file : allStorage.listFiles()) {
            String state = Environment.getStorageState(file);
            Log.i(TAG, "state: " + state);

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                String path = file.getAbsolutePath();
                String name = file.getName();
                Log.i(TAG, "Add storage " + path + ";" + name);
                int type = 1;
                if (name.toLowerCase().contains("sdcard")) {
                    type = 0;
                }
                Storage storage = new Storage(new File(path), type, name,
                        Environment.MEDIA_MOUNTED);
                storages.add(storage);
            }
        }
        sortAndRenameStorage();
        if (listener != null) {
            listener.onDiskVolumeChanged(storages);
        }
    }

    private void sortAndRenameStorage() {
        Collections.sort(storages, mStorageComparator);
        int i = 1;
        String storagePrefix = ResUtils.storage_prefix;
        for (Storage storage : storages) {
            Log.d(TAG, "sorted " + storage.getName());
            if (storage.getType() == 1) {
                storage.setName(storagePrefix + (i++));
            } else if (storage.getType() == 0) {
                storage.setName(ResUtils.sdcard);
            }
        }
    }

    public void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i(TAG, "Storage receiver: " + action);
                updateExternalStorageList();
                if(mVolumeChangeCallback != null) {
                    mVolumeChangeCallback.onVolumeStateChange(action);
                }
            }
        };
        mExternalStorageRoot = System.getenv(ENV_ANDROID_STORAGE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        mContext.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageList();
    }

    public void stopWatchingExternalStorage() {
        mContext.unregisterReceiver(mExternalStorageReceiver);
    }

    @Override
    public void stop() {
        Log.i(TAG, "stop");
        stopWatchingExternalStorage();
        this.listener = null;
    }

    @Override
    public List<Storage> getAllVolumes() {
        return storages;
    }

    class StorageComparator implements Comparator<Storage> {

        @Override
        public int compare(Storage lhs, Storage rhs) {
            if (lhs.getType() > rhs.getType()) {
                return 1;
            } else {
                return lhs.getName().compareTo(rhs.getName());
            }
        }
    }
}
