/* 
 * Copyright 2007 Steven Osborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baofeng.aone.filemanager.bean;

import java.io.File;

import android.content.Context;

import com.baofeng.aone.filemanager.utils.FileUtil;
import com.baofeng.aone.filemanager.utils.Log;

/*
 *  the class used in file browser
 *  it is used by adapter to fill the item view 
 *  which will shown to user
 */
public class FileItem implements Comparable<FileItem> {
    private static final String TAG = "FileItem";

    // it will be remove
    // private static FileClipBoard mfcbMarkedFile =
    // FileClipBoard.getInstance();

    private String mName = "";
    private String mType = "";
    // private String mText = "";
    // private File mFile = null;
    private String mPath = "";
    private long mSize = 0;
    private long mTime = 0;

    // private boolean isCut = false;

    // new construction
    // public IconifiedText(File file){
    //
    // }

    // remove isSysDir
    public FileItem(File file) {
        mName = file.getName();
        mPath = file.getAbsolutePath();
        mSize = calcuateFileSize(file);
        mTime = file.lastModified();
        mType = "";

        // remove it
        /*
         * if (mfcbMarkedFile.hasIt(file)) { // if the file is selected and to
         * be cut, set is cut flag to ture. if (mfcbMarkedFile.getOperation() ==
         * FileClipBoard.OP_CUT) { Log.i(TAG, "set it cut flag");
         * setIsCut(true); } }
         */

    }

    // [improve]maybe remove
    public FileItem(File file, String type) {
        this(file);
        setType(type);
    }

    public void redirect(Context context, File file) {
        if (null == file || !file.exists()) {
            Log.i(TAG, "file is invalid in redirect");
            return;
        } else {
            mName = file.getName();
            mPath = file.getAbsolutePath();
            // mSize = file.length();
            mSize = calcuateFileSize(file);
            mTime = file.lastModified();
            mType = FileUtil.getFileTypeString(context, file);
            // isCut = false;
        }
    }

    private long calcuateFileSize(File file) {
        if (file.isFile() && !file.isHidden()) {
            mSize += file.length();
        } else if (file.isDirectory() && !file.isHidden()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                calcuateFileSize(files[i]);
            }
        }
        return mSize;
    }

    // [improve]check type
    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    /*
     * public void setIsCut(boolean iscut) { isCut = iscut; }
     * 
     * public boolean getIsCut() { return isCut; }
     */
    public long getTime() {
        return mTime;
    }

    public long getSize() {
        return mSize;
    }

    public String getPath() {
        return mPath;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        if (null == name || 0 == name.length())
            return;
        else
            mName = name;
    }

    // [improve] it is wrong
    // @Override
    public int compareTo(FileItem other) {
        if (mName != null)
            return mName.compareTo(other.getName());
        else
            throw new IllegalArgumentException();
    }
}
