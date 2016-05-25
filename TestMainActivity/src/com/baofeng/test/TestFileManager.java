package com.baofeng.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.baofeng.R;
import com.baofeng.aone.filemanager.bean.FileItem;
import com.baofeng.aone.filemanager.filebrowser.FileManager;
import com.baofeng.aone.filemanager.filebrowser.FileManagerCallback;
import com.baofeng.aone.filemanager.filebrowser.VolumeChangeCallback;
import com.baofeng.aone.filemanager.utils.ResUtils;
import com.baofeng.aone.filemanager.utils.ResultUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.TextView;

public class TestFileManager extends Activity implements OnItemClickListener,
        OnItemLongClickListener {

    private static final String TAG = "ymy";

    private FileManager manager;
    private FileCallback mCallback;
    private List<FileItem> mFileList = new ArrayList<>();
    private Gson mGson = new Gson();
    private FileAdapter mAdapter;
    private GridView mGridView;
    private TextView mEmptyView;
    private static int SHOW_FOLDER_LIST = 1;
    private static int SHOW_DELETE_DIALOG = 2;

    private Map<String, FileItem> mDeleteList = new HashMap<>();
    private int miStatus;
    // status
    private static final int STATUS_BROWSE_DISK = 0;
    private static final int STATUS_BROWSE_DIR = 1;
    private static final int STATUS_SHOW_WARNING = 2;
    private LinkedList<FileType> mLinkedList = new LinkedList<>();
    private boolean mIsBack = false;
    private VolumeCallback mVolumeCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_layout);
        mGridView = (GridView) findViewById(R.id.gview);
        mGridView.setOnItemLongClickListener(this);
        mGridView.setOnItemClickListener(this);
        mEmptyView = (TextView) findViewById(R.id.empty);
        mLinkedList.clear();

        manager = FileManager.getAndroidManager();
        mVolumeCallback = new VolumeCallback();
        mCallback = new FileCallback();
        manager.registerVolumeChangeCallback(mVolumeCallback);

        if(manager.getExternalSdcardState().equals(Environment.MEDIA_MOUNTED)) {
            manager.getFileList(null, mCallback);
        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == SHOW_FOLDER_LIST) {
                mAdapter = new FileAdapter(mFileList, getApplicationContext());
                mGridView.setAdapter(mAdapter);
            } else if (what == SHOW_DELETE_DIALOG) {
                FileItem item = (FileItem) msg.obj;
                confirmDeleteDialog(item);
            }
        }
    };

    private void confirmDeleteDialog(final FileItem item) {
        final String path = item.getPath();
        AlertDialog.Builder builder = new AlertDialog.Builder(
                TestFileManager.this);
        builder.setTitle("Are you sure to delete " + path);
        builder.setPositiveButton("ok", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mDeleteList.put(path, item);
                manager.doDelete(path, mCallback);
            }
        });
        builder.setNegativeButton("cancel", null);
        builder.show();
    }

    class FileCallback implements FileManagerCallback {

        @Override
        public void onBrowseFileSuccess(String path, String msg) {
            Log.d(TAG, "===onBrowseFileSuccess== path : " + path);
            if (!mIsBack) {
                if (path.length() > 17) {
                    miStatus = STATUS_BROWSE_DIR;
                    FileType fileType = new FileType(path, "folder");
                    if (!mLinkedList.contains(fileType)) {
                        mLinkedList.add(fileType);
                    }
                } else {
                    miStatus = STATUS_BROWSE_DISK;
                    FileType fileType = new FileType(path, "root");
                    if (!mLinkedList.contains(fileType)) {
                        mLinkedList.add(fileType);
                    }
                }
            } else {
                // mLinkedList.removeLast();
                mIsBack = false;
            }

            List<FileItem> list = mGson.fromJson(msg,
                    new TypeToken<List<FileItem>>() {
                    }.getType());
            mFileList = list;
            mHandler.sendEmptyMessage(SHOW_FOLDER_LIST);
        }

        @Override
        public void onBrowseFileFail(String path, int reason) {
            Log.d(TAG, "===onBrowseFileFail== reason is " + reason);
            miStatus = STATUS_SHOW_WARNING;
            if (reason == ResultUtils.BROWSE_ERROR_NO_VALID_STORAGE) {
                mGridView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onDeleteFileSuccess(String fileName) {
            Log.d(TAG, "===onDeleteFileSuccess== : " + fileName);
            FileItem deleteItem = mDeleteList.get(fileName);
            if (deleteItem != null) {
                mFileList.remove(deleteItem);
                mHandler.sendEmptyMessage(SHOW_FOLDER_LIST);
            }
        }

        @Override
        public void onDeleteFileFail(String filePath, int reason) {
            Log.d(TAG, "===onDeleteFileFail== : " + filePath + " reason "
                    + reason);
        }

        @Override
        public void onOpenFileFail(String path, int reason) {
            // TODO Auto-generated method stub
            Log.d(TAG, "===onOpenFileFail== " + path);
        }
    }

    class VolumeCallback implements VolumeChangeCallback {

        @Override
        public void onVolumeStateChange(String state) {
            Log.d("ymy", "onVolumeStateChange = " + state);
            if (state.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                manager.getFileList(null, mCallback);
            }
        }

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
            int position, long id) {
        FileItem item = (FileItem) mAdapter.getItem(position);
        Message message = new Message();
        message.obj = item;
        message.what = SHOW_DELETE_DIALOG;
        mHandler.sendMessage(message);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
        FileItem item = (FileItem) mAdapter.getItem(position);
        String path = item.getPath();
        if (item.getType().equals(ResUtils.filetype_folder)) {
            manager.getFileList(path, mCallback);
        } else {
            manager.doOpenFile(path, mCallback);
        }
    }

    @Override
    public void onBackPressed() {
        onBack();
    }

    private void onBack() {
        Log.d(TAG, "==> onBack");

        if (STATUS_SHOW_WARNING == miStatus) {
            Log.i(TAG, "quit app");
            quitApp();
        } else {
            mLinkedList.removeLast();
            if (mLinkedList.size() == 0) {
                Log.i(TAG, "list size is 0");
                quitApp();
            } else {
                FileType fileType = mLinkedList.getLast();
                if (fileType == null) {
                    Log.i(TAG, "1111===quit app");
                    quitApp();
                } else {
                    mIsBack = true;
                    String type = fileType.getType();
                    Log.d(TAG, "22222 path = " + type);
                    if (type.equals("root")) {
                        manager.getFileList(null, mCallback);
                    } else if (type.equals("folder")) {
                        manager.getFileList(fileType.getPath(), mCallback);
                    } else {
                        Log.d(TAG, "path is neither root nor folder .");
                    }

                }
            }
        }
    }

    private void quitApp() {
        Log.i(TAG, "==> quitApp");
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // DiskVolumeMgr.getInstance().stop();
        manager.unregisterVolumeChangeCallback();
    }

}
