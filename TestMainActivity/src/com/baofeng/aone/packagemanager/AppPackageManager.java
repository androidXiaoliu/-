//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.baofeng.aone.packagemanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
//import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baofeng.aone.AndroidCallback;
import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.packagemanager.AppPackageChangeObserver;
import com.baofeng.aone.packagemanager.callback.IconCallback;
import com.baofeng.aone.packagemanager.callback.PackageChangeCallback;
import com.baofeng.aone.packagemanager.callback.PackageListCallback;
import com.baofeng.aone.packagemanager.callback.packageOperationCallback;
import com.baofeng.aone.packagemanager.utils.Utils;
import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppPackageManager extends AndroidManager  {
    private static final String TAG = "AppPackageManager";
    private static final int POST_LAUNCH_IDLE_TIMEOUT = 750;
    PackageManager mPackageManager;
    ActivityManager mActivitymanager;
    Context mContext;

    private List<PackageItem> mInstalledAppList = new ArrayList<>();
    private static final int SEND_CALLBACK_MSG = 0;
    private static final int SEND_STOP_MSG = 1;
    private AppListThread mThread;

    Gson mGson = new Gson();
    private IconCache mIconCache;
    private static AppPackageManager mAppPackageManager;
    private AppChangeReceiver mReceiver;

    private AppPackageManager(Context mContext) {
        this.mContext = mContext;
        this.mPackageManager = mContext.getPackageManager();
        this.mActivitymanager = (ActivityManager) mContext
                .getSystemService("activity");

        mIconCache = new IconCache(mContext, mPackageManager);
        if (mReceiver == null) {
            mReceiver = new AppChangeReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mReceiver, filter);
    }

    public static AppPackageManager getAndroidManager() {
        return getAppPackageManager(LauncherApplication.getInstance());
    }

    private synchronized static AppPackageManager getAppPackageManager(
            Context context) {
        if (mAppPackageManager == null) {
            mAppPackageManager = new AppPackageManager(context);
        }
        return mAppPackageManager;
    }

    public void registerAppChangeCallback(PackageChangeCallback callback) {
        if (mReceiver != null) {
            mReceiver.registerCallback(callback);
        }
    }

    public void unregisterAppChangeCallback() {
        if (mReceiver != null) {
            mReceiver.unregisterCallback();
        }
    }

    public boolean installPackage(String filePath,
            packageOperationCallback callback) {
        AppPackageChangeObserver appPackageChangeObserver = new AppPackageChangeObserver(
                callback);
        Log.e("AppPackageManager", "installPackage filePath : " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            PackageInfo info = this.mPackageManager.getPackageArchiveInfo(
                    filePath, 1);
            if (info != null) {
                ApplicationInfo mAppInfo = info.applicationInfo;
                mAppInfo.sourceDir = filePath;
                mAppInfo.publicSourceDir = filePath;
                int installFlags = 0;

                try {
                    PackageInfo e = this.mPackageManager.getPackageInfo(
                            mAppInfo.packageName, 8192);
                    if (e != null) {
                        installFlags |= 2;
                        Log.w("AppPackageManager", "Replacing package:"
                                + mAppInfo.packageName);
                    }
                } catch (NameNotFoundException var8) {
                    ;
                }

                try {
//                	ReflectUtil.invorkMethod(PackageManager.class, this.mPackageManager, "installPackage", Uri.fromFile(file),
//                            appPackageChangeObserver.installObserver,
//                            installFlags, mAppInfo.packageName);
//                    this.mPackageManager.installPackage(Uri.fromFile(file),
//                            appPackageChangeObserver.installObserver,
//                            installFlags, mAppInfo.packageName);
                    return true;
                } catch (Exception var7) {
                    Log.e("AppPackageManager",
                            "installPackage application fail: "
                                    + var7.getMessage());
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public void unInstallPackage(String packageName,
            packageOperationCallback callback) {
        AppPackageChangeObserver appPackageChangeObserver = new AppPackageChangeObserver(
                callback);
        Log.e("AppPackageManager", "unInstallPackage filePath : " + packageName);
//        this.mPackageManager
//                .deletePackage(
//                        packageName,
//                        (IPackageDeleteObserver) appPackageChangeObserver.deleteObserver,
//                        0);
    }

    class AppListThread extends Thread {

        private boolean stop = false;
        public PackageListCallback callback = null;

        public void onStop(boolean isStop) {
            stop = isStop;
        }

        public boolean isStop() {
            return stop;
        }

        public void run() {
            List<ResolveInfo> infos = getFileterInfos();
            getInstalledAppList(infos, callback);
        }
    }

    public void getInstalledAppList(PackageListCallback callback) {
        if (mInstalledAppList.size() > 0) {
            String msg = mGson.toJson(mInstalledAppList);
            if (callback != null) {
                callback.onPackageListResult(msg);
            }
            return;
        }
        if (mThread == null) {
            mThread = new AppListThread();
        }
        mThread.callback = callback;
        mThread.start();
    }

    private List<ResolveInfo> getFileterInfos() {
        List<ResolveInfo> infos = null;
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final List<ResolveInfo> resolveInfos = mPackageManager
                .queryIntentActivities(intent,
                        PackageManager.GET_INTENT_FILTERS);
        Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(
                mPackageManager));
        infos = resolveInfos;

        return infos;
    }

    public void getAppIconFromPackageName(String name, IconCallback iconCallback) {
        IconData data = mIconCache.getIconData(name);
        if (iconCallback != null) {
            iconCallback.onApplicationIconBytes(data);
        }
    }

    private void getInstalledAppList(final List<ResolveInfo> resolveInfos,
            PackageListCallback listCallback) {
        for (ResolveInfo reInfo : resolveInfos) {
            String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
            String packageName = reInfo.activityInfo.packageName; // 获得应用程序的包名
            String appLabel = (String) reInfo.loadLabel(mPackageManager); // 获得应用程序的Label
            // Drawable icon = reInfo.loadIcon(mPackageManager);
            int flag = reInfo.activityInfo.applicationInfo.flags;

            PackageItem item = new PackageItem(packageName, appLabel,
                    activityName, flag);
            mInstalledAppList.add(item);
            if (!Utils.isLoadAllAppOnlyString && listCallback != null) {
                String msg = mGson.toJson(item);
                listCallback.onPackageListResult(msg);
            }
        }
        if (Utils.isLoadAllAppOnlyString && listCallback != null) {
            String msg = mGson.toJson(mInstalledAppList);
            listCallback.onPackageListResult(msg);
        }
    }

    public void startActivity(String packageName, String className) {
        Log.d(TAG, "startActivity packageName : " + packageName
                + "  className :" + className);
        if (packageName != null && className != null) {
            Intent launchIntent = new Intent();
            launchIntent
                    .setComponent(new ComponentName(packageName, className));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mContext.startActivity(launchIntent);
            } catch (RuntimeException e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    class AppChangeReceiver extends BroadcastReceiver {

        private PackageChangeCallback callback;

        public void registerCallback(PackageChangeCallback callback) {
            this.callback = callback;
        }

        public void unregisterCallback() {
            this.callback = null;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String packageName = intent.getData().getSchemeSpecificPart();

                String action = intent.getAction();
                Log.d(TAG, "AppChangeReceiver onReceive action = " + action);
                if (action != null) {
                    if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                        PackageItem item = Utils.getPackageItemFromPackageName(
                                packageName, mPackageManager);
                        Drawable iconDrawable = Utils
                                .getIconDrawableFromPackageName(packageName,
                                        mPackageManager);
                        if (item != null) {
                            mInstalledAppList.add(item);
                            if (callback != null) {
                                String msgString = mGson.toJson(item);
                                callback.onInstallPackageInfo(msgString);
                            }
                        }

                    } else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                        PackageItem item = findPackageItemFromList(packageName);
                        if (item != null) {
                            mInstalledAppList.remove(item);
                            mIconCache.remove(packageName);
                            if (callback != null) {
                                String msgString = mGson.toJson(item);
                                callback.onUninstallPackageInfo(msgString);
                            }
                        }

                    } else if (action.equals(Intent.ACTION_PACKAGE_CHANGED)) {
                        PackageItem item = findPackageItemFromList(packageName);
                        if (item != null) {
                            mInstalledAppList.remove(item);
                            mIconCache.remove(packageName);

                            PackageItem updateItem = Utils
                                    .getPackageItemFromPackageName(packageName,
                                            mPackageManager);

                            mInstalledAppList.add(updateItem);
                            if (callback != null) {
                                String msgString = mGson.toJson(updateItem);
                                callback.onPackageUpdateInfo(msgString);
                            }
                        }

                    }
                }
            }
        }

    }

    private PackageItem findPackageItemFromList(String packageName) {
        PackageItem item = null;
        if (mInstalledAppList.size() > 0) {
            for (int i = 0; i < mInstalledAppList.size(); i++) {
                if (mInstalledAppList.get(i).getPackageName()
                        .equals(packageName)) {
                    item = mInstalledAppList.get(i);
                    return item;
                }
            }
        }
        return item;

    }

    /*
     * public void uninstallApp(String pkgName) { Uri uri = Uri.parse("package:"
     * + pkgName); Intent intent = new Intent(Intent.ACTION_DELETE, uri);
     * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     * mContext.startActivity(intent); }
     */
}