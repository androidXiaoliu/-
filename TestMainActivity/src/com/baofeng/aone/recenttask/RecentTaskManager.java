package com.baofeng.aone.recenttask;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.util.Log;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.packagemanager.IconCache;
import com.baofeng.aone.packagemanager.IconData;
import com.baofeng.aone.packagemanager.callback.IconCallback;
import com.google.gson.Gson;


public class RecentTaskManager extends AndroidManager{
    public static final String TAG = "RecentTaskManager";
    public static final boolean DEBUG = true;
    private static RecentTaskManager mManager;
    private List<RecentTaskInfo> mRecentTaskList;
    private ActivityManager mActManager;
    private PackageManager mPackageManager;
    private Context mContext;
    private IconCache mIconCache;
    private String[] mPackageWhiteList;
    private ActivityInfo mHomeInfo;

    private RecentTaskManager() {
        mContext = LauncherApplication.getInstance();
        mActManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = mContext.getPackageManager();
        mIconCache = new IconCache(mContext, mPackageManager);
        mHomeInfo = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .resolveActivityInfo(mPackageManager, 0);
        String homePackage = mHomeInfo.applicationInfo.packageName;
        mPackageWhiteList = new String[] {homePackage};
    }

    public static AndroidManager getAndroidManager() {
        return getInstance();
    }

    private static RecentTaskManager getInstance() {
        if (mManager == null) {
            mManager = new RecentTaskManager();
        }
        return mManager;
    }

    public void getRecentTaskList(RecentTaskCallback callback){
        loadThread load = new loadThread();
        load.callback = callback;
        load.start();
    }

    /**
     *  get app icon by package name
     *
     * @param callback IconCallback
     * @param packageName app package name
     */
    public void getIconByPackage (IconCallback callback, String packageName) {
        IconData data = mIconCache.getIconData(packageName);
        if (callback != null) {
            callback.onApplicationIconBytes(data);
        }
    }

    /**
     * A thread for get recent task list
     *
     */
    public class loadThread extends Thread {
        List<TaskInfo> taskInfoList = new ArrayList<TaskInfo>();
        TaskInfo taskInfo;
        Gson gson = new Gson();
        RecentTaskCallback callback;

        @SuppressLint("NewApi") @Override
        public void run() {
            super.run();
            mRecentTaskList = mActManager.getRecentTasks(21, 0);
            mRecentTaskList = getWhiteList(mRecentTaskList);
            for (RecentTaskInfo info : mRecentTaskList) {
                taskInfo = new TaskInfo();
                try {
                    taskInfo.setPackageName(info.baseIntent.getComponent().getPackageName());
                    taskInfo.setPersistentId(info.persistentId);
                    taskInfo.setAppName((String) mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(taskInfo.getPackageName(), 0)));
                    if (DEBUG) {
                        Log.d(TAG,"packageName = " + info.baseIntent.getComponent().getPackageName());
                        Log.d(TAG,"persistentId = " + info.persistentId);
                        Log.d(TAG,"appName = " + mPackageManager.getApplicationLabel(mPackageManager.getApplicationInfo(taskInfo.getPackageName(), 0)));
                    }
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                taskInfoList.add(taskInfo);
            }
            String msg = gson.toJson(taskInfoList);
            callback.onRecentTaskLoadCompleted(msg);
        }
    }
    /**
     * Completely remove the given task.
     * @param callback  RecentTaskCallback
     * @param persistentId
     */
    @SuppressLint("NewApi")
    public void remove(RecentTaskCallback callback, int persistentId) {
        if (persistentId <= -1) return;

        try {
        	ReflectUtil.invorkMethod(ActivityManager.class, mActManager, "removeTask", persistentId);

//            mActManager.removeTask(persistentId);
            for (RecentTaskInfo info : mRecentTaskList) {
                if (info.persistentId == persistentId) {
                    mRecentTaskList.remove(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.onRemove();
    }

    @SuppressLint("NewApi")
    public void removeAll(RecentTaskCallback callback) {
        if (mRecentTaskList == null) return;

        try {
            for (RecentTaskInfo info : mRecentTaskList) {
            	ReflectUtil.invorkMethod(ActivityManager.class, mActManager, "removeTask", info.persistentId);
//                mActManager.removeTask(info.persistentId);
            }
            mRecentTaskList.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
        callback.onRemoveAll();
    }

    private List<RecentTaskInfo> getWhiteList(List<RecentTaskInfo> list) {
        List<RecentTaskInfo> removeList = new ArrayList<ActivityManager.RecentTaskInfo>();
        for (RecentTaskInfo info : list) {
            String packageName = info.baseIntent.getComponent().getPackageName();
            for (int i = 0; i < mPackageWhiteList.length; i++) {
                if(packageName.equals(mPackageWhiteList[i])){
                    removeList.add(info);
                }
            }
        }
        list.removeAll(removeList);
        return list;
    }

    /**
     * using the recent task to start the application
     * @param callback
     * @param persistentId
     */
    public void launchApp (RecentTaskCallback callback, int persistentId) {
        String msg = "";
        boolean launchResult = true;
        for (RecentTaskInfo info : mRecentTaskList) {
            if (info.persistentId == persistentId) {
                //the task is running
                if (info.id > 0) {
                    mActManager.moveTaskToFront(info.id, ActivityManager.MOVE_TASK_WITH_HOME);
                } else {
                    Intent intent = new Intent(info.baseIntent);
                    intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                            | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                    	int userid = (int)ReflectUtil.getField(RecentTaskInfo.class, "userId",info);
                         Constructor constructor = ReflectUtil.Constructor("UserHandle", int.class);
                         UserHandle mUserHandler= (UserHandle)ReflectUtil.newInstance(constructor, userid);
                    	ReflectUtil.invorkMethod(Context.class, mContext, "startActivityAsUser", intent,mUserHandler);
//                        mContext.startActivityAsUser(intent, new UserHandle(info.userId));
                    } catch (SecurityException e) {
                        msg = "Recents does not have the permission to launch.";
                        launchResult = false;
                    } catch (ActivityNotFoundException e) {
                        msg = "Error launching activity, the activity not found";
                        launchResult = false;
                    }
                }
                break;
            }
        }
        callback.onLaunchAppCompleted(launchResult, msg);
    }
}
