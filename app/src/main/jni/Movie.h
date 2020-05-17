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
    // 공통 
    AVFormatContext *gFormatCtx;
    int64_t gCurrentTimeUs;

    // 비디오 관련 
    AVCodecContext *gVideoCodecCtx;
    AVCodec *gVideoCodec;
    AVFrame *gFrame;
    AVFrame *gFrameRGB;
    int gVideoStreamIdx;
    int gPictureSize;
    uint8_t *gVideoBuffer;
    uint8_t *gData[3];// YUV 데이터
    int gPixelFormat;
    double gFps;
    pthread_t gFrameThread;
    int gFrameThreadRunning;
    int gTargetWidth;
    int gTargetHeight;

    // 오디오 관련 
    AVCodecContext *gAudioCodecCtx;
    AVCodec *gAudioCodec;
    AVFrame *gFrameAudio;
    int gAudioStreamIdx;
    pthread_t gAudioThread;
    int gAudioThreadRunning;

    // 소프트웨어 디코딩시에만 사용됨 
    struct SwsContext *gImgConvertCtx;
    //AVDictionary *optionsDict;// 필요없음
    //enum AVSampleFormat sfmt;// 필요없음
} Movie;

#ifdef __cplusplus
}
#endif

#endif//__MOVIE_H__