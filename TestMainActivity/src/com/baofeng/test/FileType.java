package com.baofeng.test;

public class FileType {

	private String mPath;
	private String mType;

	public FileType(String path, String type) {
		this.mPath = path;
		this.mType = type;
	}

	public String getPath() {
		return mPath;
	}
	public void setPath(String path) {
		this.mPath = path;
	}
	public String getType() {
		return mType;
	}
	public void setType(String type) {
		this.mType = type;
	}

}
