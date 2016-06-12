LOCAL_PATH := $(call my-dir)

# BEGIN PREBUILT_SHARED_LIBRARY

include $(CLEAR_VARS)
LOCAL_MODULE:= libavcodec
LOCAL_SRC_FILES:= ../lib/libavcodec.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavformat
LOCAL_SRC_FILES:= ../lib/libavformat.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)   
LOCAL_MODULE:= libswscale
LOCAL_SRC_FILES:= ../lib/libswscale.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavutil
LOCAL_SRC_FILES:= ../lib/libavutil.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavfilter
LOCAL_SRC_FILES:= ../lib/libavfilter.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libwsresample
LOCAL_SRC_FILES:= ../lib/libswresample.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include
include $(PREBUILT_SHARED_LIBRARY)

# END PREBUILT_SHARED_LIBRARY


include $(CLEAR_VARS)

LOCAL_MODULE := libbasicplayer
LOCAL_SRC_FILES := BasicPlayer.cpp Interface.c
#LOCAL_SRC_FILES := Interface.c
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include/ \
                    $(LOCAL_PATH)/../include/libavcodec \
                    $(LOCAL_PATH)/../include/libavformat \
                    $(LOCAL_PATH)/../include/libswscale
LOCAL_CPPFLAGS := -w -D__STDC_CONSTANT_MACROS -fpermissive
LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample
LOCAL_LDLIBS := -lz -ljnigraphics -llog
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)