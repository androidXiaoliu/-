package com.baofeng.test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baofeng.R;
import com.baofeng.R.id;
import com.baofeng.R.layout;
import com.baofeng.aone.fota.OTACallback;
import com.baofeng.aone.fota.OTAManager;

public class TestFOTAActivity extends Activity implements OnClickListener, OTACallback{

    public static final String TAG = "TestFOTAActivity";
    private Button mStart,mPause,mResume,mCancel,mGetStatus;
    private OTAManager mOTA;
    private static final int START_SUCCESSFUL = 0;
    private static final int START_FAILED = 1;
    private static final int START_NO_NEW_VERSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fota_test);
        mOTA = (OTAManager) OTAManager.getAndroidManager();
        mStart = (Button) findViewById(R.id.start);
        mPause = (Button) findViewById(R.id.pause);
        mResume = (Button) findViewById(R.id.resume);
        mCancel = (Button) findViewById(R.id.cancel);
        mGetStatus = (Button) findViewById(R.id.get_status);
        mStart.setOnClickListener(this);
        mPause.setOnClickListener(this);
        mResume.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mGetStatus.setOnClickListener(this);
        mResume.setEnabled(false);
        mPause.setEnabled(false);
        mCancel.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.start:
            synchronized(this){
                new Thread(){
                    @Override
                    public void run() {
                        mOTA.start(TestFOTAActivity.this);
                        super.run();
                    }
                }.start();
            }
            mStart.setEnabled(false);
            mResume.setEnabled(false);
            mPause.setEnabled(true);
            mCancel.setEnabled(true);
            break;
        case R.id.pause:
            mOTA.pause(TestFOTAActivity.this);
            mStart.setEnabled(false);
            mPause.setEnabled(false);
            mResume.setEnabled(true);
            mCancel.setEnabled(true);
            break;
        case R.id.resume:
            synchronized(this){
                new Thread() {
                    @Override
                    public void run() {
                        mOTA.resume(TestFOTAActivity.this);
                        super.run();
                    }
                }.start();
            }
            mStart.setEnabled(false);
            mPause.setEnabled(true);
            mCancel.setEnabled(true);
            mResume.setEnabled(false);
            break;
        case R.id.cancel:
            mOTA.cancel(TestFOTAActivity.this);
            mCancel.setEnabled(false);
            mStart.setEnabled(true);
            mPause.setEnabled(false);
            mResume.setEnabled(false);
            break;
        case R.id.get_status:
            mOTA.getOTAStatus(this);
            break;
        default:
            break;
        }
    }

    @Override
    public void onOTAStart(int status) {
        switch(status){
        case START_FAILED:
            Log.d(TAG, "onOTAStart Start failed.");
            break;
        case START_SUCCESSFUL:
            Log.d(TAG, "onOTAStart Start successful.");
            break;
        case START_NO_NEW_VERSION:
            Log.d(TAG, "onOTAStart no new version to upgrade.");
            break;
        default:
            break;
        }
    }

    @Override
    public void onOTAPause(boolean result) {
        Log.d(TAG, "onOTAPause result = " + result);
    }

    @Override
    public void onOTAResume(boolean result) {
        Log.d(TAG, "onOTAResume result = " + result);
    }

    @Override
    public void onOTACancel(boolean result) {
        Log.d(TAG, "onOTACancel result = " + result);
    }

    @Override
    public void onGetOTAStatus(int status) {
        Log.d(TAG, "onGetOTAStatus status = " + status);
    }
}
