package com.baofeng.aone.filemanager.filebrowser;

import com.baofeng.aone.AndroidCallback;

public interface VolumeChangeCallback extends AndroidCallback {

    public void onVolumeStateChange(String state);

}
