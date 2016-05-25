package com.baofeng.aone.download;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.utils.Utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadTools extends AndroidManager {

    private static final String TAG = "DownloadTools";
    private static DownloadTools sInstance;
    private Context mContext;
    private Map<Long, DownloadAsyncTask> mDownloadMap;
    private SharedPreferences mPreferences;
    private DownloadManagerPro managerPro;
    private DownloadManager mDownloadManager;

    private DownloadTools(Context context) {
        this.mContext = context;
        mDownloadMap = new HashMap<Long, DownloadAsyncTask>();
        mPreferences = mContext.getSharedPreferences("download",
                Context.MODE_APPEND);
        mDownloadManager = (android.app.DownloadManager) mContext
                .getSystemService(context.DOWNLOAD_SERVICE);
        managerPro = new DownloadManagerPro(mDownloadManager);
    }

    public static DownloadTools getAndroidManager() {
        return getDownloadManager(LauncherApplication.getInstance());
    }

    private static synchronized DownloadTools getDownloadManager(Context context) {
        if (sInstance == null) {
            sInstance = new DownloadTools(context);
        }
        return sInstance;
    }

    public void startDownload(String url, DownloadListener callback) {
        Log.d(TAG, "===> startDownload");
        long downloadId = startDownload(url);
        startDownloadAsynTask(callback, downloadId);

    }

    /*
     * public void restartDownload(String requestId, DownloadListener callback)
     * { long downloadId = stringToLong(requestId); Log.d(TAG,
     * "===> restartDownload downloadId="+downloadId);
     * mDownloadManager.restartDownload(downloadId);
     * startDownloadAsynTask(callback, downloadId); }
     */

    private long startDownload(String url) {
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                | DownloadManager.Request.NETWORK_WIFI);
        String downloadFrom = mContext
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + Utils.getMD5Str(url);
        Uri uriDestination = Uri.fromFile(new File(downloadFrom));
        request.setDestinationUri(uriDestination);
        long mDownloadId = mDownloadManager.enqueue(request);
        saveDownloadId(url, mDownloadId);
        return mDownloadId;
    }

    private void saveDownloadId(String downloadUrl, long downloadId) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(downloadUrl, downloadId);
        editor.commit();
    }

    public void pauseDownload(String requestId) {
        long downloadId = stringToLong(requestId);
        Log.d(TAG, "===> pauseDownload downloadId=" + downloadId);
        ReflectUtil.invorkMethod(DownloadManager.class, mDownloadManager, "pausedDownload", downloadId);
//        mDownloadManager.pausedDownload(downloadId);
    }

    public void resumeDownload(String requestId, DownloadListener callback) {
        long downloadId = stringToLong(requestId);
        Log.d(TAG, "===> resumeDownload downloadId=" + downloadId);
        ReflectUtil.invorkMethod(DownloadManager.class, mDownloadManager, "resumeDownload", downloadId);
//        mDownloadManager.resumeDownload(downloadId);
        startDownloadAsynTask(callback, downloadId);
    }

    public void cleanUpDownload(String deleteUrl) {
        long deleteDownloadId = getDownloadId(deleteUrl);
        removeDownloadId(deleteUrl);
        Log.d(TAG, "cleanUp deleteDownloadId = " + deleteDownloadId);
        if (deleteDownloadId == -1) {
            Log.d(TAG, "Could not remove invalid ID -1 .");
            return;
        }
        boolean removeSuccess = mDownloadManager.remove(deleteDownloadId) == 1;
        if (removeSuccess) {
            Log.d(TAG, "Successfully removed the device owner installer file.");
        } else {
            Log.d(TAG, "Could not remove the device owner installer file.");
            // Ignore this error. Failing cleanup should not stop provisioning
            // flow.
        }
        cancelDownloadTask(deleteUrl);
    }

    public void getDownloadFileList(final DownloadListCallback callback,
            final String accessAll) {
        ExecutorService singleThreadExecutor = Executors
                .newSingleThreadExecutor();
        Thread thread = new Thread() {
            @Override
            public void run() {
                managerPro.getDownloadList(callback, accessAll);
            }
        };
        singleThreadExecutor.execute(thread);
    }

    public void getDownloadFile(final DownloadFileCallback callback,
            final String url) {
        final long downloadId = getDownloadId(url);
        if (downloadId == -1) {
            callback.onDownloadFileInfo("");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                managerPro.getDownloadFileInfo(callback, downloadId);
            }
        }.start();
    }

    /*
     * private boolean taskIsRunning(String url) { DownloadAsyncTask task =
     * mDownloadMap.get(url); if (task != null && (task.getStatus() ==
     * AsyncTask.Status.RUNNING)) { return true; } return false; }
     * 
     * private void restoreDownloadStatus(String url, long downloadId,
     * DownloadListener listener) { int status =
     * managerPro.getStatusById(downloadId); switch (status) { case
     * DownloadManager.STATUS_RUNNING: Log.d(TAG, "STATUS_RUNNING"); if
     * (!taskIsRunning(url)) { DownloadAsyncTask task = new
     * DownloadAsyncTask(listener, managerPro); task.setDownloadId(downloadId);
     * task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
     * mDownloadMap.put(url, task); } break; case DownloadManager.STATUS_FAILED:
     * Log.d(TAG, "STATUS_FAILED"); int reason =
     * managerPro.getReason(downloadId); listener.onDownloadFail(url,
     * String.valueOf(reason)); break; case DownloadManager.STATUS_PAUSED: int[]
     * process = managerPro.getDownloadBytes(downloadId);
     * listener.onDownloadPause(url, process[0], process[1]); Log.d(TAG,
     * "STATUS_PAUSED"); break; case DownloadManager.STATUS_PENDING: Log.d(TAG,
     * "STATUS_PENDING"); listener.onDownloadPending(url, "STATUS_PENDING");
     * break; case DownloadManager.STATUS_SUCCESSFUL: Log.d(TAG,
     * "STATUS_SUCCESSFUL"); String location =
     * managerPro.getFileName(downloadId); listener.onDownloadSuccess(url,
     * location); break; default: break; } }
     */
    private void cancel(DownloadAsyncTask task) {
        if (task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(true);
        }
    }

    public void cancelDownloadTask(String url) {
        if (url != null && !url.isEmpty()) {
            DownloadAsyncTask needCancelTask = mDownloadMap.get(url);
            if (needCancelTask != null) {
                cancel(needCancelTask);
                mDownloadMap.remove(url);
            }
        }
    }

    public void cancelAllDownloadTask() {
        for (DownloadAsyncTask task : mDownloadMap.values()) {
            task.cancel(true);
        }
        mDownloadMap.clear();
    }

    private long getDownloadId(String url) {
        return mPreferences.getLong(url, -1);
    }

    private void removeDownloadId(String url) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.remove(url);
        editor.commit();
    }

    private void startDownloadAsynTask(DownloadListener callback,
            long downloadId) {
        DownloadAsyncTask task = new DownloadAsyncTask(callback, managerPro);
        task.setDownloadId(downloadId);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mDownloadMap.put(downloadId, task);
    }

    private long stringToLong(String downloadstring) {
        long downloadId = -1;
        try {
            downloadId = Long.parseLong(downloadstring);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return downloadId;
        }
        return downloadId;
    }

    private String getDownloadList() {
        Map<String, ?> allPreferences;
        SharedPreferences downloadPreferences = mContext.getSharedPreferences(
                "download", Context.MODE_APPEND);
        allPreferences = downloadPreferences.getAll();
        Set<String> keySet = allPreferences.keySet();
        StringBuilder stringBuilder = new StringBuilder();
        for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
            String next = it.next();
            stringBuilder.append(next);
            stringBuilder.append(",");
        }
        return stringBuilder.length() == 0 ? null : stringBuilder.substring(0,
                stringBuilder.length() - 1);
    }
}
