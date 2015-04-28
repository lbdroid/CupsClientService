LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := cups_shim
LOCAL_SRC_FILES := shim.c
LOCAL_SHARED_LIBRARIES := cups

include $(BUILD_SHARED_LIBRARY)