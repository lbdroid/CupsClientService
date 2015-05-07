LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

ECHO_RESULT := $(shell ($(LOCAL_PATH)/jnaerator.sh $(LOCAL_PATH)))
LOCAL_MODULE    := jnidispatch
LOCAL_SRC_FILES := libjnidispatch.so
LOCAL_SHARED_LIBRARIES := ml.rabidbeaver.jna

include $(PREBUILT_SHARED_LIBRARY)