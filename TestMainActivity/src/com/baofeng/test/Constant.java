package com.baofeng.test;

public class Constant {

    /* DispalyManager's short name */
    public static final String DISPLAY_MANAGER = "com.baofeng.aone.display.DisplayManager";

    /* use enum to show the methods in the calss DisplayManage */
    public enum DisplayManagerMethods {
        void_setDisplayValue_string,
        string_getDisplayValue_void
    }

    /* VolumeManager's short name */
    public static final String VOLUME_MANAGER = "com.baofeng.aone.volume.VolumeManager";
    /* use enum to show the methods in the calss VolumeManager */
    public enum VolumeManagerMethods{
            void_setVolumeChange_string,
            strings_getVolumeValue_void
    };

    /* ResourceManager's short name */
    public static final String RESOURCE_MANAGER = "com.baofeng.aone.volley.ResourceManager";

    /* use enum to show the methods in the ResourceManager class */
    public enum ResourceManagerMethods {
        void_imageloaderRequestVolley_string_ResourceCallback,
        void_stringRequestVolley_string_ResourceCallback,
        void_imageRequestVolley_string_ResourceCallback,
        void_stringRequestVolley_string_Hashmap_ResourceCallback

    }

    /*DownloadTools */
    public static final String DOWNLOAD_TOOLS = "com.baofeng.aone.download.DownloadTools";
    /*the methods enum */
    public enum DownloadToolsMethods{
        void_startDownload_string_DownloadListener,
        void_pauseDownload_string,
        void_resumeDownload_string,
        void_cleanUpDownload_string,
        void_cancelDownloadTask_string,
        void_cancelAllDownloadTask_void,
        void_getDownloadFile_DownloadListCallback_string,
        void_getDownloadFileList_DownloadListCallback_string
    }

    /*BatteryManager*/
    public static final String BATTERY_MANAGER = "com.baofeng.aone.battery.BatteryManager";
    /*the methods in battery manager*/
    public enum BatteryManagerMethods{
        void_registerCallback_BatteryCallback
    }

    //AppPackageManager
    public static final String APP_PACKAGE_MANAGER = "com.baofeng.aone.packagemanager.AppPackageManager";
    //methods
    public enum AppPackageManagerMethods{
        void_getInstalledAppList_PackageListCallback,
        void_getAppIconFromPackageName_string_IconCallback,
        void_registerAppChangeCallback_PackageChangeCallback,
        void_unregisterAppChangeCallback_void,
        void_installPackage_string_packageOperationCallback,
        void_unInstallPackage_string_packageOperationCallback,
        void_startActivity_string_string,
    }

    public static final String FILE_MANAGER = "com.baofeng.aone.filemanager.filebrowser.FileManager";

    public enum FileManagerMethods {
        string_getExternalSdcardState_void,
        void_registerVolumeChangeCallback_VolumeChangeCallback,
        void_getFileList_string_FileManagerCallback,
        void_doDelete_string_FileManagerCallback,
        void_doOpenFile_string_FileManagerCallback,
        void_unregisterVolumeChangeCallback_void,
        string_getExternalSdcardPath_void
    }

    //MemoryManager
    public static final String MEMORY_MANAGER = "com.baofeng.aone.memory.MemoryManager";
    //the method in memory manager
    public enum MemoryManagerMethods{
        void_memoryClear_MemoryCallback
    }

    //RecentTaskManager
    public static final String RECENT_TASK_MANAGER = "com.baofeng.aone.recenttask.RecentTaskManager";
    //the method in recent task manager
    public enum RecentTaskManagerMethods {
        void_getRecentTaskList_RecentTaskCallback,
        void_remove_RecentTaskCallback_int,
        void_removeAll_RecentTaskCallback,
        void_getIconByPackage_IconCallback_string,
        void_launchApp_RecentTaskCallback_string
    }
    //WifiManager
    public static final String WIFI_MANAGER = "com.baofeng.aone.wifi.WifiManager";
    //methods
    public enum WifiManagerMethods{
        void_registSystemUICallback_WifiSystemUICallback,
        void_registWifiSettingsCallback_WifiSettingsCallback,
        void_unregistWifiSettingsCallback_void,
        boolean_isWifiOpen_void,
        void_openWifi_void,
        void_closeWifi_void,
        void_disconnectWifiAp_string,
        void_connectWifiAp_string,
        boolean_whetherConnected_string,
        void_addNetWork_string_string_string,
        void_removeNetWork_stirng,
        void_startScan_void
    }
    
  //DateManager
    public static final String DATE_MANAGER = "com.baofeng.aone.fota.DateManager";
    //the methods in date manager
    public enum DateManagerMethods{
        void_registerDateChangelistner_DateCallback,
        void_unregisterDateChangelistner_void,
        void_getDate_DateCallback,
        void_getTime_DateCallback
    }

    //OTAManager
    public static final String OTA_MANAGER = "com.baofeng.aone.fota.OTAManager";
    //the methods in ota manager
    public enum OTAManagerMethods{
        void_start_OTACallback,
        void_pause_OTACallback,
        void_resume_OTACallback,
        void_cancel_OTACallback,
        void_goToUpgradeMode_void,
        void_getOTAStatus_OTACallback
    }

    //User data Manager
    public static final String USER_DATA_MANAGER = "com.baofeng.aone.userdata.UserDataManager";
    //the methods in userdata manager
    public enum UserDataManagerMethods{
        void_query_UserDataCallback_string,
        void_update_UserDataCallback_string_string_string_string
    }

    //shut down manager
    public static final String SHUTDOWN_MANAGER = "com.baofeng.aone.shutdown.ShutdownManager";
    //the methods in shut down manager
    public enum ShutdownManagerMethods{
        void_shutdown_void
    }
    //BluetoothManager
    public static final String BLUETOOTH_MANAGER = "com.baofeng.aone.bluetooth.BluetoothManager";
    //methods
    public enum BluetoothManagerMethods{
        void_registBluetoothStateChangedCallback_BluetoothStateChangedCallback,
        void_registDiscoveryCallback_DiscoveryCallback,
        void_registDeviceStateChangedCallback_DeviceStateChangedCallback,
        void_unregistDeviceStateChangedCallback_void,
        void_unregistBluetoothStateChangedCallback_void,
        void_unregistDiscoveryCallback_void,
        string_isEnabled_void,
        string_enable_void,
        string_disable_void,
        string_startDiscovery_void,
        void_cancelDiscovery_void,
        void_pair_string,
        void_setPin_string_string,
        void_unpair_String,
        void_connect_String,
        void_disconnect_String,
        void_getBondedDevices_BondedDevicesCallback,
        string_getName_void,
        void_setName_string
    }
}
