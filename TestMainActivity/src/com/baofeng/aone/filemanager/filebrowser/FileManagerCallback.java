package com.baofeng.aone.filemanager.filebrowser;

import com.baofeng.aone.AndroidCallback;

public interface FileManagerCallback extends AndroidCallback {

    public void onDeleteFileSuccess(String fileName);

    public void onDeleteFileFail(String filePath, int reason);

    public void onBrowseFileSuccess(String path, String msg);

    public void onBrowseFileFail(String path, int reason);

    public void onOpenFileFail(String path, int reason);

}
