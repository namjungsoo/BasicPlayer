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

#ifdef __cplusplus
}
#endif

#endif//__MOVIE_H__