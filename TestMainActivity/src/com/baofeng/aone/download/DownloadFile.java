package com.baofeng.aone.download;

public class DownloadFile {

    private int idx;
    private String url;
    private String mimeType;
    private String displayName;
    private long lastModified;
    private int status;
    private long totalSize;
    private long currentSize;
    private String filePath;

    public DownloadFile(int id, String url, String type, String name,
            long time, int status, long total, long current, String path) {
        this.idx = id;
        this.url = url;
        this.mimeType = type;
        this.displayName = name;
        this.lastModified = time;
        this.status = status;
        this.totalSize = total;
        this.currentSize = current;
        this.filePath = path;
    }

    // private int icon;

    public long getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

}
