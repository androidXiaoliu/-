package com.baofeng.aone.packagemanager.utils;

import java.io.ByteArrayOutputStream;
import java.util.List;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.baofeng.aone.packagemanager.PackageItem;

public class Utils {

    private static final String TAG = Application.class.getName() + "Utils";

    public static boolean isLoadAllAppOnlyString = true;

    private static Canvas mCanvas = new Canvas();

    public static byte[] drawableToByte(Drawable drawable) {
        byte[] size = null;

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);

        final Canvas canvas = mCanvas;
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        size = baos.toByteArray();
        return size;
    }

    public static PackageItem getPackageItemFromPackageName(String packageName,
            PackageManager packageManager) {
        PackageItem item = null;
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(packageName, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info == null) {
            Log.d(TAG, "install apk packinfo is null");
            return null;
        }
   //     String className = info.applicationInfo.className;
        String className = getLauncherClassByPackageName(packageName,packageManager);
        String appName = (String) info.applicationInfo
                .loadLabel(packageManager);
        int flag = info.applicationInfo.flags;
        // IconData data = new IconData(drawableToByte(info.applicationInfo
        // .loadIcon(packageManager)));
        // item = new PackageItem(packageName, appName, className, data, flag);
        item = new PackageItem(packageName, appName, className, flag);
        return item;
    }

    public static Drawable getIconDrawableFromPackageName(String packageName,
            PackageManager manager) {
        Drawable drawable = null;
    //    PackageInfo info = null;
        try {
           // info = manager.getPackageInfo(packageName, 0);
            drawable = manager.getApplicationIcon(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        if (info == null) {
//            Log.d(TAG, "install apk packinfo is null");
//            return null;
//        }
//        drawable = info.applicationInfo.loadIcon(manager);
        return drawable;

    }

    private static String getLauncherClassByPackageName(String pkg, PackageManager manager) {
        String className = null;
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN,null);
        resolveIntent.setPackage(pkg);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveinfoList = manager
                .queryIntentActivities(resolveIntent, 0);

        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            className = resolveinfo.activityInfo.name;
        }
        return className;
    }

}
