package com.baofeng.aone.filemanager.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

class FileInfo extends Object {
    private static final String TAG = "FileInfo";

    private class InfoItem extends Object {
        private static final String TAG = "InfoItem";

        private long miStamp = 0;
        private Object moInfo = null;

        public InfoItem(Object value, long stamp) {
            if (null == value || stamp < 0) {
                Log.e(TAG, "invalid parameter in InfoItem");
                return;
            }

            moInfo = value;
            miStamp = stamp;
        }

        public void updateInfoWithStamp(Object value, long stamp) {
            if (isOverdueForStamp(stamp)) {
                if (null == value) {
                    Log.e(TAG, "value is null in updateInfo");
                } else {
                    moInfo = value;
                    miStamp = stamp;
                }
            }
        }

        public boolean isOverdueForStamp(long stamp) {
            return (stamp != miStamp ? true : false);
        }

        public Object getInfoWithStamp(long stamp) {
            if (isOverdueForStamp(stamp)) {
                Log.e(TAG, "the info is overdue");
                return null;
            } else
                return moInfo;
        }

        public void dump() {
            if (null != moInfo)
                Log.i(TAG, "[Info] = " + moInfo.toString() + " [Stamp] = "
                        + miStamp);
            else
                Log.i(TAG, "No info");
        }
    }

    private File mFile = null;
    private HashMap<String, InfoItem> mmapInfo = null;

    public FileInfo(File file) {
        this(file, null, null);
    }

    public File getFile() {
        return mFile;
    }

    public FileInfo(File file, String key, Object value) {
        if (null == file || !file.exists()) {
            Log.e(TAG, "invalid file in FileInfo");
        } else {
            mFile = file;
            if (null == mmapInfo) {
                mmapInfo = new HashMap<String, InfoItem>();
            } else {
                mmapInfo.clear();
            }

            if (null != key && 0 < key.length() && null != value) {
                mmapInfo.put(key, new InfoItem(value, file.lastModified()));
            }
        }
    }

    public void addInfo(String key, Object value) {
        if (null == key || 0 == key.length() || null == value) {
            Log.e(TAG, "invalid parameter in addInfo");
            return;
        } else {
            if (!mFile.exists()) {
                Log.e(TAG, "the file is nonexistent");
            } else {
                InfoItem ii = null;

                if (null == mmapInfo) {
                    Log.e(TAG, "mmapInfo is null");
                    mmapInfo = new HashMap<String, InfoItem>();
                    ii = new InfoItem(value, mFile.lastModified());
                    mmapInfo.put(key, ii);
                } else {
                    if (mmapInfo.containsKey(key)) {
                        ii = mmapInfo.get(key);
                        ii.updateInfoWithStamp(value, mFile.lastModified());
                    } else {
                        ii = new InfoItem(value, mFile.lastModified());
                        mmapInfo.put(key, ii);
                    }
                }
            }
        }
    }

    public Object getInfo(String key) {
        if (null == key || 0 == key.length()) {
            Log.e(TAG, "key is empty or null in getInfo");
            return null;
        } else {
            if (null == mmapInfo)
                return null;
            else {
                if (!mmapInfo.containsKey(key)) {
                    return null;
                } else {
                    InfoItem ii = mmapInfo.get(key);
                    if (ii.isOverdueForStamp(mFile.lastModified())) {
                        Log.i(TAG, "the info [ " + key + " ] is overdue");
                        mmapInfo.remove(key);
                        return null;
                    } else
                        return ii.getInfoWithStamp(mFile.lastModified());
                }
            }
        }
    }

    public void removeInfo(String key) {
        if (null == key || 0 == key.length()) {
            Log.e(TAG, "key is empty or null in removeInfo");
            return;
        } else {
            if (null == mmapInfo)
                return;
            else {
                if (!mmapInfo.containsKey(key)) {
                    return;
                } else {
                    InfoItem ii = mmapInfo.get(key);
                    if (null == ii) {
                        return;
                    } else {
                        mmapInfo.remove(key);
                    }
                }
            }
        }
    }

    public boolean isEmpty() {
        if (null == mmapInfo)
            return true;
        else {
            return mmapInfo.isEmpty();
        }
    }

    public void dump() {
        Log.i(TAG, "## the info of the file [ " + mFile.getAbsolutePath()
                + " ]");

        if (null == mmapInfo || mmapInfo.isEmpty()) {
            Log.i(TAG, "there is no info");
        } else {
            Iterator<String> iterator = mmapInfo.keySet().iterator();
            while (iterator.hasNext()) {
                InfoItem ii = mmapInfo.get(iterator.next());
                if (null != ii)
                    ii.dump();
                else
                    Log.i(TAG,
                            "get info failed at the key " + iterator.toString());
            }
        }

        Log.i(TAG, "#########");
    }
}

public class FileInfoCache extends Object {
    private static final String TAG = "FileInfoCache";

    public static final String INFO_KEY_SAMPLE_FILE = "sample_file_for_foler";
    public static final String INFO_KEY_THUMBNAIL_FILE = "thumbnail_file_of_image_file";
    public static final String INFO_KEY_IMAGE_FILE_FOR_DIR = "image_file_of_image_file";
    public static final String INFO_KEY_SEARCH_RESULT = "search_result_in_folder";
    public static final String INFO_KEY_FILE_AMOUNT_IN_FOLDER = "file_amount_in_folder";

    private static HashMap<File, FileInfo> shmCache = null;

    // private static FileInfoCache sInstance = new FileInfoCache();
    // private FileInfoCache() {
    // if (null == shmCache){
    // shmCache = new HashMap<File, FileInfo>();
    // }else
    // shmCache.clear();
    // }

    // public synchronized static FileInfoCache getInstance() {
    // return sInstance;
    // }

    // private static void initialize(){
    // if (null == shmCache)
    // shmCache = new HashMap<File, FileInfo>();
    //
    // shmCache.clear();
    // }

    public static Object getFileInfo(File file, String key) {
        if (null == file || null == key || 0 == key.length()) {
            Log.e(TAG, "invalid parameter in getFileInfo");
            return null;
        }

        if (null == shmCache || shmCache.isEmpty()) {
            Log.i(TAG, "the cache is empty");
            return null;
        } else {
            FileInfo fi = shmCache.get(file);
            if (null == fi) {
                Log.i(TAG, "no FileInfo in cache");
                return null;
            } else {
                if (!file.exists()) {
                    shmCache.remove(file);
                    return null;
                } else
                    return fi.getInfo(key);
            }
        }
    }

    public static void addFileInfo(File file, String key, Object value) {
        if (null == file || !file.exists()) {
            Log.e(TAG, "file is invalid in addFileInfo");
            return;
        }

        if (null == key || 0 == key.length()) {
            Log.e(TAG, "key is empty in addFileInfo");
            return;
        }

        if (null == value) {
            Log.e(TAG, "value is null in addFileInfo");
            return;
        }

        if (null == shmCache) {
            shmCache = new HashMap<File, FileInfo>();
            shmCache.put(file, new FileInfo(file, key, value));
        } else {
            FileInfo fi = shmCache.get(file);
            if (null == fi) {
                shmCache.put(file, new FileInfo(file, key, value));
            } else {
                fi.addInfo(key, value);
            }
        }
    }

    public static void removeFileInfo(File file, String key) {
        if (null == file || null == key || 0 == key.length()) {
            Log.e(TAG, "invalid parameter in removeFileInfo");
            return;
        }

        if (null == shmCache) {
            return;
        } else {
            FileInfo fi = shmCache.get(file);
            if (null == fi) {
                return;
            } else {
                if (null == fi.getInfo(key)) {
                    return;
                } else {
                    fi.removeInfo(key);
                    if (fi.isEmpty())
                        shmCache.remove(file);
                }
            }
        }
    }

    public static void removeFileInfo(File file) {
        if (null == file) {
            Log.e(TAG, "invalid parameter in removeFileInfo");
            return;
        }

        if (null == shmCache) {
            return;
        } else {
            FileInfo fi = shmCache.get(file);
            if (null == fi) {
                return;
            } else {
                shmCache.remove(file);
            }
        }
    }

    public void dump() {
        Log.i(TAG,
                "====================  dump the FileInfoCache  ===================");

        if (null == shmCache || shmCache.isEmpty()) {
            Log.i(TAG, "there is no file info in cache");
        } else {
            Iterator<File> iterator = shmCache.keySet().iterator();
            while (iterator.hasNext()) {
                FileInfo fi = shmCache.get(iterator.next());
                if (null != fi)
                    fi.dump();
                else
                    Log.i(TAG,
                            "get info failed at the key " + iterator.toString());
            }
        }

        Log.i(TAG,
                "================================================================");
    }

}