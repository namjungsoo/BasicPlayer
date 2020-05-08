#ifndef __MOVIE_H__
#define __MOVIE_H__

#pragma once

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

    // 비디오 관련
    // 입력 비디오
    AVFormatContext *gFormatCtx;
    AVCodecContext *gVideoCodecCtx;
    AVCodec *gVideoCodec;
    int gVideoStreamIdx;
    AVFrame *gFrame;
    int gVideoPictureSize;

    // 출력비디오
    struct SwsContext *gImgConvertCtx;
    int gPictureSize;
    uint8_t *gVideoBuffer;
    AVFrame *gFrameRGB;
    int gPixelFormat;
    int gTargetWidth;
    int gTargetHeight;

    // 비디오 공통
    double gFps;
    int64_t gCurrentTimeUs;
    AVDictionary *optionsDict;

    // 오디오 관련 
    AVCodecContext *gAudioCodecCtx;
    AVCodec *gAudioCodec;
    AVFrame *gFrameAudio;
    pthread_t gAudioThread;
    pthread_t gFrameThread;
    int gAudioStreamIdx;
    int gAudioThreadRunning;
    int gFrameThreadRunning;

    uint8_t *gData[3];

    enum AVSampleFormat sfmt;
} Movie;

#ifdef __cplusplus
}
#endif

#endif//__MOVIE_H__