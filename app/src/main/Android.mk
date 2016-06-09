LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := iWediaSimpleTvInputService

LOCAL_SRC_FILES := $(call all-java-files-under, java)
LOCAL_SRC_FILES += $(call all-Iaidl-files-under, java)

LOCAL_STATIC_JAVA_LIBRARIES := \
    com.iwedia.dtv.framework.service

LOCAL_JAVA_LIBRARIES := \
    com.iwedia.comedia.comm

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_DEX_PREOPT := false

LOCAL_MODULE_TAGS := optional

include $(BUILD_PACKAGE)
