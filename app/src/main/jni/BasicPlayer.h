/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#ifndef __BASICPLAYER_H__
#define __BASICPLAYER_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "Movie.h"
#define THREAD_RENDER 0

void initMovie(Movie *movie);

// return: == 0 - success
//          < 0 - error code
int openMovie(Movie *movie, const char filePath[], int width, int height);
int openMovieWithAudio(Movie *movie, const char *filePath, int audio, int width, int height);

int openVideoStream(Movie *movie, int width, int height);
int openAudioStream(Movie *movie);

// return: == 0 - success
//         != 0 then end of movie or fail
int decodeFrame(Movie *movie);

void copyPixels(Movie *movie, uint8_t *pixels);

int getWidth(Movie *movie);
int getHeight(Movie *movie);

void closeMovie(Movie *movie);

// Java단에서 Video는 Timer를 중지시키고, 여기서는 Audio만 중지시킨다.
void pauseMovie(JNIEnv *env, jobject thiz);
void resumeMovie(JNIEnv *env, jobject thiz);

int seekMovie(Movie *movie, int64_t positionUs);

double getFps(Movie *movie);
int64_t getDuration(Movie *movie);
int64_t getPosition(Movie *movie);

// Frame thread & YUV data
void *decodeFrameThread(void *param);
void copyFrameYUVTexData(Movie *movie);

#ifdef __cplusplus
}
#endif

#endif//__BASICPLAYER_H__