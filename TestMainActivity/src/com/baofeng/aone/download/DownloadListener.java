package com.baofeng.aone.download;

import com.baofeng.aone.AndroidCallback;


public interface DownloadListener extends AndroidCallback{
    /* onDownloadSuccess : DownloadManager.STATUS_SUCCESSFUL
     * onDownloadFail : DownloadManager.STATUS_FAILED
     * onDownloadRunning : DownloadManager.STATUS_RUNNING
     * onDownloadPending : DownloadManager.STATUS_PENDING
     * onDownloadPause : DownloadManager.STATUS_PAUSED
     */
	public void onDownloadSuccess(String fileInfo);
	public void onDownloadFail(String fileInfo, String reason);
	public void onDownloadRunning(String url, int downloadSize, int totalSize);
	public void onDownloadPending(String fileInfo);
	public void onDownloadPause(String fileInfo, int downloadSize, int totalSize);

}
