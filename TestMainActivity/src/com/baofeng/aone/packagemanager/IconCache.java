package com.baofeng.aone.packagemanager;

import com.baofeng.aone.packagemanager.utils.Utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;

public class IconCache {

    private static LruCache<String, IconData> mLruCache;

    private PackageManager mPackageManager;

    public IconCache(Context context, PackageManager manager) {
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 16);
        mLruCache = new LruCache<>(maxSize);
        mPackageManager = manager;
    }

    public IconData getIconData(String packageName) {
        IconData data = null;
        if (mLruCache.get(packageName) != null) {
            data = mLruCache.get(packageName);
        } else {
            Drawable icon = Utils.getIconDrawableFromPackageName(packageName,
                    mPackageManager);
            byte[] iconArray = Utils.drawableToByte(icon);
            data = new IconData(iconArray);
            mLruCache.put(packageName, data);
        }
        return data;
    }

    public void remove(String pkgName) {
        mLruCache.remove(pkgName);
    }

}
