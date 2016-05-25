package com.baofeng.test;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.baofeng.aone.packagemanager.AppPackageManager;
import com.baofeng.aone.packagemanager.IconData;
import com.baofeng.aone.packagemanager.PackageItem;
import com.baofeng.aone.packagemanager.callback.PackageChangeCallback;
import com.baofeng.aone.packagemanager.callback.PackageListCallback;
import com.baofeng.aone.packagemanager.callback.packageOperationCallback;
import com.baofeng.aone.packagemanager.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.baofeng.R;

public class TestPackageManager extends Activity implements
        OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = "ymy";
    private List<PackageItem> mInstalledAppList = new ArrayList<PackageItem>();
    GridView mGridView = null;
    // private List<PackageItem> mList;
    AppPackageManager manager;
    // BrowseApplicationInfoAdapter mAdapter;

    PackageAdapter mAdapter;
    private Gson mGson = new Gson();

    private static final int UPDATE_ICON_MSG = 0;
    private static final int GET_ICON_MSG = 1;
    private static final int GET_APP_LIST = 2;
    private static final int ADD_NEW_DATA = 3;

    // private int startId = 0;
    private String mStartName;
    private String mAddName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_grid_activity);
        mGridView = (GridView) findViewById(R.id.gridView);
        manager = AppPackageManager.getAndroidManager();
        manager.getInstalledAppList(new AppListCallback());
        manager.registerAppChangeCallback(new AppChangeCallback());
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnItemClickListener(this);
    }

    Handler handler = new Handler() {
        int startId = 0;

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_ICON_MSG) {
                IconData data = (IconData) msg.obj;
                if (mAddName != null) {
                    // mInstalledAppList.get(mInstalledAppList.size()-1).setData(data);
                    // mAdapter.setData(data, mAddName);
                    mAddName = null;
                    return;
                }
                PackageItem item = mInstalledAppList.get(startId);
                // item.setData(data);
                // mAdapter.setData(data, startId);

                if (startId < mInstalledAppList.size() - 1) {
                    startId++;
                    handler.sendEmptyMessage(GET_ICON_MSG);
                }
            } else if (msg.what == GET_ICON_MSG) {
                mStartName = mInstalledAppList.get(startId).getPackageName();
                // manager.getAppIconFromPackageName(mStartName,new
                // AppIconCallback());
            } else if (msg.what == GET_APP_LIST) {
                mAdapter = new PackageAdapter(mInstalledAppList,
                        getApplicationContext(), manager);
                mGridView.setAdapter(mAdapter);
                // startId = 0;
                // handler.sendEmptyMessage(GET_ICON_MSG);
            } else if (msg.what == ADD_NEW_DATA) {
                PackageItem item = (PackageItem) msg.obj;
                mAddName = item.getPackageName();
                // manager.getAppIconFromPackageName(mAddName, new
                // AppIconCallback());
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {

        String pak = ((PackageItem) mAdapter.getItem(position))
                .getPackageName();
        String cls = ((PackageItem) mAdapter.getItem(position))
                .getActivityname();
        manager.startActivity(pak, cls);
    }

    // class AppIconCallback implements IconCallback {
    //
    // @Override
    // public void onApplicationIconBytes(IconData data) {
    // handler.sendMessage(handler.obtainMessage(UPDATE_ICON_MSG,data));
    // }
    //
    // }

    class AppListCallback implements PackageListCallback {

        @Override
        public void onPackageListResult(String result) {

            Log.d(TAG, "=====onPackageListResult ====");
            Gson gson = new Gson();
            if (Utils.isLoadAllAppOnlyString) {
                java.lang.reflect.Type type = new TypeToken<List<PackageItem>>() {
                }.getType();
                List<PackageItem> items = gson.fromJson(result, type);
                mInstalledAppList.clear();
                // mInstalledAppList = items;
                mInstalledAppList.addAll(items);
                handler.sendMessage(handler.obtainMessage(GET_APP_LIST));

            } else {

                // PackageItem item = gson.fromJson(result, PackageItem.class);
                mGridView.setAdapter(mAdapter);
            }
        }
    }

    class AppChangeCallback implements PackageChangeCallback {

        @Override
        public void onInstallPackageInfo(String msg) {
            Log.d(TAG, "=====onInstallPackageInfo ==== " + msg);
            Gson gson = new Gson();

            PackageItem item = gson.fromJson(msg, PackageItem.class);

            mAdapter.addItem(item);
            // mGridView.setAdapter(mAdapter);
            mInstalledAppList.add(item);

//            Message message = new Message();
//            message.obj = item;
//            message.what = ADD_NEW_DATA;
//            handler.sendMessage(message);
        }

        @Override
        public void onUninstallPackageInfo(String msg) {
            Log.d(TAG, "=====onUninstallPackageInfo ====");
            Gson gson = new Gson();
            PackageItem item = gson.fromJson(msg, PackageItem.class);
            String packageName = item.getPackageName();
            mAdapter.removeData(packageName);
            mGridView.setAdapter(mAdapter);
            mInstalledAppList.remove(item);

        }

        @Override
        public void onPackageUpdateInfo(String msg) {
            Log.d(TAG, "==onPackageUpdateInfo==");
        }
    }

    class AppOperationCallback implements packageOperationCallback {

        @Override
        public void onInstallResult(int result) {
            Log.d(TAG, "==onInstallResult== result" + result);
        }

        @Override
        public void onUninstallResult(int result) {
            Log.d(TAG, "==onUninstallResult== result" + result);
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {

        String packageName = ((PackageItem) mAdapter.getItem(position))
                .getPackageName();
        manager.unInstallPackage(packageName, new AppOperationCallback());

        return true;
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "==onDestroy==");
        handler.removeCallbacksAndMessages(null);
        if (mInstalledAppList.size() > 0)
            mInstalledAppList.clear();

        super.onDestroy();
    }

}
