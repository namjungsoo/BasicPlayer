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
#include <time.h> // clock_gettime
//#include <ctime>
//#include <chrono>

// ffmpeg lib
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

#include <libavutil/imgutils.h>// av_image_fill_arrays, av_image_get_buffer_size
#include <libavutil/mem.h>

// android lib
#include <android/log.h>
#include <jni.h>// JNI_OnLoad

// linux lib
#include <sys/types.h>
#include <unistd.h>

#include "BasicPlayer.h"
#include "Log.h"

//Audio
#include "AudioQ.h"
#include "AudioTrack.h"
#include "AudioFormatMap.h"

#define AVCODEC_MAX_AUDIO_FRAME_SIZE 192000

int64_t getTimeNsec() 
{
    struct timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return (int64_t) now.tv_sec*1000000000LL + now.tv_nsec;
}

// 구조체를 초기화한다.
void initMovie(Movie *movie)
{
	memset(movie, 0, sizeof(Movie));
	
	movie->gPixelFormat = AV_PIX_FMT_BGR32;
	movie->gVideoStreamIdx = -1;// 인덱스 INVALID
	movie->gAudioStreamIdx = -1;// 인덱스 INVALID
	movie->gAudioThreadRunning = 0;// 쓰레드 중지 상태
}

// 현재 사용안함 
double getFps(Movie *movie) 
{
	LOGD("getFps %f", movie->gFps);
	return movie->gFps;
}

int openVideoStream(Movie *movie, int width, int height) 
{
	LOGD("openVideoStream");

	// 비디오 스트림 인덱스를 체크한다. 
	if (movie->gVideoStreamIdx == -1)
		return -4;

	// 비디오 코텍을 찾아서 오픈한다. 
	movie->gVideoCodecCtx = movie->gFormatCtx->streams[movie->gVideoStreamIdx]->codec;
	movie->gVideoCodec = avcodec_find_decoder(movie->gVideoCodecCtx->codec_id);
	if (movie->gVideoCodec == NULL)
		return -5;

	if (avcodec_open2(movie->gVideoCodecCtx, movie->gVideoCodec, &movie->optionsDict) < 0)
		return -6;

	// 프레임을 할당한다. frame은 원본 frameRGB는 변환용 
	movie->gFrame = av_frame_alloc();
	if (movie->gFrame == NULL)
		return -7;
	movie->gFrameRGB = av_frame_alloc();
	if (movie->gFrameRGB == NULL)
		return -8;

	// 오디오를 위해 추가됨 
	movie->gFrameAudio = av_frame_alloc();
	if (movie->gFrameAudio == NULL)
		return -9;
	
	if (width == 0)
		movie->gTargetWidth = movie->gVideoCodecCtx->width;
	else 
		movie->gTargetWidth = width;

	if (height == 0)
		movie->gTargetHeight = movie->gVideoCodecCtx->height;
	else 
		movie->gTargetHeight = height;

	// 픽처 사이즈를 계산한다. 
	movie->gPictureSize = av_image_get_buffer_size(movie->gPixelFormat, movie->gTargetWidth, movie->gTargetHeight, 1);
	movie->gVideoPictureSize = av_image_get_buffer_size(movie->gVideoCodecCtx->pix_fmt, movie->gTargetWidth, movie->gTargetHeight, 1);
    LOGD("openVideoStream gPictureSize=%d gVideoPictureSize=%d", movie->gPictureSize, movie->gVideoPictureSize);

	// 비디오 버퍼를 할당한다. 
	movie->gVideoBuffer = (uint8_t*)(malloc(sizeof(uint8_t) * movie->gPictureSize));

	// 비디오 버퍼 메모리(gFrameRGB)를 설정함
	av_image_fill_arrays(movie->gFrameRGB->data, movie->gFrameRGB->linesize, movie->gVideoBuffer, movie->gPixelFormat, movie->gTargetWidth, movie->gTargetHeight, 1);

	LOGD("openVideoStream gFrame->linesize=%ld gFrameRGB->linesize=%ld", movie->gFrame->linesize, movie->gFrameRGB->linesize);
	movie->gFps = av_q2d(movie->gFormatCtx->streams[movie->gVideoStreamIdx]->r_frame_rate);
	LOGD("openVideoStream fps=%f", movie->gFps);

	LOGD("openVideoStream pix_fmt=%d(%s)", movie->gVideoCodecCtx->pix_fmt, av_get_pix_fmt_name(movie->gVideoCodecCtx->pix_fmt));
	return 0;
}

int frameFinished = 0;
int buffer_size = AVCODEC_MAX_AUDIO_FRAME_SIZE + FF_INPUT_BUFFER_PADDING_SIZE;

uint8_t *buffer;
uint8_t *samples;

int openAudioStream(Movie *movie) 
{
	LOGD("openAudioStream gAudioStreamIdx=%d", movie->gAudioStreamIdx);

	// 오디오 스트림 인덱스를 체크한다. 
	if (movie->gAudioStreamIdx == -1)
		return -4;

	// 오디오 코텍을 찾아서 오픈한다. 
	movie->gAudioCodecCtx = movie->gFormatCtx->streams[movie->gAudioStreamIdx]->codec;
	movie->gAudioCodec = avcodec_find_decoder(movie->gAudioCodecCtx->codec_id);
	if (movie->gAudioCodec == NULL)
		return -5;

	if (avcodec_open2(movie->gAudioCodecCtx, movie->gAudioCodec, &movie->optionsDict) < 0)
		return -6;

	LOGD("openAudioStream gAudioCodecCtx->sample_fmt=%d", movie->gAudioCodecCtx->sample_fmt);
	LOGD("openAudioStream gAudioCodecCtx->sample_rate=%d", movie->gAudioCodecCtx->sample_rate);
	LOGD("openAudioStream gAudioCodecCtx->channels=%d", movie->gAudioCodecCtx->channels);

	movie->sfmt = movie->gAudioCodecCtx->sample_fmt;

	const char *audioFormat = getAudioFormatString(movie->sfmt);
	LOGD("openAudioStream audioFormat=%s", audioFormat);

	buffer = av_malloc(sizeof(uint8_t)*buffer_size);
	samples = av_malloc(sizeof(uint8_t)*buffer_size);
}

void decodeAudio(Movie *movie, AVPacket packet) {
			int64_t begin = getTimeNsec();
 			int len = avcodec_decode_audio4(movie->gAudioCodecCtx, movie->gFrameAudio, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			if(len < 0) {
				LOGD("skip audio");
			}
			
 			// 이게 전부 0.0에서 변화가 없음
 			double pts = av_frame_get_best_effort_timestamp(movie->gFrameAudio);
 			double pts_clock = pts * av_q2d(movie->gFormatCtx->streams[movie->gAudioStreamIdx]->time_base);

 			if (frameFinished) {
                int write_p = 0;
				int plane_size;
				int data_size = av_samples_get_buffer_size(&plane_size, movie->gAudioCodecCtx->channels, movie->gFrameAudio->nb_samples, movie->gAudioCodecCtx->sample_fmt, 1);
				uint16_t nb, ch;

				LOGD("movie->sfmt=%d", movie->sfmt);

				if(movie->sfmt == AV_SAMPLE_FMT_S16P) {
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(uint16_t); nb++) {
						for (ch = 0; ch < movie->gAudioCodecCtx->channels; ch++) {
							out[write_p] = ((uint16_t *) movie->gFrameAudio->extended_data[ch])[nb];
							write_p++;
						}
					}
					writeAudioTrack(samples, plane_size * movie->gAudioCodecCtx->channels);
				}
				else if(movie->sfmt == AV_SAMPLE_FMT_FLTP) {
					// LOGD("decodeAudioThread AV_SAMPLE_FMT_FLTP");
					// resample: float -> short
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(float); nb++) {
						for (ch = 0; ch < movie->gAudioCodecCtx->channels; ch++) {
							out[write_p] = (short)(((float *) movie->gFrameAudio->extended_data[ch])[nb] * SHRT_MAX);
							write_p++;
						}
					}
					writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t) * movie->gAudioCodecCtx->channels);
				}
				else if(movie->sfmt == AV_SAMPLE_FMT_U8P) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++) {
                        for (ch = 0; ch < movie->gFrameAudio->channels; ch++) {
                            out[write_p] = (((uint8_t *) movie->gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127;
                            write_p++;
                        }
                    }
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t) * movie->gAudioCodecCtx->channels);
				}

				// 채널 구분이 없음 
				else if(movie->sfmt == AV_SAMPLE_FMT_S16) {
					writeAudioTrack((char*)movie->gFrameAudio->extended_data[0], movie->gFrameAudio->linesize[0]);
				}
				else if(movie->sfmt == AV_SAMPLE_FMT_FLT) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(float); nb++) {
                        out[nb] = (short) ( ((float *) movie->gFrameAudio->extended_data[0])[nb] * SHRT_MAX);
                    }
                    writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t));
				}
				else if(movie->sfmt == AV_SAMPLE_FMT_U8) {
					uint16_t *out = (uint16_t *)samples;
                    for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++) {
                        out[nb] = (short) ( (((uint8_t *) movie->gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127);
                    }					
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t));	
				}

				av_packet_unref(&packet);
 			}
			else {
				av_packet_unref(&packet);
			}

}


void* decodeAudioThread(void *param) 
{
	LOGD("decodeAudioThread BEGIN");
	Movie *movie = (Movie*)param;


	while(movie->gAudioThreadRunning) {
		AudioQ_lock();
		size_t size = AudioQ_size();
		AudioQ_unlock();

		if(size > 0) {
			AudioQ_lock();
			AVPacket packet = AudioQ_pop();
			AudioQ_unlock();

			decodeAudio(movie, packet);
		}
		//usleep(1);
	}

	LOGW("decodeAudioThread END");

	av_free(buffer);
	av_free(samples);
	return NULL;
}

int openMovieWithAudio(Movie *movie, const char *filePath, int audio, int width, int height)
{
	LOGD("openMovieWithAudio filePath=%s audio=%d thread=%d width=%d height=%d", filePath, audio, movie->gAudioThreadRunning, width, height);

	int i;
	unsigned char errbuf[128];
	
	// 최초에 컨텍스트가 null이 맞는지 확인한다. 
	if (movie->gFormatCtx != NULL)
		return -1;

	// 파일을 연다. 
	int err = avformat_open_input(&movie->gFormatCtx, filePath, NULL, NULL);
	if(err < 0) {
		av_strerror(err, errbuf, sizeof(errbuf));
		LOGD("%s", errbuf);  
		return -2;
	}

	// 스트짐 정보를 포맷 컨텍스트에 리턴한다. 
	if (avformat_find_stream_info(movie->gFormatCtx, NULL) < 0)
		return -3;

	for (i = 0; i < movie->gFormatCtx->nb_streams; i++) {
		if (movie->gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			movie->gVideoStreamIdx = i;
			LOGD("gVideoStreamIdx=%d", movie->gVideoStreamIdx);
		}

		if (movie->gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
			movie->gAudioStreamIdx = i;
			LOGD("gAudioStreamIdx=%d", movie->gAudioStreamIdx);
		}		
	}

	int ret;
	ret = openVideoStream(movie, width, height);
	if(ret < 0)
		return ret;  

	if(audio) {
		// 오디오는 없을수 있다. 
		ret = openAudioStream(movie);
		if(ret < 0) {
			LOGD("Audio NOT FOUND");
			return 0;
		}
		else {
			prepareAudioTrack(movie->gAudioCodecCtx->sample_fmt, movie->gAudioCodecCtx->sample_rate, movie->gAudioCodecCtx->channels);
			// movie->gAudioThreadRunning = 1;
			// ret = pthread_create(&movie->gAudioThread, NULL, decodeAudioThread, movie);
		}		
	}

	return ret;
}

int openMovie(Movie *movie, const char filePath[], int width, int height)
{
	LOGD("openMovie filePath=%s width=%d height=%d", filePath, width, height);

	return openMovieWithAudio(movie, filePath, 1, width, height);
}

long getMicrotime(){
	struct timeval currentTime;
	gettimeofday(&currentTime, NULL);
	return currentTime.tv_sec * (int)1e6 + currentTime.tv_usec;
}

// 40ms만에 한번씩 호출된다. 
int decodeFrame(Movie *movie)
{
	int frameFinished = 0;
	AVPacket packet;

	if(movie->gFormatCtx == NULL) {
		LOGD("decodeFrame END");
		return -1;
	}
	
	// 한번에 하나를 읽고 종료하자 
	while (av_read_frame(movie->gFormatCtx, &packet) >= 0) {
		if (packet.stream_index == movie->gVideoStreamIdx) {
			int64_t begin = getTimeNsec();
			avcodec_decode_video2(movie->gVideoCodecCtx, movie->gFrame, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			// 이게 전부 0.0에서 변화가 없음
			int64_t pts = av_frame_get_best_effort_timestamp(movie->gFrame);
//			double pts_clock = pts * av_q2d(gFormatCtx->streams[gVideoStreamIdx]->time_base);
			movie->gCurrentTimeUs = av_rescale_q(pts, movie->gFormatCtx->streams[movie->gVideoStreamIdx]->time_base, AV_TIME_BASE_Q);
//			LOGD("pts=%f pts_clock=%f pts_long=%lu", pts, pts_clock, pts_long);

			if (frameFinished) {
				// 이미지 컨버트 컨텍스트를 받는다. 없으면 새로 생성
				long us;
				us = getMicrotime();
				//LOGD("sws_getCachedContext BEGIN %ld", us);
				// movie->gImgConvertCtx = sws_getCachedContext(movie->gImgConvertCtx,
				// 	movie->gVideoCodecCtx->width,
				// 	movie->gVideoCodecCtx->height,
				// 	movie->gVideoCodecCtx->pix_fmt,
				// 	// 원래 쓰던 파라미터
				// 	//movie->gVideoCodecCtx->width, movie->gVideoCodecCtx->height, movie->gPixelFormat, SWS_BICUBIC, NULL, NULL, NULL);
				// 	// 새로운 파라미터: target width/height 추가, fast bilinear 변경
				// 	movie->gTargetWidth,
				// 	movie->gTargetHeight,
				// 	movie->gPixelFormat,
				// 	SWS_FAST_BILINEAR, NULL, NULL, NULL);

				// us = getMicrotime() - us;

				// LOGD("sws_getCachedContext END us=%ld gVideoCodecCtx->pix_fmt=%ld gPixelFormat=%ld", us, movie->gVideoCodecCtx->pix_fmt, movie->gPixelFormat);// 이거는 솔직히 시간이 안걸림
				// LOGD("gVideoCodecCtx->width=%ld gVideoCodecCtx->height=%ld movie->gTargetWidth=%ld movie->gTargetHeight=%ld",
				// 	movie->gVideoCodecCtx->width, movie->gVideoCodecCtx->height, movie->gTargetWidth, movie->gTargetHeight);// 이거는 솔직히 시간이 안걸림

				// 실제로 scale을 하면서 픽셀포맷도 변경한다.
				// us = getMicrotime();

				//LOGD("sws_scale BEGIN %ld", us);
				// sws_scale(movie->gImgConvertCtx, (const uint8_t * const*)movie->gFrame->data, movie->gFrame->linesize, 0, movie->gVideoCodecCtx->height, movie->gFrameRGB->data, movie->gFrameRGB->linesize);

				// for(int i=0; i<AV_NUM_DATA_POINTERS; i++) {
				// 	LOGD("movie->gFrame->linesize[%d]=%d movie->gFrameRGB->linesize[%d]=%d", i, movie->gFrame->linesize[i], i, movie->gFrameRGB->linesize[i]);
				// }
				// us = getMicrotime() - us;
				// LOGD("sws_scale END us=%ld gFrame->linesize=%lld movie->gFrameRGB->linesize=%lld", us, movie->gFrame->linesize, movie->gFrameRGB->linesize);
				
				av_packet_unref(&packet);
				return 0;
			}
			else {
				av_packet_unref(&packet);
			}
		}
		else if(packet.stream_index == movie->gAudioStreamIdx) {
			decodeAudio(movie, packet);
			//TODO: 큐 동기화가 필요함 
			// if(movie->gAudioThread != 0) {
			// 	AudioQ_lock();
			// 	AudioQ_push(packet);
			// 	AudioQ_unlock();
			// }
		}
		else {
			// 처리하지 못했을때 자체적으로 packet을 free 함 
			av_packet_unref(&packet);
		}
		//usleep(100);
	}

	LOGD("decodeFrame END");
	return -1;
}

void copyPixelsYUV(Movie *movie, uint8_t *pixelsY, uint8_t *pixelsU, uint8_t *pixelsV) {
    LOGD("copyPixelsYUV BEGIN %d %d linesize=[%d %d %d]", movie->gFrame->data, movie->gVideoPictureSize, movie->gFrame->linesize[0], movie->gFrame->linesize[1], movie->gFrame->linesize[2]);
    memcpy(pixelsY, movie->gFrame->data[0], movie->gVideoPictureSize*2/3);// Y
    memcpy(pixelsU, movie->gFrame->data[1], movie->gVideoPictureSize/6);// U
    memcpy(pixelsV, movie->gFrame->data[2], movie->gVideoPictureSize/6);// V
    LOGD("copyPixelsYUV END");
}

void copyPixels(Movie *movie, uint8_t *pixels)
{
	memcpy(pixels, movie->gFrameRGB->data[0], movie->gPictureSize);
}

int getWidth(Movie *movie)
{
	return movie->gVideoCodecCtx->width;
}

int getHeight(Movie *movie)
{
	return movie->gVideoCodecCtx->height;
}

void closeFrame(Movie *movie) 
{
	if (movie->gFrame != NULL) {
		av_frame_free(&movie->gFrame);
		movie->gFrame = NULL;
	}
	LOGD("closeMovie gFrame");

	if (movie->gFrameRGB != NULL) {
		av_frame_free(&movie->gFrameRGB);
		movie->gFrameRGB = NULL;
	}
	LOGD("closeMovie gFrameRGB");

	if (movie->gFrameAudio != NULL) {
		av_frame_free(&movie->gFrameAudio);
		movie->gFrameAudio = NULL;
	}
	LOGD("closeMovie gFrameAudio");
}

void closeMovie(Movie *movie)
{
	int status;

	LOGD("closeMovie BEGIN");

	if (movie->gVideoBuffer != NULL) {
		free(movie->gVideoBuffer);
		movie->gVideoBuffer = NULL;
	}
	LOGD("closeMovie gVideoBuffer");
	
	if (movie->gVideoCodecCtx != NULL) {
		avcodec_close(movie->gVideoCodecCtx);
		movie->gVideoCodecCtx = NULL;
	}
	LOGD("closeMovie gVideoCodecCtx");

	closeFrame(movie);

	if (movie->gFormatCtx != NULL) {
        avformat_close_input(&movie->gFormatCtx);
		movie->gFormatCtx = NULL;
	}
	LOGD("closeMovie gFormatCtx");

	movie->gVideoStreamIdx = -1;
	
	//BEGIN Audio 
	// audio = 1일 경우에만 존재함 
	if(movie->gAudioThreadRunning) {
		movie->gAudioThreadRunning = 0;

		pthread_join(movie->gAudioThread, (void**)&status);
		movie->gAudioThread = 0;		
	}
	
	if(movie->gAudioCodecCtx != NULL) {
		avcodec_close(movie->gAudioCodecCtx);
		movie->gAudioCodecCtx = NULL;
	}
	LOGW("closeMovie gAudioCodecCtx");
	movie->gAudioStreamIdx = -1;
	
	av_free(buffer);
	av_free(samples);

	AudioQ_lock();
	AudioQ_clear();
	AudioQ_unlock();	
	//END Audio 

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

int seekMovie(Movie *movie, int64_t positionUs) 
{
//	LOGD("seekMovie positionUs=%lld", positionUs);

	// 프레임을 해당 시간으로 이동시킴
	int64_t seekTarget = av_rescale_q(positionUs, AV_TIME_BASE_Q, movie->gFormatCtx->streams[movie->gVideoStreamIdx]->time_base);
//	LOGD("seekMovie seekTarget=%lld", seekTarget);

	if(av_seek_frame(movie->gFormatCtx, movie->gVideoStreamIdx, seekTarget, AVSEEK_FLAG_FRAME) < 0) {
        LOGD("FAILED av_seek_frame");
        return -1;
	}

	// 오디오 큐를 비운다. 
	AudioQ_lock();
	AudioQ_clear();
	AudioQ_unlock();
	return 0;
}

int64_t getDuration(Movie *movie) 
{
	// 이건 믿으면 안됨
	// LOGD("gFormatCtx->duration=%lu", gFormatCtx->duration);
	LOGD("gFormatCtx->nb_streams=%d", movie->gFormatCtx->nb_streams);

	int i;
	for(i=0; i<movie->gFormatCtx->nb_streams; i++) {
		//AVStream* stream = gFormatCtx->streams[i];
		AVStream* stream;
		if(i == 0) {
			stream = movie->gFormatCtx->streams[movie->gVideoStreamIdx];
		}
		else {
			stream = movie->gFormatCtx->streams[movie->gAudioStreamIdx];
		}
		
		LOGD("stream->duration=%lld", stream->duration);
		if(stream->duration > 0) {
			int64_t duration = av_rescale_q(stream->duration, stream->time_base, AV_TIME_BASE_Q);
			LOGD("duration=%lld", duration);

			if(duration != 0)
				return duration;
		}
	}

	LOGD("gFormatCtx->duration=%lld", movie->gFormatCtx->duration);
	if(movie->gFormatCtx->duration > 0) {
		return movie->gFormatCtx->duration;		
	}

	return 0ll;
}

int64_t getPosition(Movie *movie)
{
	return movie->gCurrentTimeUs;
}