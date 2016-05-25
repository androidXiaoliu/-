package com.baofeng.aone.filemanager.filebrowser.operation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baofeng.aone.filemanager.bean.FileClipBoard;
import com.baofeng.aone.filemanager.filebrowser.FileManagerCallback;
import com.baofeng.aone.filemanager.utils.Constant;
import com.baofeng.aone.filemanager.utils.FileChangedNotifier;
import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.utils.ResultUtils;

import android.content.Context;

public class FileMan {
    private static final String TAG = "FileMan";

    private FileClipBoard mfcpSelected = FileClipBoard.getInstance();

    boolean mCutType;

    boolean mbDismissInWait = true;
    private Context mContext;
    private DeleteThread mDeleteThread;
    private FileManagerCallback mCallback;

    public FileMan(Context context, FileManagerCallback callback) {
        mContext = context;
        mCallback = callback;
    }

    private class DeleteThread extends Thread {

        List<File> mSrcFileList = null;
        String deleteFilePath = null;

        protected void delete(File file) {
            // Log.i(TAG, " == > delete  file");

            if (file.isDirectory()) {
                File[] childs = file.listFiles();

                if (null == childs) {
                    Log.e(TAG, "list files of the dir failed in delete()");
                    return;
                } else {
                    if (0 == childs.length) {
                        // empty directory, delete it now
                        file.delete();
                    } else if (0 < childs.length) {
                        // the directory is not empty, clean it before delete.
                        for (int i = 0; i < childs.length; i++) {
                            if (!childs[i].exists()) {
                                Log.i(TAG, "stop when deleting");
                                break;
                            }
                            delete(childs[i]);
                        }

                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            } else {
                // delete file directly
                if (file.exists()) {
                    file.delete();
                    // notify file changed after delete
                    FileChangedNotifier.notifyFileChanged(mContext, file,
                            FileChangedNotifier.OP_TYPE_DELETE);

                }
            }
        }

        public int getFileCount(File file) {
            int nFileCount = 0;
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                // if the directory is empty, delete it directly.
                if (files.length > 0) {
                    // if the directory is not empty,empty it before delete.
                    int i;
                    for (i = 0; i < files.length; i++) {
                        nFileCount += getFileCount(files[i]);
                    }
                }

                return nFileCount;
            } else {
                return 1;
            }
        }

        public int getFileCount(final List<File> fileList) {
            int nFileCount = 0;
            for (int i = 0; i < fileList.size(); i++) {
                nFileCount += getFileCount(fileList.get(i));
            }
            return nFileCount;
        }

        public void run() {
            Log.i(TAG, "==> enter delete thread");

            FileChangedNotifier.notifyOpStart(mContext);

            int nTotalFileCount = getFileCount(mSrcFileList);

            if (1 < nTotalFileCount) {
                // onShowProgress(nTotalFileCount);
                Log.d(TAG, "nTotalFileCount is " + nTotalFileCount);
            }

            for (File file : mSrcFileList) {
                if (!file.exists())
                    break;
                delete(file);

                if (!file.exists()) {
                    // remove the file in clip
                    FileMan.this.mfcpSelected.removeFile(file);
                }
            }

            // ok2browser();
            // call success;

            FileChangedNotifier.notifyOpEnd(mContext);
            Log.i(TAG, "==> leave delete thread");
            mCallback.onDeleteFileSuccess(mDeleteThread.deleteFilePath);
        }
    }

    public void doDelete(int op, int srcfrom, String filePath) {
        Log.i(TAG, "==> doDelete");
        List<File> source = new ArrayList<File>();
        if (Constant.FROM_PATH == srcfrom) {
            File file = new File(filePath);
            if (file.exists())
                source.add(file);
        } else if (Constant.FROM_CLIP == srcfrom) {
            if (mfcpSelected.hasValidData()
                    && mfcpSelected.getOperation() == FileClipBoard.OP_DELETE) {
                source.addAll(mfcpSelected.getItemsInList());
                Log.i(TAG,
                        "the item number in clip is "
                                + String.valueOf(source.size()));
            }
        }

        if (null != source) {
            Log.i(TAG, "item number is " + String.valueOf(source.size()));
            deleteInThread(source, filePath); // [improve] ArrayList / List
        } else
            Log.d(TAG, "source is null");
    }

    /*
     * Delete a file or directory. If the directory is not empty, clear it
     * before delete.
     * 
     * @param file - the file/directory to be deleted
     */

    public void deleteInThread(final List<File> delFiles, String path) {
        Log.i(TAG, "==> deleteInThread");

        if (null == delFiles || 0 == delFiles.size()) {
            Log.i(TAG, "==> delFiles is null");
            mCallback.onDeleteFileFail(path,
                    ResultUtils.OPERATION_ERRO_FILE_NOT_EXIST);
        }

        // start the delete thread
        mDeleteThread = new DeleteThread();
        mDeleteThread.mSrcFileList = delFiles;
        mDeleteThread.deleteFilePath = path;
        mDeleteThread.start();
    }

}
