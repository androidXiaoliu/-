LOCAL_PATH:= $(call my-dir)


include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := gson\
      android-support
# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src)\
        src/com/baofeng/fota/IOTAService.aidl

LOCAL_PACKAGE_NAME := TestAndroidForUnity
LOCAL_PRIVILEGED_MODULE := true
LOCAL_DEX_PREOPT := false
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := json\
      android-support
# Only compile source java files in this apk.
LOCAL_SRC_FILES := $(call all-java-files-under, src/com/baofeng/aone)\
        $(call all-java-files-under, src/com/android)\
        src/com/baofeng/fota/IOTAService.aidl

LOCAL_MODULE := AndroidForUnity
LOCAL_PRIVILEGED_MODULE := true
LOCAL_DEX_PREOPT := false
LOCAL_CERTIFICATE := platform
include $(BUILD_STATIC_JAVA_LIBRARY)

#LOCAL_SDK_VERSION := current
LOCAL_PROGUARD_ENABLED := disabled
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := gson:libs/gson-2.3.1.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += android-support:libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)
