package com.baofeng.aone.filemanager.volume;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.BaseColumns;

import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.volume.DiskVolumeMgr.OnDiskVolumeChangedListener;

public class DiskVolumeProvider implements IDiskVolume, LoaderCallbacks<Cursor> {
    private final static String TAG = "DiskVolumeProvider";

    public static final String VOLUME_AUTHORITIES_URI = "com.lenovo.nebula.provider.DiskVolumeContentProvider";

    public static final int VOLUME_TYPE_SDCARD = 0;
    public static final int VOLUME_TYPE_USB = 1;

    public final static int COL_INDEX_ID = 0;
    public final static int COL_INDEX_TYPE = 1;
    public final static int COL_INDEX_TITLE = 2;
    public final static int COL_INDEX_PATH = 3;

    protected static final int VOLUMES_CALLBACKS_ID = 0;
    private Context context = null;
    private OnDiskVolumeChangedListener listener = null;

    private static String[] PROJECTION = { VolumeColumns._ID, "volume_type",
            VolumeColumns.VOLUME_TITLE, VolumeColumns.VOLUME_PATH };

    public static final class VolumeColumns implements BaseColumns {

        public static final Uri VOLUMES_URI = Uri.parse(String.format(
                "content://%s/volumes", VOLUME_AUTHORITIES_URI));
        public static final String VOLUME_PATH = "volume_path";
        public static final String VOLUME_TITLE = "volume_title";
        public static final String VOLUME_TYPE = "volume_type";
        public static final String DEFAULT_SORT_ORDER = "diskindex";

    }

    public DiskVolumeProvider(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        queryVolumesAsync(null, null, null);
    }

    @Override
    public List<Storage> getVolumesByType(Context context, int type) {
        Cursor cursor = context.getContentResolver().query(
                VolumeColumns.VOLUMES_URI, PROJECTION, "volume_type=?",
                new String[] { String.valueOf(type) }, null);
        List<Storage> storages = new ArrayList<Storage>();
        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(DiskVolumeProvider.COL_INDEX_PATH);
            String title = cursor.getString(DiskVolumeProvider.COL_INDEX_TITLE);
            Log.i(TAG, "Add storage " + path + ";" + title);
            Storage storage = new Storage(new File(path),
                    Storage.STORAGE_TYPE_EXTERNAL_USB, title,
                    Environment.MEDIA_MOUNTED);
            storages.add(storage);
        }
        if (cursor != null) {
            cursor.close();
        }
        return storages;
    }

    @Override
    public String getVolumeTitle(Context context, String path) {
        String title = null;
        Cursor cursor = context.getContentResolver().query(
                VolumeColumns.VOLUMES_URI, PROJECTION,
                VolumeColumns.VOLUME_PATH + "=?", new String[] { path }, null);
        if (cursor != null && cursor.moveToNext()) {
            title = cursor.getString(COL_INDEX_TITLE);
        }
        if (cursor != null) {
            cursor.close();
        }
        return title;
    }

    public Cursor queryVolumesSync(String selection, String[] selectionArgs,
            String sortOrder) {
        Log.i(TAG, "--------------------->queryVolumesSync");
        return context.getContentResolver().query(VolumeColumns.VOLUMES_URI,
                PROJECTION, selection, selectionArgs, sortOrder);
    }

    public Loader<Cursor> queryVolumesAsync(String selection,
            String[] selectionArgs, String sortOrder) {
        Log.i(TAG, "--------------------->queryVolumesAsync");
        Bundle args = new Bundle();
        args.putString("selection", selection);
        args.putStringArray("selectionArgs", selectionArgs);
        args.putString("sortOrder", sortOrder);
        return ((Activity) context).getLoaderManager().initLoader(
                VOLUMES_CALLBACKS_ID, args, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "--------------------->onCreateLoader");
        CursorLoader cursorLoader = null;
        switch (id) {
        case VOLUMES_CALLBACKS_ID: {
            String selection = args.getString("selection");
            String[] selectionArgs = args.getStringArray("selectionArgs");
            String sortOrder = args.getString("sortOrder");

            cursorLoader = new CursorLoader(context, VolumeColumns.VOLUMES_URI,
                    PROJECTION, selection, selectionArgs, sortOrder);
            // cursorLoader.setUpdateThrottle(DEFAULT_THROTTLE_DELAY);
            break;
        }
        default:
            Log.w(TAG, String.format("Unsupported callbacks id : %d", id));
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i(TAG, "--------------------->onLoadFinished");
        List<Storage> storages = new ArrayList<Storage>();
        while (data != null && data.moveToNext()) {
            String path = data.getString(DiskVolumeProvider.COL_INDEX_PATH);
            String title = data.getString(DiskVolumeProvider.COL_INDEX_TITLE);
            int type = Integer.valueOf(data
                    .getString(DiskVolumeProvider.COL_INDEX_TYPE));
            Log.i(TAG, "Add storage " + path + ";" + title + ";" + type);
            Storage storage = new Storage(new File(path), type, title,
                    Environment.MEDIA_MOUNTED);
            storages.add(storage);
        }
        if (listener != null) {
            listener.onDiskVolumeChanged(storages);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i(TAG, "--------------------->onLoaderReset");
    }

    @Override
    public void setOnDiskVolumnChangedListener(
            OnDiskVolumeChangedListener listener) {
        this.listener = listener;
    }

    @Override
    public void stop() {
        this.listener = null;
    }

    @Override
    public List<Storage> getAllVolumes() {
        Cursor cursor = context.getContentResolver().query(
                VolumeColumns.VOLUMES_URI, PROJECTION, null, null, null);
        List<Storage> storages = new ArrayList<Storage>();
        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(DiskVolumeProvider.COL_INDEX_PATH);
            String title = cursor.getString(DiskVolumeProvider.COL_INDEX_TITLE);
            int type = cursor.getInt(DiskVolumeProvider.COL_INDEX_TYPE);
            Log.i(TAG, "Add storage " + path + " " + title + " " + type);
            Storage storage = new Storage(new File(path), type, title,
                    Environment.MEDIA_MOUNTED);
            storages.add(storage);
        }
        if (cursor != null) {
            cursor.close();
        }
        return storages;
    }

}
