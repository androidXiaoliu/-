package com.baofeng.aone.userdata;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.google.gson.Gson;

public class UserDataManager extends AndroidManager{

    private static UserDataManager mUserManager;
    //column name
    public static final String KEY_ROWID = "phone_number";
    public static final String KEY_SERIAL = "serial_number";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_TOKEN = "token";

    private Context mContext;
    private static final String SERAIL_NUMBER = Build.SERIAL;
    private static final String AUTHORITY = "com.baofeng.userprovider";
    private static final String DATABASE_TABLE = "userinfo";
    private Uri mUri = Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE);
    private Cursor mCursor;
    private Gson mGson;
    private ContentResolver mResolver;

    public static AndroidManager getAndroidManager() {
        return getInstance();
    }

    private static UserDataManager getInstance() {
        if (mUserManager == null) {
            mUserManager = new UserDataManager();
        }
        return mUserManager;
    }

    private UserDataManager() {
        mContext = LauncherApplication.getInstance();
        mResolver = mContext.getContentResolver();
    }

    public void query(UserDataCallback callback, String phoneNumber) {
        UserInfo info;
        if ("".equals(phoneNumber) || phoneNumber.isEmpty())
            return;
        mCursor = mResolver.query(mUri, null, KEY_ROWID + "=?", new String[]{phoneNumber}, null, null);
        if (mCursor != null && mCursor.getCount() > 0) {
            if (mCursor.moveToFirst()) {
                mGson = new Gson();
                info = new UserInfo();
                info.setPhoneNumber(mCursor.getString(mCursor.getColumnIndex(KEY_ROWID)));
                info.setSerailNumber(mCursor.getString(mCursor.getColumnIndex(KEY_SERIAL)));
                info.setUsername(mCursor.getString(mCursor.getColumnIndex(KEY_USERNAME)));
                info.setPassword(mCursor.getString(mCursor.getColumnIndex(KEY_PASSWORD)));
                info.setToken(mCursor.getString(mCursor.getColumnIndex(KEY_TOKEN)));
                String msg = mGson.toJson(info);
                callback.onQuery(msg);
            }
        } else {
            ContentValues values = new ContentValues();
            mGson = new Gson();
            info = new UserInfo(phoneNumber,SERAIL_NUMBER+System.currentTimeMillis());
            values.put(KEY_ROWID, info.getPhoneNumber());
            values.put(KEY_SERIAL, info.getSerailNumber());
            Uri uri = mResolver.insert(mUri, values);
            long rowId = ContentUris.parseId(uri);
            String msg;
            if (rowId > -1) {
                msg = mGson.toJson(info);
                callback.onQuery(msg);
            } else {
                msg = "";
                callback.onQuery(msg);
            }
        }
        if (mCursor != null)
            mCursor.close();
    }

    public void insert(UserDataCallback callback, String phoneNumber, String username, String password, String token){
        if ("".equals(phoneNumber) || phoneNumber.isEmpty())
            return;
        mCursor = mResolver.query(mUri, null, phoneNumber, null, null);
        ContentValues values = new ContentValues();
        if (mCursor != null && mCursor.getCount() > 0) {
            update(callback, phoneNumber, username, password, token);
        } else {
            values.put(KEY_ROWID, phoneNumber);
            values.put(KEY_SERIAL, SERAIL_NUMBER + System.currentTimeMillis());
            values.put(KEY_USERNAME, username);
            values.put(KEY_PASSWORD, password);
            values.put(KEY_TOKEN, token);
            Uri uri = mResolver.insert(mUri, values);
            long id = ContentUris.parseId(uri);
            callback.onInsert(id > 0 ? "true" : "false");
        }
        if (mCursor != null)
            mCursor.close();
    }

    public void update(UserDataCallback callback, String phoneNumber, String username, String password, String token) {
        if ("".equals(phoneNumber) || phoneNumber.isEmpty())
            return;
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_TOKEN, token);
        int rows = mResolver.update(mUri, values, KEY_ROWID + "=?", new String[]{phoneNumber});
        callback.onUpdate(rows > 0 ? "true" : "false");
    }
}
