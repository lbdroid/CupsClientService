LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ECHO_RESULT := $(shell ($(LOCAL_PATH)/jnaerator.sh $(LOCAL_PATH)))
LOCAL_MODULE    := cups_shim
#LOCAL_SRC_FILES := shim.c
LOCAL_SHARED_LIBRARIES := cups

include $(BUILD_SHARED_LIBRARY)