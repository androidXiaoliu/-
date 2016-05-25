package com.baofeng.aone.recenttask;

public class TaskInfo {
    
        private int mPersistentId;
        private String mAppName;
        private String mPackageName;

        public String getPackageName() {
            return mPackageName;
        }
        public void setPackageName(String mPackageName) {
            this.mPackageName = mPackageName;
        }

        public int getPersistentId() {
            return mPersistentId;
        }
        public void setPersistentId(int persistentId) {
            this.mPersistentId = persistentId;
        }

        public String getAppName() {
            return mAppName;
        }
        public void setAppName(String appName) {
            this.mAppName = appName;
        }
}
