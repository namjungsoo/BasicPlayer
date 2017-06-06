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
#include <jni.h>
#include "Log.h"
#include "BasicPlayer.h"
#include "AudioTrack.h"
#include "AudioFormatMap.h"
#include "AudioQ.h"
 
Movie *gMovie;

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGD("Hello");
    return JNI_VERSION_1_6;
}

void Java_com_duongame_basicplayer_Player_initAudioTrack(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN initAudioTrack");
	initAudioTrack(env, thiz);
	LOGD("END initAudioTrack");
}

jint Java_com_duongame_basicplayer_Player_initBasicPlayer(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN initBasicPlayer");
	// ARM 패밀리 이고, NEON 피쳐가 있을경우 av_register_all을 해준다. 
	//Initializes libavformat and registers all the muxers, demuxers and protocols. 
	av_register_all();
	initAudioFormatMap();
	AudioQ_init();

	gMovie = (Movie*)malloc(sizeof(Movie));
	LOGD("END initBasicPlayer");
	return 0;
}

jint Java_com_duongame_basicplayer_Player_openMovieWithAudio(JNIEnv *env, jobject thiz, jstring filePath, int audio)
{
	LOGD("BEGIN openMovieWithAudio");
	const jbyte *str;
	int result;

	// 문자열 사용하고 나서 삭제 
	str = (*env)->GetStringUTFChars(env, filePath, NULL);
	result = openMovieWithAudio(gMovie, str, audio);
	(*env)->ReleaseStringUTFChars(env, filePath, str);

	LOGD("END openMovieWithAudio");
	return result;
}

jint Java_com_duongame_basicplayer_Player_openMovie(JNIEnv *env, jobject thiz, jstring filePath)
{
	LOGD("BEGIN openMovie");
	const jbyte *str;
	int result;

	// 문자열 사용하고 나서 삭제 
	str = (*env)->GetStringUTFChars(env, filePath, NULL);
	result = openMovie(gMovie, str);
	(*env)->ReleaseStringUTFChars(env, filePath, str);

	LOGD("END openMovie");
	return result;
}

jint Java_com_duongame_basicplayer_Player_renderFrame(JNIEnv *env, jobject thiz, jobject bitmap)
{
//	LOGD("BEGIN renderFrame");
    
    void *pixels;
	int result;

	// LOGD("renderFrame BEGIN");

	// 영상이 종료된 상태임 
	if(decodeFrame(gMovie) < 0) {
		// LOGD("closeMovie");

		// 영상이 종료되도 close하지 말자 
		//closeMovie();

		// LOGD("renderFrame END");
		return 1;// 종료 상태 
	}
	else {
		// LOGD("renderFrame lockPixels");
		if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
			return result;

		copyPixels(gMovie, (uint8_t*)pixels);

		// LOGD("renderFrame unlockPixels");
		AndroidBitmap_unlockPixels(env, bitmap);
	}

//	LOGD("END renderFrame");
	return 0;
}

jint Java_com_duongame_basicplayer_Player_getMovieWidth(JNIEnv *env, jobject thiz)
{
	return getWidth(gMovie);
}

jint Java_com_duongame_basicplayer_Player_getMovieHeight(JNIEnv *env, jobject thiz)
{
	return getHeight(gMovie);
}

void Java_com_duongame_basicplayer_Player_closeMovie(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN closeMovie");
	closeMovie(gMovie);
	free(gMovie);
	LOGD("END closeMovie");
}

void Java_com_duongame_basicplayer_Player_pauseMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN pauseMovie");
	pauseMovie(env, thiz);
//	LOGD("END pauseMovie");
}

void Java_com_duongame_basicplayer_Player_resumeMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN resumeMovie");
	resumeMovie(env, thiz);
//	LOGD("END resumeMovie");
}

int Java_com_duongame_basicplayer_Player_seekMovie(JNIEnv *env, jobject thiz, jlong positionUs)
{
//	LOGD("BEGIN seekMovie");
	int ret = seekMovie(gMovie, positionUs);
//	LOGD("END seekMovie");
	return ret;
}

jdouble Java_com_duongame_basicplayer_Player_getFps(JNIEnv *env, jobject thiz)
{
	jdouble fps = getFps(gMovie);
	LOGD("interface fps=%f", fps);
	return fps;
}

jlong Java_com_duongame_basicplayer_Player_getMovieDurationUs(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN getMovieDurationUs");
	jlong ret =  getDuration(gMovie);
	LOGD("END getMovieDurationUs");
	return ret;
}

jlong Java_com_duongame_basicplayer_Player_getCurrentPositionUs(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN getCurrentPositionUs");
	jlong ret = getPosition(gMovie);
//	LOGD("END getCurrentPositionUs");
	return ret;
}
