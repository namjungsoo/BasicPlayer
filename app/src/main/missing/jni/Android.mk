LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE:= libmissing
LOCAL_SRC_FILES:= missing.c
LOCAL_LDLIBS := -lc

include $(BUILD_SHARED_LIBRARY)

# include $(CLEAR_VARS)

# LOCAL_MODULE:= libnone
# LOCAL_STATIC_LIBRARIES := libmissing

# include $(BUILD_SHARED_LIBRARY)
