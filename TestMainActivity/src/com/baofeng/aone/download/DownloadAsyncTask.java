package com.baofeng.aone.download;

import android.app.DownloadManager;
import android.os.AsyncTask;

public class DownloadAsyncTask extends AsyncTask<String, String, String> {
    private DownloadListener mDownloadListener;
    private long mDownloadId = 0;
    private DownloadManagerPro mPro;
    private boolean mFinished = false;

    public DownloadAsyncTask(DownloadListener listener, DownloadManagerPro managerPro) {
        mDownloadListener = listener;
        mPro = managerPro;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "start download";
        if(isCancelled()) {
           return result;
        }
        try {
          // while(!isCompleted(mDownloadId)) {
            while(!mFinished) {
                 queryDownloadStatus(mDownloadId);
                 Thread.sleep(500);
           }
           result = "finished download !";
        } catch (Exception e) {
           e.printStackTrace();
        }
        return result;
    }

   //onDownloadProcess
    private int queryDownloadStatus(long downloadId) {
        int status = mPro.getStatusById(downloadId);
        String downloadUrl = mPro.getUri(downloadId);
        if(status == DownloadManager.STATUS_RUNNING) {
            int[] currentProgress = mPro.getDownloadBytes(downloadId);
            if(currentProgress[0] > 0) { //avoid displaying 0/-1
            //currentProgress[0] : current download size
            //currentProgress[1] : total size
                mDownloadListener.onDownloadRunning(downloadUrl, currentProgress[0], currentProgress[1]);
            }
        }else if(status == DownloadManager.STATUS_SUCCESSFUL) {
            mFinished = true;
           // String location = mPro.getFileName(downloadId);
            String fileInfo = mPro.getDownloadFileInfo(null, downloadId);
            mDownloadListener.onDownloadSuccess(fileInfo);
        }else if(status == DownloadManager.STATUS_FAILED) {
            mFinished = true;
            int reason = mPro.getReason(downloadId);
            String fileInfo = mPro.getDownloadFileInfo(null, downloadId);
            mDownloadListener.onDownloadFail(fileInfo, String.valueOf(reason));
        }else if(status == DownloadManager.STATUS_PAUSED) {
            mFinished = true;
            int[] currentProgress = mPro.getDownloadBytes(downloadId);
            String fileInfo = mPro.getDownloadFileInfo(null, downloadId);
            mDownloadListener.onDownloadPause(fileInfo, currentProgress[0], currentProgress[1]);
        }
        return status;
    }

    public void setDownloadId(long id) {
        mDownloadId = id;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}
