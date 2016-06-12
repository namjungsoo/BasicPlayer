/*
 * jni for Android
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include "BasicPlayer.h"
 
#define TAG "basicplayer-so"

#define LOGV(...)   __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGD(...)   __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGI(...)   __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGW(...)   __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define LOGE(...)   __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGD("Hello");
    return JNI_VERSION_1_6;
}

jint Java_com_duongame_basicplayer_MoviePlayView_initBasicPlayer(JNIEnv *env, jobject thiz)
{
	// ARM 패밀리 이고, NEON 피쳐가 있을경우 av_register_all을 해준다. 
	//Initializes libavformat and registers all the muxers, demuxers and protocols. 
	av_register_all();
	return 0;
}

jint Java_com_duongame_basicplayer_MoviePlayView_openMovie(JNIEnv *env, jobject thiz, jstring filePath)
{
	const jbyte *str;
	int result;
	str = (*env)->GetStringUTFChars(env, filePath, NULL);
	result = openMovie(str);
	(*env)->ReleaseStringUTFChars(env, filePath, str);
	return result;
}

jint Java_com_duongame_basicplayer_MoviePlayView_renderFrame(JNIEnv *env, jobject thiz, jobject bitmap)
{
    void *pixels;
	int result;

	if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
		return result;

	decodeFrame();
	copyPixels((uint8_t*)pixels);

	AndroidBitmap_unlockPixels(env, bitmap);
}

jint Java_com_duongame_basicplayer_MoviePlayView_getMovieWidth(JNIEnv *env, jobject thiz)
{
	return getWidth();
}

jint Java_com_duongame_basicplayer_MoviePlayView_getMovieHeight(JNIEnv *env, jobject thiz)
{
	return getHeight();
}

void Java_com_duongame_basicplayer_MoviePlayView_closeMovie(JNIEnv *env, jobject thiz)
{
	closeMovie();
}

jdouble Java_com_duongame_basicplayer_MoviePlayView_getFps(JNIEnv *env, jobject thiz)
{
	//jdouble fps = getFps();
	jdouble fps = gFps;
	LOGD("interface fps=%f", fps);
	return fps;
}
