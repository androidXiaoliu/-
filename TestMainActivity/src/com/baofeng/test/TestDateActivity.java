package com.baofeng.test;
import com.baofeng.R;
import com.baofeng.R.id;
import com.baofeng.R.layout;
import com.baofeng.aone.date.DateCallback;
import com.baofeng.aone.date.DateManager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class TestDateActivity extends Activity  implements DateCallback, OnClickListener{

    private static final String TAG = "TestDateActivity";
    private TextView dateView,mTextViewDate,mTextViewTime;
    private Button mBtnGetDate, mBtnGetTime;
    private DateManager mDateManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_time);

        mDateManager = (DateManager) DateManager.getAndroidManager();
        mDateManager.registerDateChangelistner(this);
        dateView = (TextView) findViewById(R.id.date);
        mTextViewDate = (TextView) findViewById(R.id.tv_date);
        mTextViewTime = (TextView) findViewById(R.id.tv_time);
        mBtnGetDate = (Button) findViewById(R.id.get_date);
        mBtnGetTime = (Button) findViewById(R.id.get_time);
        mBtnGetDate.setOnClickListener(this);
        mBtnGetTime.setOnClickListener(this);
    }

    @Override
    public void onUpdateDate(String time) {
        Log.d(TAG, "onUpdateDate Date is " + time);
        if (dateView != null) {
            dateView.setText(time);
        }
    }

    @Override
    public void onGetTime(String time) {
        Log.d(TAG,"onGetTime time is " + time);
        if (mTextViewTime != null) {
            mTextViewTime.setText(time);
        }
    }

    @Override
    public void onGetDate(String date) {
        Log.d(TAG, "onGetDate date is " + date);
        if (mTextViewDate != null) {
            mTextViewDate.setText(date);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
        case R.id.get_date:
            mDateManager.getDate(TestDateActivity.this);
            break;
        case R.id.get_time:
            mDateManager.getTime(TestDateActivity.this);
            break;
        }
    }
}
