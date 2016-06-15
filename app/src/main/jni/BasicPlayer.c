/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

// std c lib
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>

// ffmpeg lib
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

// android lib
#include <android/log.h>
#include <jni.h>// JNI_OnLoad

// linux lib
#include <sys/types.h>
#include <unistd.h>
#include <pthread.h>

#include "BasicPlayer.h"
#include "Log.h"
#include "AudioQ.h"
#include "AudioTrack.h"

AVFormatContext *gFormatCtx = NULL;

// 비디오 관련 
AVCodecContext *gVideoCodecCtx = NULL;
AVCodec *gVideoCodec = NULL;
int gVideoStreamIdx = -1;

AVFrame *gFrame = NULL;
AVFrame *gFrameRGB = NULL;

struct SwsContext *gImgConvertCtx = NULL;

int gPictureSize = 0;
uint8_t *gVideoBuffer = NULL;

AVDictionary *optionsDict = NULL;

int gPixelFormat = AV_PIX_FMT_BGR32;
double gFps = 0.0;

// 오디오 관련 
AVCodecContext *gAudioCodecCtx = NULL;
AVCodec *gAudioCodec = NULL;
int gAudioStreamIdx = -1;
AVFrame *gFrameAudio = NULL;

pthread_t gAudioThread = 0;
int gAudioThreadRunning = 1;

int64_t getTimeNsec() 
{
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t) now.tv_sec*1000000000LL + now.tv_nsec;
}

// 현재 사용안함 
// double getFps() 
// {
// 	// LOGD("getFps %f", gFps);
// 	// return gFps;
// 	return 24.0;
// }

int openVideoStream() 
{
	LOGD("openVideoStream");

	// 비디오 스트림 인덱스를 체크한다. 
	if (gVideoStreamIdx == -1)
		return -4;

	// 비디오 코텍을 찾아서 오픈한다. 
	gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIdx]->codec;
	gVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
	if (gVideoCodec == NULL)
		return -5;

	if (avcodec_open2(gVideoCodecCtx, gVideoCodec, &optionsDict) < 0)
		return -6;

	// 프레임을 할당한다. frame은 원본 frameRGB는 변환용 
	gFrame = av_frame_alloc();
	if (gFrame == NULL)
		return -7;
	gFrameRGB = av_frame_alloc();
	if (gFrameRGB == NULL)
		return -8;

	// 오디오를 위해 추가됨 
	gFrameAudio = av_frame_alloc();
	if (gFrameAudio == NULL)
		return -9;
	
	// 픽처 사이즈를 계산한다. 
	gPictureSize = avpicture_get_size(gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);
	// 비디오 버퍼를 할당한다. 
	gVideoBuffer = (uint8_t*)(malloc(sizeof(uint8_t) * gPictureSize));

	// 비디오 버퍼 메모리를 설정함
	avpicture_fill((AVPicture*)gFrameRGB, gVideoBuffer, gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);

	gFps = av_q2d(gFormatCtx->streams[gVideoStreamIdx]->r_frame_rate);
	LOGD("fps=%f", gFps);
	return 0;
}

int openAudioStream() 
{
	LOGD("openAudioStream");

	// 오디오 스트림 인덱스를 체크한다. 
	if (gAudioStreamIdx == -1)
		return -4;

	// 오디오 코텍을 찾아서 오픈한다. 
	gAudioCodecCtx = gFormatCtx->streams[gAudioStreamIdx]->codec;
	gAudioCodec = avcodec_find_decoder(gAudioCodecCtx->codec_id);
	if (gAudioCodec == NULL)
		return -5;

	if (avcodec_open2(gAudioCodecCtx, gAudioCodec, &optionsDict) < 0)
		return -6;

	LOGD("gAudioCodecCtx->sample_fmt=%d", gAudioCodecCtx->sample_fmt);
	LOGD("gAudioCodecCtx->sample_rate=%d", gAudioCodecCtx->sample_rate);
	LOGD("gAudioCodecCtx->channels=%d", gAudioCodecCtx->channels);
}

void decodeAudioThread(void *param) 
{
	LOGD("decodeAudioThread");
	int frameFinished = 0;

//	createAudioTrack();

	while(gAudioThreadRunning) {
//		LOGD("decodeAudioThread running");
		if(AudioQ_size() > 0) {
//			LOGD("decodeAudioThread queue pop");

			AVPacket packet = AudioQ_pop();			

			int64_t begin = getTimeNsec();
 			int len = avcodec_decode_audio4(gAudioCodecCtx, gFrameAudio, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			if(len < 0) {
				LOGD("skip audio");
			}
			
//			LOGD("audio diff time=%llu", diff);

 			// 이게 전부 0.0에서 변화가 없음
 			double pts = av_frame_get_best_effort_timestamp(gFrameAudio);
 			double pts_clock = pts * av_q2d(gFormatCtx->streams[gAudioStreamIdx]->time_base);
//			LOGD("decodeAudioThread ts=%f pts_clock=%f", pts, pts_clock);

 			if (frameFinished) {
				int data_size = av_samples_get_buffer_size(NULL, gAudioCodecCtx->channels, gFrameAudio->nb_samples, gAudioCodecCtx->sample_fmt, 1);
				//gFrameAudio->data[0];
//				LOGD("frameFinished data_size=%d", data_size);

				//사운드 데이터를 집어 넣는다. 
				writeAudioTrack(gFrameAudio->data[0], data_size);

				av_free_packet(&packet);
//				return 0;
 			}
			else {
				LOGD("frameFinished NO");
				av_free_packet(&packet);
			}

		}
		usleep(100);
	}

	LOGD("decodeAudioThread end");
}

int openMovie(const char filePath[])
{
	LOGD("openMovie %s", filePath);

	int i;
	unsigned char errbuf[128];
	
	// 최초에 컨텍스트가 null이 맞는지 확인한다. 
	if (gFormatCtx != NULL)
		return -1;

	// 파일을 연다. 
	int err = avformat_open_input(&gFormatCtx, filePath, NULL, NULL);
	if(err < 0) {
		av_strerror(err, errbuf, sizeof(errbuf));
		LOGD("%s", errbuf);  
		return -2;
	}

	// 스트짐 정보를 포맷 컨텍스트에 리턴한다. 
	if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
		return -3;

	for (i = 0; i < gFormatCtx->nb_streams; i++) {
		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			gVideoStreamIdx = i;
//			LOGD("gVideoStreamIdx=%d", gVideoStreamIdx);
//			break;
		}

		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
			gAudioStreamIdx = i;
//			LOGD("gAudioStreamIdx=%d", gAudioStreamIdx);
//			break;
		}		
	}

	int ret;
	ret = openVideoStream();
	if(ret < 0)
		return ret;  

	ret = openAudioStream(); 
	if(ret < 0)
		return ret;  

	prepareAudioTrack(gAudioCodecCtx->sample_rate, gAudioCodecCtx->channels);

// 	ret = createAudioTrack(env, thiz);
	ret = pthread_create(&gAudioThread, NULL, decodeAudioThread, NULL);

	return ret;
}

// 40ms만에 한번씩 호출된다. 
int decodeFrame()
{
	int frameFinished = 0;
	AVPacket packet;
	
	// 한번에 하나를 읽고 종료하자 
	while (av_read_frame(gFormatCtx, &packet) >= 0) {
		if (packet.stream_index == gVideoStreamIdx) {
			int64_t begin = getTimeNsec();
			avcodec_decode_video2(gVideoCodecCtx, gFrame, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;
//			LOGD("video diff time=%llu", diff);

			// 이게 전부 0.0에서 변화가 없음
			double pts = av_frame_get_best_effort_timestamp(gFrame);
			double pts_clock = pts * av_q2d(gFormatCtx->streams[gVideoStreamIdx]->time_base);
//			LOGD("decodeVideoFrame ts=%f pts_clock=%f", pts, pts_clock);

			if (frameFinished) {
				gImgConvertCtx = sws_getCachedContext(gImgConvertCtx,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gVideoCodecCtx->pix_fmt,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gPixelFormat, SWS_BICUBIC, NULL, NULL, NULL);
				
				sws_scale(gImgConvertCtx, gFrame->data, gFrame->linesize, 0, gVideoCodecCtx->height, gFrameRGB->data, gFrameRGB->linesize);
				
				av_free_packet(&packet);
				return 0;
			}
			else {
				av_free_packet(&packet);
			}
		}
		else if(packet.stream_index == gAudioStreamIdx) {
			//TODO: 큐 동기화가 필요함 
			AudioQ_push(packet);
//			LOGD("decodeFrame audio queue push");
		}
		else {
			// 처리하지 못했을때 자체적으로 packet을 free 함 
			av_free_packet(&packet);
		}
		usleep(100);
	}
	return -1;
}

void copyPixels(uint8_t *pixels)
{
	memcpy(pixels, gFrameRGB->data[0], gPictureSize);
}

int getWidth()
{
	return gVideoCodecCtx->width;
}

int getHeight()
{
	return gVideoCodecCtx->height;
}

void closeMovie()
{
	if (gVideoBuffer != NULL) {
		free(gVideoBuffer);
		gVideoBuffer = NULL;
	}
	
	if (gFrame != NULL)
		av_freep(gFrame);

	if (gFrameRGB != NULL)
		av_freep(gFrameRGB);

	if (gVideoCodecCtx != NULL) {
		avcodec_close(gVideoCodecCtx);
		gVideoCodecCtx = NULL;
	}
	
	if (gFormatCtx != NULL) {
        avformat_close_input(&gFormatCtx);
		gFormatCtx = NULL;
	}
}
