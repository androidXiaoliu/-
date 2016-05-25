package com.baofeng.aone.filemanager.volume;

import java.util.List;

import android.content.Context;

public class DiskVolumeMgr implements IDiskVolume {

    public static final String TAG = "DiskVolumeMgr";
    public static DiskVolumeMgr instance = null;
    private IDiskVolume diskVolume = null;

    private DiskVolumeMgr() {
    }

    public static DiskVolumeMgr getInstance() {
        if (instance == null) {
            instance = new DiskVolumeMgr();
        }
        return instance;
    }

    public void initContext(Context context) {
        diskVolume = new DiskVolume4_4(context); // android4.4
        // diskVolume = new DiskVolumeProvider(context); //except android 4.4
    }

    @Override
    public List<Storage> getVolumesByType(Context context, int type) {
        return diskVolume.getVolumesByType(context, type);
    }

    @Override
    public String getVolumeTitle(Context context, String path) {
        return diskVolume.getVolumeTitle(context, path);
    }

    public void setOnDiskVolumnChangedListener(
            OnDiskVolumeChangedListener listener) {
        diskVolume.setOnDiskVolumnChangedListener(listener);
    }

    @Override
    public void start() {
        diskVolume.start();
    }

    @Override
    public void stop() {
        if (diskVolume != null) {
            diskVolume.stop();
        }
        instance = null;
    }

    @Override
    public List<Storage> getAllVolumes() {
        return diskVolume.getAllVolumes();
    }

    public interface OnDiskVolumeChangedListener {
        public void onDiskVolumeChanged(List<Storage> storages);
    }

}
