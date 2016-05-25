package com.baofeng.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.baofeng.R;
import com.baofeng.aone.userdata.UserDataCallback;
import com.baofeng.aone.userdata.UserDataManager;
import com.baofeng.aone.userdata.UserInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TestUserProviderActivity extends Activity implements OnClickListener, UserDataCallback{

    private EditText mEidt;
    private TextView mQueryResult, mUpdateResult;
    private Button mQueryBtn, mUpdateBtn;
    private UserDataManager mManager;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.database_test);

        mManager = (UserDataManager) UserDataManager.getAndroidManager();
        mGson = new Gson();
        mEidt = (EditText) findViewById(R.id.phone_number);
        mQueryResult = (TextView) findViewById(R.id.query_result);
        mUpdateResult = (TextView) findViewById(R.id.update_result);
        mQueryBtn = (Button) findViewById(R.id.query);
        mUpdateBtn = (Button) findViewById(R.id.update);
        mQueryBtn.setOnClickListener(this);
        mUpdateBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.query:
            mManager.query(TestUserProviderActivity.this, mEidt.getText().toString());
            break;
        case R.id.update:
            mManager.update(TestUserProviderActivity.this, mEidt.getText().toString(), "baofeng", "root", null);
            break;
        }
    }

    @Override
    public void onQuery(String msg) {
        UserInfo info = mGson.fromJson(msg, new TypeToken<UserInfo>(){}.getType());
        mQueryResult.setText(info.getPhoneNumber() + " " + info.getSerailNumber() + " " + info.getUsername() + " " + info.getPassword() + info.getToken());
    }

    @Override
    public void onInsert(String result) {

    }

    @Override
    public void onUpdate(String result) {
        mUpdateResult.setText(result);
    }
}
