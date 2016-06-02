LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libbasicplayer
LOCAL_SRC_FILES := BasicPlayer.c Interface.c

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include/ \
                    $(LOCAL_PATH)/../include/libavcodec \
                    $(LOCAL_PATH)/../include/libavformat \
                    $(LOCAL_PATH)/../include/libswscale

LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample cpufeatures

LOCAL_LDLIBS := -lz -ljnigraphics

LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)

$(call import-module,ffmpeg-3.0.2/android/arm,android/cpufeatures)
