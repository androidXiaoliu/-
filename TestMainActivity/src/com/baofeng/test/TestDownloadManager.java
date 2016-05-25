package com.baofeng.test;

import com.baofeng.aone.download.DownloadFile;
import com.baofeng.aone.download.DownloadFileCallback;
import com.baofeng.aone.download.DownloadListCallback;
import com.baofeng.aone.download.DownloadListener;
import com.baofeng.aone.download.DownloadTools;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baofeng.R;
import com.google.gson.Gson;

public class TestDownloadManager extends Activity implements OnClickListener{
    
    private static final String TAG = "TestDownloadManager";
    private DownloadTools mDownloadTools;
    private Button mButton;
    private TextView mTextView;
    private int mStatus = DownloadManager.STATUS_PENDING;
    private static final int STATUS_INIT = DownloadManager.STATUS_PENDING;
    private static final int STATUS_PAUSE = DownloadManager.STATUS_PAUSED;
    private static final int STATUS_RUN = DownloadManager.STATUS_RUNNING;
    private static final int STATUS_FAIL = DownloadManager.STATUS_FAILED;
    private static final int STATUS_SUCCESS = DownloadManager.STATUS_SUCCESSFUL;
    String downloadUrl = "http://dldir1.qq.com/qqfile/qq/QQ6.0/11743/QQ6.0.exe";
    private static final int UPDATE_BUTTON_TEXT = 10;
    private long downloadId = -1;
    private boolean mHasTask = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        mButton = (Button)findViewById(R.id.download);
        mTextView = (TextView)findViewById(R.id.progress);
      //  updateButtonView();
        
        mButton.setOnClickListener(this);
        mDownloadTools = DownloadTools.getAndroidManager();
        mDownloadTools.getDownloadFile(new DownloadFileCallbackTest(), downloadUrl);
    }
    
    private void updateButtonView() {

        switch (mStatus) {
        case STATUS_INIT:
        case STATUS_FAIL:
        case STATUS_SUCCESS:
            mButton.setText(R.string.download_start);
            break;
        case STATUS_PAUSE:
            mButton.setText(R.string.download_resume);
            break;
        case STATUS_RUN:
            mButton.setText(R.string.download_pause);
            break;

        default:
            break;
        }
    }
    
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == UPDATE_BUTTON_TEXT) {
                updateButtonView();
            }
        }
        
    };
    
    
    
    class DownloadListCallbackTest implements DownloadListCallback {

        @Override
        public void onDownloadFile(String content) {
            
        }
        
    }
    
    class DownloadFileCallbackTest implements DownloadFileCallback {

        @Override
        public void onDownloadFileInfo(String msg) {
            if(!msg.isEmpty()) {
                Gson gson = new Gson();
                DownloadFile file = gson.fromJson(msg, DownloadFile.class);
                int status = file.getStatus();
                mStatus = status;
                downloadId = file.getIdx();
                mHasTask = true;
            }
            mHandler.sendEmptyMessage(UPDATE_BUTTON_TEXT);
 
        }
        
    }
    
    class DownloadStatusCallback implements DownloadListener {

        @Override
        public void onDownloadSuccess(String string) {
            Log.d(TAG, "onDownloadSuccess:" + string);
            mStatus = STATUS_SUCCESS;
            mHandler.sendEmptyMessage(UPDATE_BUTTON_TEXT);
        }

        @Override
        public void onDownloadFail(String string, String msg) {
            Log.d(TAG, "onDownloadFail:" + string);
            Log.d(TAG, "onDownloadFail reason : " + msg);
            mStatus = STATUS_FAIL;
            mHandler.sendEmptyMessage(UPDATE_BUTTON_TEXT);
        }

        @Override
        public void onDownloadRunning(String url, int downloadSize,
                int totalSize) {
            Log.d(TAG, "onDownloadRunning:" + downloadSize+"/"+totalSize);
            if(mStatus != STATUS_RUN) {
                mStatus = STATUS_RUN;
                mHandler.sendEmptyMessage(UPDATE_BUTTON_TEXT);
            }
        }

        @Override
        public void onDownloadPending(String url) {
            
        }

        @Override
        public void onDownloadPause(String file, int downloadSize, int totalSize) {
            Log.d(TAG, "onDownloadPause:" + downloadSize+"/"+totalSize);
            mStatus = STATUS_PAUSE;
            mHandler.sendEmptyMessage(UPDATE_BUTTON_TEXT);
        }
        
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.download:
            startDownloadEvent();
            break;

        default:
            break;
        }
        
    }
    
    private void startDownloadEvent(){
        switch (mStatus) {
        case STATUS_INIT:
        case STATUS_SUCCESS:
        case STATUS_FAIL:
            mDownloadTools.startDownload(downloadUrl, new DownloadStatusCallback());
            mDownloadTools.getDownloadFile(new DownloadFileCallbackTest(), downloadUrl);
            break;
        case STATUS_RUN:
            mDownloadTools.pauseDownload(Long.toString(downloadId));
            break;
        case STATUS_PAUSE:
            mDownloadTools.resumeDownload(Long.toString(downloadId), new DownloadStatusCallback());
            break;
   

        default:
            break;
        }
    }

}
