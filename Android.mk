LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := tapis
LOCAL_SRC_FILES := $(call all-java-files-under, src)
#LOCAL_SRC_FILES += src/com/letv/dmr/IDmrService.aidl
LOCAL_PACKAGE_NAME := TVKanKan
#LOCAL_CERTIFICATE := platform
LOCAL_PROGUARD_ENABLED := full
LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := tapis:libs/TVapi.jar
include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
