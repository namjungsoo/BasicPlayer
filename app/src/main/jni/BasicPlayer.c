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

//Audio
#include "AudioQ.h"
#include "AudioTrack.h"
#include "AudioFormatMap.h"

#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000

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
int64_t gCurrentTimeUs = 0l;

// 오디오 관련 
AVCodecContext *gAudioCodecCtx = NULL;
AVCodec *gAudioCodec = NULL;
int gAudioStreamIdx = -1;
AVFrame *gFrameAudio = NULL;

pthread_t gAudioThread = 0;
int gAudioThreadRunning = 1;

enum AVSampleFormat sfmt;

int64_t getTimeNsec() 
{
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t) now.tv_sec*1000000000LL + now.tv_nsec;
}

// 현재 사용안함 
double getFps() 
{
	LOGD("getFps %f", gFps);
	return gFps;
}

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
//	gPictureSize = avpicture_get_size(gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);
	gPictureSize = av_image_get_buffer_size(gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height, 1);

	// 비디오 버퍼를 할당한다. 
	gVideoBuffer = (uint8_t*)(malloc(sizeof(uint8_t) * gPictureSize));

	// 비디오 버퍼 메모리를 설정함
//	avpicture_fill((AVPicture*)gFrameRGB, gVideoBuffer, gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height);
	av_image_fill_arrays(gFrameRGB->data, gFrameRGB->linesize, gVideoBuffer, gPixelFormat, gVideoCodecCtx->width, gVideoCodecCtx->height, 1);

	// attribute_deprecated int avpicture_fill	(	AVPicture * 	picture,
	// const uint8_t * 	ptr,
	// enum AVPixelFormat 	pix_fmt,
	// int 	width,
	// int 	height 
	// )	

	// int av_image_fill_arrays	(	uint8_t * 	dst_data[4],
	// int 	dst_linesize[4],
	// const uint8_t * 	src,
	// enum AVPixelFormat 	pix_fmt,
	// int 	width,
	// int 	height,
	// int 	align 
	// )	

	gFps = av_q2d(gFormatCtx->streams[gVideoStreamIdx]->r_frame_rate);
	LOGD("fps=%f", gFps);
	return 0;
}

int openAudioStream() 
{
	LOGD("openAudioStream gAudioStreamIdx=%d", gAudioStreamIdx);

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

	sfmt = gAudioCodecCtx->sample_fmt;

	const char *audioFormat = getAudioFormatString(sfmt);
	LOGD("audioFormat=%s", audioFormat);
}

void* decodeAudioThread(void *param) 
{
	LOGD("decodeAudioThread begin");
	int frameFinished = 0;

	int buffer_size = AVCODEC_MAX_AUDIO_FRAME_SIZE + FF_INPUT_BUFFER_PADDING_SIZE;
	LOGD("decodeAudioThread buffer_size=%d", buffer_size);

	uint8_t *buffer = av_malloc(sizeof(uint8_t)*buffer_size);
	uint8_t *samples = av_malloc(sizeof(uint8_t)*buffer_size);

	while(gAudioThreadRunning) {
		AudioQ_lock();
		size_t size = AudioQ_size();
		AudioQ_unlock();

		if(size > 0) {
			AudioQ_lock();
			AVPacket packet = AudioQ_pop();
			AudioQ_unlock();

			int64_t begin = getTimeNsec();
 			int len = avcodec_decode_audio4(gAudioCodecCtx, gFrameAudio, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			if(len < 0) {
				LOGD("skip audio");
			}
			
 			// 이게 전부 0.0에서 변화가 없음
 			double pts = av_frame_get_best_effort_timestamp(gFrameAudio);
 			double pts_clock = pts * av_q2d(gFormatCtx->streams[gAudioStreamIdx]->time_base);

 			if (frameFinished) {
                int write_p = 0;
				int plane_size;
				int data_size = av_samples_get_buffer_size(&plane_size, gAudioCodecCtx->channels, gFrameAudio->nb_samples, gAudioCodecCtx->sample_fmt, 1);
				uint16_t nb, ch;

				if(sfmt == AV_SAMPLE_FMT_S16P) {
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(uint16_t); nb++) {
						for (ch = 0; ch < gAudioCodecCtx->channels; ch++) {
							out[write_p] = ((uint16_t *) gFrameAudio->extended_data[ch])[nb];
							write_p++;
						}
					}
					writeAudioTrack(samples, plane_size * gAudioCodecCtx->channels);
				}
				else if(sfmt == AV_SAMPLE_FMT_FLTP) {
					// LOGD("decodeAudioThread AV_SAMPLE_FMT_FLTP");
					// resample: float -> short
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(float); nb++) {
						for (ch = 0; ch < gAudioCodecCtx->channels; ch++) {
							out[write_p] = (short)(((float *) gFrameAudio->extended_data[ch])[nb] * SHRT_MAX);
							write_p++;
						}
					}
					writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t) * gAudioCodecCtx->channels);
					
					// float
					// float *out = (float *)samples;
					// for (nb = 0; nb < plane_size / sizeof(float); nb++) {
					// 	for (ch = 0; ch < gAudioCodecCtx->channels; ch++) {
					// 		out[write_p] = ((float *) gFrameAudio->extended_data[ch])[nb];
					// 		write_p++;
					// 	}
					// }
					// writeAudioTrack(samples, plane_size * gAudioCodecCtx->channels);
				}
				else if(sfmt == AV_SAMPLE_FMT_U8P) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++) {
                        for (ch = 0; ch < gFrameAudio->channels; ch++) {
                            out[write_p] = (((uint8_t *) gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127;
                            write_p++;
                        }
                    }
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t) * gAudioCodecCtx->channels);

					// uint8
					// uint8_t *out = (uint8_t *)samples;
                    // for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++) {
                    //     for (ch = 0; ch < gAudioCodecCtx->channels; ch++) {
                    //         out[write_p] = ((uint8_t *) gFrameAudio->extended_data[ch])[nb];
                    //         write_p++;
                    //     }
                    // }
					// writeAudioTrack(samples, plane_size * gAudioCodecCtx->channels);
				}

				// 채널 구분이 없음 
				else if(sfmt == AV_SAMPLE_FMT_S16) {
					writeAudioTrack((char*)gFrameAudio->extended_data[0], gFrameAudio->linesize[0]);
				}
				else if(sfmt == AV_SAMPLE_FMT_FLT) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(float); nb++) {
                        out[nb] = (short) ( ((float *) gFrameAudio->extended_data[0])[nb] * SHRT_MAX);
                    }
                    writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t));

                    // writeAudioTrack((char*)gFrameAudio->extended_data[0], gFrameAudio->linesize[0]);
				}
				else if(sfmt == AV_SAMPLE_FMT_U8) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++) {
                        out[nb] = (short) ( (((uint8_t *) gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127);
                    }					
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t));	

					// writeAudioTrack((char*)gFrameAudio->extended_data[0], gFrameAudio->linesize[0]);
				}

				av_packet_unref(&packet);
 			}
			else {
				av_packet_unref(&packet);
			}

		}
		usleep(1);
	}

	av_free(buffer);
	av_free(samples);
	return NULL;
}

int openMovieWithAudio(const char *filePath, int audio)
{
	LOGD("openMovieWithAudio filePath=%s audio=%d", filePath, audio);

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
			LOGD("gVideoStreamIdx=%d", gVideoStreamIdx);
		}

		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
			gAudioStreamIdx = i;
			LOGD("gAudioStreamIdx=%d", gAudioStreamIdx);
		}		
	}

	int ret;
	ret = openVideoStream();
	if(ret < 0)
		return ret;  

	if(audio) {
		// 오디오는 없을수 있다. 
		ret = openAudioStream(); 
		if(ret < 0) {
			LOGD("Audio NOT FOUND");
			return 0;
		}
		else {
			prepareAudioTrack(gAudioCodecCtx->sample_fmt, gAudioCodecCtx->sample_rate, gAudioCodecCtx->channels);
			gAudioThreadRunning = 1;
			ret = pthread_create(&gAudioThread, NULL, decodeAudioThread, NULL);
		}		
	}

	return ret;
}

int openMovie(const char filePath[])
{
	LOGD("openMovie filePath=%s", filePath);

	return openMovieWithAudio(filePath, 1);
}

// 40ms만에 한번씩 호출된다. 
int decodeFrame()
{
	int frameFinished = 0;
	AVPacket packet;

	if(gFormatCtx == NULL) {
		LOGD("decodeFrame END");
		return -1;
	}
	
	// 한번에 하나를 읽고 종료하자 
	while (av_read_frame(gFormatCtx, &packet) >= 0) {
		if (packet.stream_index == gVideoStreamIdx) {
			int64_t begin = getTimeNsec();
			avcodec_decode_video2(gVideoCodecCtx, gFrame, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			// 이게 전부 0.0에서 변화가 없음
			int64_t pts = av_frame_get_best_effort_timestamp(gFrame);
//			double pts_clock = pts * av_q2d(gFormatCtx->streams[gVideoStreamIdx]->time_base);
			gCurrentTimeUs = av_rescale_q(pts, gFormatCtx->streams[gVideoStreamIdx]->time_base, AV_TIME_BASE_Q);
//			LOGD("pts=%f pts_clock=%f pts_long=%lu", pts, pts_clock, pts_long);

			if (frameFinished) {
				gImgConvertCtx = sws_getCachedContext(gImgConvertCtx,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gVideoCodecCtx->pix_fmt,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gPixelFormat, SWS_BICUBIC, NULL, NULL, NULL);
				
				sws_scale(gImgConvertCtx, (const uint8_t * const*)gFrame->data, gFrame->linesize, 0, gVideoCodecCtx->height, gFrameRGB->data, gFrameRGB->linesize);
				
				av_packet_unref(&packet);
				return 0;
			}
			else {
				av_packet_unref(&packet);
			}
		}
		else if(packet.stream_index == gAudioStreamIdx) {
			//TODO: 큐 동기화가 필요함 
			if(gAudioThread != 0) {
				AudioQ_lock();
				AudioQ_push(packet);
				AudioQ_unlock();
			}
		}
		else {
			// 처리하지 못했을때 자체적으로 packet을 free 함 
			av_packet_unref(&packet);
		}
		usleep(100);
	}

	LOGD("decodeFrame END");
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

void closeFrame() 
{
	if (gFrame != NULL) {
		av_frame_free(&gFrame);
		gFrame = NULL;
	}
	LOGD("closeMovie gFrame");

	if (gFrameRGB != NULL) {
		av_frame_free(&gFrameRGB);
		gFrameRGB = NULL;
	}
	LOGD("closeMovie gFrameRGB");

	if (gFrameAudio != NULL) {
		av_frame_free(&gFrameAudio);
		gFrameAudio = NULL;
	}
	LOGD("closeMovie gFrameAudio");
}

void closeMovie()
{
	int status;

	LOGD("closeMovie BEGIN");
	gAudioThreadRunning = 0;

	pthread_join(gAudioThread, (void**)&status);
	gAudioThread = 0;

	if (gVideoBuffer != NULL) {
		free(gVideoBuffer);
		gVideoBuffer = NULL;
	}
	LOGD("closeMovie gVideoBuffer");
	
	if (gVideoCodecCtx != NULL) {
		avcodec_close(gVideoCodecCtx);
		gVideoCodecCtx = NULL;
	}
	LOGD("closeMovie gVideoCodecCtx");
	
	//Audio 
	if(gAudioCodecCtx != NULL) {
		avcodec_close(gAudioCodecCtx);
		gAudioCodecCtx = NULL;
	}
	LOGD("closeMovie gAudioCodecCtx");

	if (gFormatCtx != NULL) {
        avformat_close_input(&gFormatCtx);
		gFormatCtx = NULL;
	}
	LOGD("closeMovie gFormatCtx");

	closeFrame();

	gVideoStreamIdx = -1;
	gAudioStreamIdx = -1;

	AudioQ_lock();
	AudioQ_clear();
	AudioQ_unlock();

	LOGD("closeMovie END");
}

void pauseMovie(JNIEnv *env, jobject thiz) 
{
	// 사운드 쓰레드를 중지시킴 
	pauseAudioTrack(env, thiz);    
}

void resumeMovie(JNIEnv *env, jobject thiz) 
{
	// 사운드 쓰레드를 재개
	resumeAudioTrack(env, thiz); 
}

int seekMovie(int64_t positionUs) 
{
//	LOGD("seekMovie positionUs=%lld", positionUs);

	// 프레임을 해당 시간으로 이동시킴
	int64_t seekTarget = av_rescale_q(positionUs, AV_TIME_BASE_Q, gFormatCtx->streams[gVideoStreamIdx]->time_base);
//	LOGD("seekMovie seekTarget=%lld", seekTarget);

	if(av_seek_frame(gFormatCtx, gVideoStreamIdx, seekTarget, AVSEEK_FLAG_FRAME) < 0) {
        LOGD("FAILED av_seek_frame");
        return -1;
	}

	// 오디오 큐를 비운다. 
	AudioQ_lock();
	AudioQ_clear();
	AudioQ_unlock();
	return 0;
}

int64_t getDuration() 
{
	// 이건 믿음녀 안됨 
	// LOGD("gFormatCtx->duration=%lu", gFormatCtx->duration);
	LOGD("gFormatCtx->nb_streams=%d", gFormatCtx->nb_streams);

	int i;
	for(i=0; i<gFormatCtx->nb_streams; i++) {
		//AVStream* stream = gFormatCtx->streams[i];
		AVStream* stream;
		if(i == 0) {
			stream = gFormatCtx->streams[gVideoStreamIdx];
		}
		else {
			stream = gFormatCtx->streams[gAudioStreamIdx];
		}
		
		LOGD("stream->duration=%lld", stream->duration);
		if(stream->duration > 0) {
			int64_t duration = av_rescale_q(stream->duration, stream->time_base, AV_TIME_BASE_Q);
			LOGD("duration=%lld", duration);

			if(duration != 0)
				return duration;
		}
	}

	LOGD("gFormatCtx->duration=%lld", gFormatCtx->duration);
	if(gFormatCtx->duration > 0) {
		return gFormatCtx->duration;		
	}

	return 0ll;
}

int64_t getPosition()
{
	return gCurrentTimeUs;
}