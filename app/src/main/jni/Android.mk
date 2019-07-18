LOCAL_PATH := $(call my-dir)
#LOCAL_FFMPEG_PATH := ffmpeg/ffmpeg/arm64-v8a
LOCAL_FFMPEG_PATH := static/arm64-v8a

# BEGIN PREBUILT_SHARED_LIBRARY
include $(CLEAR_VARS)
LOCAL_MODULE:= libavcodec
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libavcodec.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libavformat
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libavformat.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)
 
include $(CLEAR_VARS)   
LOCAL_MODULE:= libswscale
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libswscale.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)
 
include $(CLEAR_VARS)
LOCAL_MODULE:= libswresample
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libswresample.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libavutil
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libavutil.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

# 추가된것
include $(CLEAR_VARS)
LOCAL_MODULE:= libx264
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libx264.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libcrypto
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libcrypto.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libssl
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libssl.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libvorbis
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libvorbis.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libvorbisenc
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libvorbisenc.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libvorbisfile
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libvorbisfile.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libogg
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libogg.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libopus
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libopus.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libmp3lame
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libmp3lame.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libfdk-aac
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libfdk-aac.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE:= libshine
LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libshine.a
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
include $(PREBUILT_STATIC_LIBRARY)

# include $(CLEAR_VARS)
# LOCAL_MODULE:= libavfilter
# LOCAL_SRC_FILES:= $(LOCAL_FFMPEG_PATH)/lib/libavfilter.so
# LOCAL_EXPORT_C_INCLUDES := $(LOCAL_FFMPEG_PATH)/include
# include $(PREBUILT_SHARED_LIBRARY)


# END PREBUILT_SHARED_LIBRARY
include $(CLEAR_VARS)

LOCAL_MODULE := libbasicplayer
LOCAL_SRC_FILES := BasicPlayer.c Interface.c AudioQ.cpp AudioTrack.cpp AudioFormatMap.cpp PlayerMap.cpp
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../include/ \
                    $(LOCAL_PATH)/../include/libavcodec \
                    $(LOCAL_PATH)/../include/libavformat \
                    $(LOCAL_PATH)/../include/libswscale \
                    $(LOCAL_PATH)/../include/libavutil
LOCAL_CFLAGS := -D__STDC_CONSTANT_MACROS -fpermissive -w
#LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil libswresample
#LOCAL_SHARED_LIBRARIES := libavformat libavcodec libswscale libavutil
LOCAL_STATIC_LIBRARIES := libavformat libavcodec libswscale libswresample libavutil libx264 libssl libvorbis libcrypto libvorbisfile libvorbisenc libogg libopus libmp3lame libfdk-aac libshine 
LOCAL_LDLIBS := -lz -ljnigraphics -llog
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)
