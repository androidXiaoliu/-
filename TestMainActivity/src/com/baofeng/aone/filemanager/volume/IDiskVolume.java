package com.baofeng.aone.filemanager.volume;

import java.util.List;

import com.baofeng.aone.filemanager.volume.DiskVolumeMgr.OnDiskVolumeChangedListener;

import android.content.Context;

public interface IDiskVolume {

    public void start();

    public List<Storage> getVolumesByType(Context context, int type);

    public String getVolumeTitle(Context context, String path);

    public void setOnDiskVolumnChangedListener(
            OnDiskVolumeChangedListener listener);

    public void stop();

    public List<Storage> getAllVolumes();
}
