/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#ifndef BASICPLAYER_H__INCED__110326
#define BASICPLAYER_H__INCED__110326

#ifdef __cplusplus
extern "C" {
#endif

// ffmpeg lib
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

#include <libavutil/imgutils.h>// av_image_fill_arrays, av_image_get_buffer_size
#include <libavutil/mem.h>
#include <pthread.h>

typedef struct {
    AVFormatContext *gFormatCtx;

    // 비디오 관련 
    AVCodecContext *gVideoCodecCtx;
    AVCodec *gVideoCodec;
    int gVideoStreamIdx;

    AVFrame *gFrame;
    AVFrame *gFrameRGB;

    struct SwsContext *gImgConvertCtx;

    int gPictureSize;
    uint8_t *gVideoBuffer;

    AVDictionary *optionsDict;

    int gPixelFormat;
    double gFps;
    int64_t gCurrentTimeUs;

    // 오디오 관련 
    AVCodecContext *gAudioCodecCtx;
    AVCodec *gAudioCodec;
    int gAudioStreamIdx;
    AVFrame *gFrameAudio;

    pthread_t gAudioThread;
    int gAudioThreadRunning;

    enum AVSampleFormat sfmt;
} Movie;

void initMovie(Movie *movie);

// return: == 0 - success
//          < 0 - error code
int openMovie(Movie *movie, const char filePath[]);
int openMovieWithAudio(Movie *movie, const char *filePath, int audio);

int openVideoStream(Movie *movie);
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

#ifdef __cplusplus
}
#endif

#endif
