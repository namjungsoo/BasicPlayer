#ifndef __LOG_H__
#define __LOG_H__

#include <android/log.h>

#define TAG "libbasicplayer"

#if 1
#define LOGV(...)   __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...)   __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#else
#define LOGV(...)   
#define LOGD(...)   
#define LOGI(...)   
#define LOGW(...)   
#define LOGE(...)   
#endif

#endif//__LOG_H__