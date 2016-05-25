package com.baofeng.test;

import com.baofeng.R;
import com.baofeng.aone.shutdown.ShutdownManager;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestShutdownActivity extends Activity implements OnClickListener{

    private Button mBtnShutdown;
    private ShutdownManager mShutdownManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shutdown_test);

        mShutdownManager = (ShutdownManager) ShutdownManager.getAndroidManager();
        mBtnShutdown = (Button) findViewById(R.id.shut_down);
        mBtnShutdown.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        mShutdownManager.shutdown();
    }
}
