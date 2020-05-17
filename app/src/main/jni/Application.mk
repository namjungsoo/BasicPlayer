#APP_ABI := armeabi-v7a
APP_ABI := arm64-v8a, armeabi-v7a
#APP_STL := c++_static
APP_STL := c++_shared
APP_CPPFLAGS += -std=c++11

#APP_ALLOW_MISSING_DEPS := true
#APP_PLATFORM := android-21
APP_PLATFORM := android-16