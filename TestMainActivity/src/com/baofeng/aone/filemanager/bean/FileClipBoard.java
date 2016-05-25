package com.baofeng.aone.filemanager.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.baofeng.aone.filemanager.utils.Log;
import com.baofeng.aone.filemanager.utils.MenuManager;

public class FileClipBoard {
    private static final String TAG = "FileClipBoard";

    public final static int OP_NONE = 0;
    public final static int OP_COPY = 1;
    public final static int OP_CUT = 2;
    public final static int OP_DELETE = 3;
    public final static int OP_COMPRESS = 4;

    public interface OnValidChangeListener {
        boolean onValidChanged(boolean isValid);
    }

    private static FileClipBoard instance = new FileClipBoard();

    private static List<File> mltFiles;
    private static int mOperation;
    private static boolean mbIsValid = false;
    private static OnValidChangeListener mOnValidChangeListener = null;

    private static File mfSelected = null;

    private void updateValidStatus() {
        if (hasValidData() && !mbIsValid) {
            mbIsValid = true;
            if (null != mOnValidChangeListener)
                mOnValidChangeListener.onValidChanged(true);
        } else if (!hasValidData() && mbIsValid) {
            mbIsValid = false;
            if (null != mOnValidChangeListener)
                mOnValidChangeListener.onValidChanged(false);
        }
    }

    private FileClipBoard() {
        if (null == mltFiles) {
            mltFiles = new ArrayList<File>();
        }
        mltFiles.clear();
        mOperation = OP_NONE;
        mbIsValid = false;
    }

    public synchronized static FileClipBoard getInstance() {
        return instance;
    }

    public static void setOnValidChangeListener(OnValidChangeListener listener) {
        mOnValidChangeListener = listener;
    }

    public void clear() {
        if (null != mltFiles) {
            mltFiles.clear();
        }
        mOperation = OP_NONE;

        updateValidStatus();
    }

    public boolean hasIt(File file) {
        if (file != null) {
            // if ( file != null && file.exists()) {
            if (null != mltFiles) {
                return mltFiles.contains(file);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean addFile(File file) {
        if (file != null && file.exists()) {
            MenuManager.pastetag = true;
            if (hasIt(file)) {
                return true;
            } else {
                if (null != mltFiles) {
                    mltFiles.add(file);
                    updateValidStatus();
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean removeFile(File file) {
        Log.i(TAG, "==> removeFile in FileClipBoard");
        if (file != null) {
            // if (file != null && file.exists()) {
            if (hasIt(file)) {
                if (null != mltFiles) {
                    boolean res = mltFiles.remove(file);
                    if (res)
                        updateValidStatus();
                    return res;
                } else {
                    MenuManager.pastetag = false;
                    return false;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    /*
     * public boolean removeAll(ArrayList<File> files) { if ( null == files ||
     * files.size() == 0 ) { return false; }else { if (null == mltFiles) {
     * return false; } else { for (File file : files){ removeFile(file); }
     * updateValidStatus(); return true; } }
     * 
     * }
     */

    public boolean removeAll(List<File> files) {
        if (null == files || files.size() == 0) {
            return false;
        } else {
            if (null == mltFiles) {
                return false;
            } else {
                for (File file : files) {
                    removeFile(file);
                }
                updateValidStatus();
                return true;
            }
        }

    }

    public int getSize() {
        if (null != mltFiles) {
            return mltFiles.size();
        } else {
            return -1;
        }
    }

    public File getFile(int pos) {
        if (null != mltFiles) {
            if (pos < 0 || pos > mltFiles.size()) {
                return null;
            } else {
                return mltFiles.get(pos);
            }
        } else {
            return null;
        }
    }

    public File[] getItems() {
        if (null != mltFiles) {
            if (!isEmpty()) {
                return (File[]) mltFiles.toArray();
            } else
                return null;
        } else
            return null;
    }

    public List<File> getItemsInList() {
        if (null != mltFiles) {
            if (!isEmpty()) {
                return mltFiles;
            } else
                return null;
        } else
            return null;
    }

    public boolean isEmpty() {
        if (null != mltFiles) {
            if (0 == getSize())
                return true;
            else
                return false;
        } else
            return true;
    }

    public boolean setOperation(int op) {
        if (op < OP_NONE || op > OP_COMPRESS)
            return false;
        else {
            mOperation = op;
            updateValidStatus();
            return true;
        }
    }

    public int getOperation() {
        return mOperation;
    }

    public boolean hasValidData() {
        return (mOperation != OP_NONE && !isEmpty());
    }

    public boolean setSelected(File file) {
        if (null == file || !file.exists())
            return false;
        else {
            if (null == mfSelected) {
                mfSelected = file;
                return true;
            } else {
                if (mfSelected.getAbsolutePath().equals(file.getAbsolutePath()))
                    return false;
                else {
                    mfSelected = file;
                    return true;
                }
            }
        }
    }

    public File getSelected() {
        return mfSelected;
    }

    public void clearSelected() {
        mfSelected = null;
    }

}