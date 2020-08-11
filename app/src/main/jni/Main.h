#ifndef __MAIN_H__
#define __MAIN_H__

#pragma once
#include <jni.h>

namespace Main {
    void initAudioTrack(JNIEnv *env, jobject thiz);
    void pauseMovie(JNIEnv *env, jobject thiz);
    void resumeMovie(JNIEnv *env, jobject thiz);

    int initBasicPlayer();
    int openMovieWithAudio(JNIEnv *env, jobject thiz, int id, jstring filePath, int audio, int width, int height);
    int openMovie(JNIEnv *env, jobject thiz, int id, jstring filePath, int width, int height);
    int renderFrame(JNIEnv *env, jobject thiz, int id, jobject bitmap);
    int renderFrameYUVTexId(int id, int width, int height, int texIdY, int texIdU, int texIdV);

    int getMovieWidth(int id);
    int getMovieHeight(int id);
    void closeMovie(int id);
    int seekMovie(int id, jlong positionUs);
    double getFps(int id);
    long getMovieDurationUs(int id);
    long getCurrentPositionUs(int id);
}

#endif//__MAIN_H__