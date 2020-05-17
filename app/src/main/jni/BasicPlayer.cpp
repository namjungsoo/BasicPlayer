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

#ifdef __cplusplus
extern "C"
{
#endif
// ffmpeg lib
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

#include <libavutil/imgutils.h> // av_image_fill_arrays, av_image_get_buffer_size
#include <libavutil/mem.h>
#ifdef __cplusplus
}
#endif

// android lib
#include <android/log.h>
#include <jni.h> // JNI_OnLoad

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
	return (int64_t)now.tv_sec * 1000000000LL + now.tv_nsec;
}

// 구조체를 초기화한다.
void initMovie(Movie *movie)
{
	memset(movie, 0, sizeof(Movie));

	movie->gPixelFormat = AV_PIX_FMT_BGR32;
	movie->gVideoStreamIdx = -1;	// 인덱스 INVALID
	movie->gAudioStreamIdx = -1;	// 인덱스 INVALID
	movie->gAudioThreadRunning = 0; // 쓰레드 중지 상태
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
    LOGD("openVideoStream -4");

	// 비디오 코텍을 찾아서 오픈한다.
	movie->gVideoCodecCtx = movie->gFormatCtx->streams[movie->gVideoStreamIdx]->codec;
	movie->gVideoCodec = avcodec_find_decoder(movie->gVideoCodecCtx->codec_id);
	if (movie->gVideoCodec == NULL)
		return -5;
	LOGD("openVideoStream -5");

	if (avcodec_open2(movie->gVideoCodecCtx, movie->gVideoCodec, NULL) < 0)
		return -6;
    LOGD("openVideoStream -6");

	// 프레임을 할당한다. frame은 원본 frameRGB는 변환용
	movie->gFrame = av_frame_alloc();
	if (movie->gFrame == NULL)
		return -7;
	LOGD("openVideoStream -7");

	movie->gFrameRGB = av_frame_alloc();
	if (movie->gFrameRGB == NULL)
		return -8;
    LOGD("openVideoStream -8");

	// 오디오를 위해 추가됨
	movie->gFrameAudio = av_frame_alloc();
	if (movie->gFrameAudio == NULL)
		return -9;
    LOGD("openVideoStream -9");

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
    LOGD("openVideoStream av_image_get_buffer_size");

	// 비디오 버퍼를 할당한다.
	movie->gVideoBuffer = (uint8_t *)(malloc(sizeof(uint8_t) * movie->gPictureSize));
    LOGD("openVideoStream gVideoBuffer");

	//TODO: 입력받은 width, height로 동작하게 설정
	// 비디오 버퍼 메모리(gFrameRGB)를 설정함
	av_image_fill_arrays(movie->gFrameRGB->data, movie->gFrameRGB->linesize, movie->gVideoBuffer, movie->gPixelFormat, movie->gTargetWidth, movie->gTargetHeight, 1);
    LOGD("openVideoStream av_image_fill_arrays");

	LOGD("gFrame->linesize=%d gFrameRGB->linesize=%d", movie->gFrame->linesize, movie->gFrameRGB->linesize);
	movie->gFps = av_q2d(movie->gFormatCtx->streams[movie->gVideoStreamIdx]->r_frame_rate);
	LOGD("fps=%f", movie->gFps);

	LOGD("pix_fmt=%d(%s)", movie->gVideoCodecCtx->pix_fmt, av_get_pix_fmt_name(movie->gVideoCodecCtx->pix_fmt));

	movie->gData[0] = (uint8_t *)(malloc(sizeof(uint8_t) * getWidth(movie) * getHeight(movie)));
	movie->gData[1] = (uint8_t *)(malloc(sizeof(uint8_t) * getWidth(movie) * getHeight(movie) / 4));
	movie->gData[2] = (uint8_t *)(malloc(sizeof(uint8_t) * getWidth(movie) * getHeight(movie) / 4));

	return 0;
}

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

	if (avcodec_open2(movie->gAudioCodecCtx, movie->gAudioCodec, NULL) < 0)
		return -6;

	LOGD("gAudioCodecCtx->sample_fmt=%d", movie->gAudioCodecCtx->sample_fmt);
	LOGD("gAudioCodecCtx->sample_rate=%d", movie->gAudioCodecCtx->sample_rate);
	LOGD("gAudioCodecCtx->channels=%d", movie->gAudioCodecCtx->channels);

	//movie->sfmt = movie->gAudioCodecCtx->sample_fmt;

	const char *audioFormat = getAudioFormatString(movie->gAudioCodecCtx->sample_fmt);
	LOGD("audioFormat=%s", audioFormat);
	return 0;
}

void *decodeAudioThread(void *param)
{
	LOGD("decodeAudioThread BEGIN new");
	LOGD("decodeAudioThread BEGIN %d %llu", param, param);
	Movie *movie = (Movie *)param;
	int frameFinished = 0;
	LOGD("decodeAudioThread movie=%llu %d", movie, movie->gAudioThreadRunning);

	int buffer_size = AVCODEC_MAX_AUDIO_FRAME_SIZE + FF_INPUT_BUFFER_PADDING_SIZE;
	LOGD("decodeAudioThread buffer_size=%d", buffer_size);

	uint8_t *buffer = (uint8_t *)av_malloc(sizeof(uint8_t) * buffer_size);
	uint8_t *samples = (uint8_t *)av_malloc(sizeof(uint8_t) * buffer_size);
    LOGD("decodeAudioThread buffer, samples");

	while (movie->gAudioThreadRunning)
	{
	    LOGD("decodeAudioThread size BEGIN");
		AudioQ_lock();
		size_t size = AudioQ_size();
		AudioQ_unlock();

        LOGD("decodeAudioThread size END");

		if (size > 0)
		{
			AudioQ_lock();
			AVPacket packet = AudioQ_pop();
			AudioQ_unlock();
            LOGD("decodeAudioThread pop");

			int64_t begin = getTimeNsec();
			int len = avcodec_decode_audio4(movie->gAudioCodecCtx, movie->gFrameAudio, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;
			LOGD("decodeAudioThread avcodec_decode_audio4");

			if (len < 0)
			{
				LOGD("skip audio");
			}

			// 이게 전부 0.0에서 변화가 없음
			double pts = av_frame_get_best_effort_timestamp(movie->gFrameAudio);
			LOGD("decodeAudioThread pts");
			double pts_clock = pts * av_q2d(movie->gFormatCtx->streams[movie->gAudioStreamIdx]->time_base);
			LOGD("decodeAudioThread pts_clock");

			if (frameFinished)
			{
				int write_p = 0;
				int plane_size;
				int data_size = av_samples_get_buffer_size(&plane_size, movie->gAudioCodecCtx->channels, movie->gFrameAudio->nb_samples, movie->gAudioCodecCtx->sample_fmt, 1);
				uint16_t nb, ch;
                LOGD("decodeAudioThread av_samples_get_buffer_size");

				if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_S16P)
				{
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(uint16_t); nb++)
					{
						for (ch = 0; ch < movie->gAudioCodecCtx->channels; ch++)
						{
							out[write_p] = ((uint16_t *)movie->gFrameAudio->extended_data[ch])[nb];
							write_p++;
						}
					}
					writeAudioTrack(samples, plane_size * movie->gAudioCodecCtx->channels);
					LOGD("decodeAudioThread AV_SAMPLE_FMT_S16P");
				}
				else if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_FLTP)
				{
					// LOGD("decodeAudioThread AV_SAMPLE_FMT_FLTP");
					// resample: float -> short
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(float); nb++)
					{
						for (ch = 0; ch < movie->gAudioCodecCtx->channels; ch++)
						{
							out[write_p] = (short)(((float *)movie->gFrameAudio->extended_data[ch])[nb] * SHRT_MAX);
							write_p++;
						}
					}
					writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t) * movie->gAudioCodecCtx->channels);
					LOGD("decodeAudioThread AV_SAMPLE_FMT_FLTP");
				}
				else if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_U8P)
				{
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++)
					{
						for (ch = 0; ch < movie->gFrameAudio->channels; ch++)
						{
							out[write_p] = (((uint8_t *)movie->gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127;
							write_p++;
						}
					}
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t) * movie->gAudioCodecCtx->channels);
					LOGD("decodeAudioThread AV_SAMPLE_FMT_U8P");
				}

				// 채널 구분이 없음
				else if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_S16)
				{
					writeAudioTrack(movie->gFrameAudio->extended_data[0], movie->gFrameAudio->linesize[0]);
					LOGD("decodeAudioThread AV_SAMPLE_FMT_S16");
				}
				else if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_FLT)
				{
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(float); nb++)
					{
						out[nb] = (short)(((float *)movie->gFrameAudio->extended_data[0])[nb] * SHRT_MAX);
					}
					writeAudioTrack(samples, (plane_size / sizeof(float)) * sizeof(uint16_t));
					LOGD("decodeAudioThread AV_SAMPLE_FMT_FLT");
				}
				else if (movie->gAudioCodecCtx->sample_fmt == AV_SAMPLE_FMT_U8)
				{
					uint16_t *out = (uint16_t *)samples;
					for (nb = 0; nb < plane_size / sizeof(uint8_t); nb++)
					{
						out[nb] = (short)((((uint8_t *)movie->gFrameAudio->extended_data[0])[nb] - 127) * SHRT_MAX / 127);
					}
					writeAudioTrack(samples, (plane_size / sizeof(uint8_t)) * sizeof(uint16_t));
					LOGD("decodeAudioThread AV_SAMPLE_FMT_U8");
				}

				av_packet_unref(&packet);
			}
			else
			{
				av_packet_unref(&packet);
			}
		}
		usleep(1);
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
	char errbuf[128];

	// 최초에 컨텍스트가 null이 맞는지 확인한다.
	if (movie->gFormatCtx != NULL)
		return -1;

	// 파일을 연다.
	int err = avformat_open_input(&movie->gFormatCtx, filePath, NULL, NULL);
	if (err < 0)
	{
		av_strerror(err, errbuf, sizeof(errbuf));
		LOGD("%s", errbuf);
		return -2;
	}

	// 스트짐 정보를 포맷 컨텍스트에 리턴한다.
	if (avformat_find_stream_info(movie->gFormatCtx, NULL) < 0)
		return -3;

	for (i = 0; i < movie->gFormatCtx->nb_streams; i++)
	{
		if (movie->gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
		{
			movie->gVideoStreamIdx = i;
			LOGD("gVideoStreamIdx=%d", movie->gVideoStreamIdx);
		}

		if (movie->gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			movie->gAudioStreamIdx = i;
			LOGD("gAudioStreamIdx=%d", movie->gAudioStreamIdx);
		}
	}

	int ret;
	ret = openVideoStream(movie, width, height);
	if (ret < 0)
		return ret;

	if (audio)
	{
		LOGD("openMovieWithAudio movie=%llu", movie);
		void* param = movie;
		LOGD("openMovieWithAudio movie=%llu %d", param, param);

		// 오디오는 없을수 있다.
		LOGD("openAudioStream BEGIN");
		ret = openAudioStream(movie);
		LOGD("openAudioStream END");

		if (ret < 0)
		{
			LOGD("Audio NOT FOUND");
			return 0;
		}
		else
		{
			LOGD("openMovieWithAudio prepareAudioTrack");
			prepareAudioTrack(movie->gAudioCodecCtx->sample_fmt, movie->gAudioCodecCtx->sample_rate, movie->gAudioCodecCtx->channels);
			movie->gAudioThreadRunning = 1;
			LOGD("openMovieWithAudio prepareAudioTrack END");

			LOGD("openMovieWithAudio decodeAudioThread movie=%llu %d", param, param);
			ret = pthread_create(&movie->gAudioThread, NULL, decodeAudioThread, movie);

#if THREAD_RENDER == 1
			LOGD("THREAD_RENDER decodeFrameThread");
			movie->gFrameThreadRunning = 1;
			ret = pthread_create(&movie->gFrameThread, NULL, decodeFrameThread, (void*)movie);
#endif
		}
	}

	return ret;
}

int openMovie(Movie *movie, const char filePath[], int width, int height)
{
	LOGD("openMovie filePath=%s width=%d height=%d", filePath, width, height);

	return openMovieWithAudio(movie, filePath, 1, width, height);
}

long getMicrotime()
{
	struct timeval currentTime;
	gettimeofday(&currentTime, NULL);
	return currentTime.tv_sec * (int)1e6 + currentTime.tv_usec;
}

// 40ms만에 한번씩 호출된다.
int decodeFrame(Movie *movie)
{
	int frameFinished = 0;
	AVPacket packet;

	if (movie->gFormatCtx == NULL)
	{
		LOGD("decodeFrame END");
		return -1;
	}

	// 한번에 하나를 읽고 종료하자
	while (av_read_frame(movie->gFormatCtx, &packet) >= 0)
	{
		if (packet.stream_index == movie->gVideoStreamIdx)
		{
			int64_t begin = getTimeNsec();
			avcodec_decode_video2(movie->gVideoCodecCtx, movie->gFrame, &frameFinished, &packet);
			int64_t end = getTimeNsec();
			int64_t diff = end - begin;

			// 이게 전부 0.0에서 변화가 없음
			int64_t pts = av_frame_get_best_effort_timestamp(movie->gFrame);
			//			double pts_clock = pts * av_q2d(gFormatCtx->streams[gVideoStreamIdx]->time_base);
			movie->gCurrentTimeUs = av_rescale_q(pts, movie->gFormatCtx->streams[movie->gVideoStreamIdx]->time_base, AV_TIME_BASE_Q);
			//			LOGD("pts=%f pts_clock=%f pts_long=%lu", pts, pts_clock, pts_long);

			if (frameFinished)
			{
				//TODO: 이부분이 성능이 느리다.
				// 이미지 컨버트 컨텍스트를 받는다. 없으면 새로 생성

				long us;
				us = getMicrotime();
				//LOGD("sws_getCachedContext BEGIN %ld", us);
				movie->gImgConvertCtx = sws_getCachedContext(movie->gImgConvertCtx,
															 movie->gVideoCodecCtx->width, movie->gVideoCodecCtx->height, movie->gVideoCodecCtx->pix_fmt,
															 //movie->gVideoCodecCtx->width, movie->gVideoCodecCtx->height, movie->gPixelFormat, SWS_BICUBIC, NULL, NULL, NULL);
															 movie->gTargetWidth, movie->gTargetHeight, movie->gPixelFormat, SWS_FAST_BILINEAR, NULL, NULL, NULL);
				us = getMicrotime() - us;
				LOGD("sws_getCachedContext END %ld", us);

				// 실제로 scale을 하면서 픽셀포맷도 변경한다.
				us = getMicrotime();
				//LOGD("sws_scale BEGIN %ld", us);
				sws_scale(movie->gImgConvertCtx, (const uint8_t *const *)movie->gFrame->data, movie->gFrame->linesize, 0, movie->gVideoCodecCtx->height, movie->gFrameRGB->data, movie->gFrameRGB->linesize);

				// for(int i=0; i<AV_NUM_DATA_POINTERS; i++) {
				// 	LOGD("movie->gFrame->linesize[%d]=%d movie->gFrameRGB->linesize[%d]=%d", i, movie->gFrame->linesize[i], i, movie->gFrameRGB->linesize[i]);
				// }
				us = getMicrotime() - us;
				LOGD("sws_scale END %ld", us);

				av_packet_unref(&packet);
				return 0;
			}
			else
			{
				av_packet_unref(&packet);
			}
		}
		else if (packet.stream_index == movie->gAudioStreamIdx)
		{
			//TODO: 큐 동기화가 필요함
			if (movie->gAudioThread != 0)
			{
				AudioQ_lock();
				AudioQ_push(packet);
				AudioQ_unlock();
			}
		}
		else
		{
			// 처리하지 못했을때 자체적으로 packet을 free 함
			av_packet_unref(&packet);
		}
		usleep(100);
	}

	LOGD("decodeFrame END");
	return -1;
}

void copyPixels(Movie *movie, uint8_t *pixels)
{
	// 테스트로 gFrameRGB로 memcpy 해봄
	// 여기서는 크래시 발생
	//	memcpy(pixels, movie->gFrame->data[0], movie->gPictureSize);

	//ORG
	memcpy(pixels, movie->gFrameRGB->data[0], movie->gPictureSize);

	//NEW
	// int pictureSize = av_image_get_buffer_size(movie->gPixelFormat, width, height, 1);
	// memcpy(pixels, movie->gFrameRGB->data[0], pictureSize);
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
	if (movie->gFrame != NULL)
	{
		av_frame_free(&movie->gFrame);
		movie->gFrame = NULL;
	}
	LOGD("closeMovie gFrame");

	if (movie->gFrameRGB != NULL)
	{
		av_frame_free(&movie->gFrameRGB);
		movie->gFrameRGB = NULL;
	}
	LOGD("closeMovie gFrameRGB");

	if (movie->gFrameAudio != NULL)
	{
		av_frame_free(&movie->gFrameAudio);
		movie->gFrameAudio = NULL;
	}
	LOGD("closeMovie gFrameAudio");
}

void closeMovie(Movie *movie)
{
	LOGD("closeMovie BEGIN");

	// frame thread를 종료시킨다
	if (movie->gFrameThreadRunning)
	{
		movie->gFrameThreadRunning = 0;

		LOGD("closeMovie gFrameThread=%d", movie->gFrameThread);
		pthread_join(movie->gFrameThread, NULL);
		movie->gFrameThread = 0;
	}

	free(movie->gData[0]);
	free(movie->gData[1]);
	free(movie->gData[2]);

	if (movie->gVideoBuffer != NULL)
	{
		free(movie->gVideoBuffer);
		movie->gVideoBuffer = NULL;
	}
	LOGD("closeMovie gVideoBuffer");

	if (movie->gVideoCodecCtx != NULL)
	{
		avcodec_close(movie->gVideoCodecCtx);
		movie->gVideoCodecCtx = NULL;
	}
	LOGD("closeMovie gVideoCodecCtx");

	closeFrame(movie);

	if (movie->gFormatCtx != NULL)
	{
		avformat_close_input(&movie->gFormatCtx);
		movie->gFormatCtx = NULL;
	}
	LOGD("closeMovie gFormatCtx");

	movie->gVideoStreamIdx = -1;

	//BEGIN Audio
	// audio = 1일 경우에만 존재함
	if (movie->gAudioThreadRunning)
	{
		movie->gAudioThreadRunning = 0;

		LOGD("closeMovie gAudioThread=%d", movie->gAudioThread);
		pthread_join(movie->gAudioThread, NULL);
		movie->gAudioThread = 0;
	}
	if (movie->gAudioCodecCtx != NULL)
	{
		avcodec_close(movie->gAudioCodecCtx);
		movie->gAudioCodecCtx = NULL;
	}
	LOGW("closeMovie gAudioCodecCtx");
	movie->gAudioStreamIdx = -1;

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

	if (av_seek_frame(movie->gFormatCtx, movie->gVideoStreamIdx, seekTarget, AVSEEK_FLAG_FRAME) < 0)
	{
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
	for (i = 0; i < movie->gFormatCtx->nb_streams; i++)
	{
		//AVStream* stream = gFormatCtx->streams[i];
		AVStream *stream;
		if (i == 0)
		{
			stream = movie->gFormatCtx->streams[movie->gVideoStreamIdx];
		}
		else
		{
			stream = movie->gFormatCtx->streams[movie->gAudioStreamIdx];
		}

		LOGD("stream->duration=%lld", stream->duration);
		if (stream->duration > 0)
		{
			int64_t duration = av_rescale_q(stream->duration, stream->time_base, AV_TIME_BASE_Q);
			LOGD("duration=%lld", duration);

			if (duration != 0)
				return duration;
		}
	}

	LOGD("gFormatCtx->duration=%lld", movie->gFormatCtx->duration);
	if (movie->gFormatCtx->duration > 0)
	{
		return movie->gFormatCtx->duration;
	}

	return 0ll;
}

int64_t getPosition(Movie *movie)
{
	return movie->gCurrentTimeUs;
}

// frame thread
void *decodeFrameThread(void *param)
{
	LOGD("decodeFrameThread BEGIN");
	Movie *movie = (Movie *)param;
	LOGD("decodeFrameThread movie=%llu", movie);
	AVPacket packet;
	int frameFinished = 0;

	// 무한으로 돌린다
	while (movie->gFrameThreadRunning && av_read_frame(movie->gFormatCtx, &packet) >= 0)
	{
		LOGD("decodeFrameThread av_read_frame");
		if (packet.stream_index == movie->gVideoStreamIdx)
		{
			avcodec_decode_video2(movie->gVideoCodecCtx, movie->gFrame, &frameFinished, &packet);
			LOGD("decodeFrameThread avcodec_decode_video2");

			int64_t pts = av_frame_get_best_effort_timestamp(movie->gFrame);
			movie->gCurrentTimeUs = av_rescale_q(pts, movie->gFormatCtx->streams[movie->gVideoStreamIdx]->time_base, AV_TIME_BASE_Q);
			LOGD("decodeFrameThread pts=%f movie->gCurrentTimeUs=%lu", pts, movie->gCurrentTimeUs);

			if (frameFinished)
			{
				copyFrameYUVTexData(movie);
				LOGD("avcodec_decode_video2 copyFrameYUVTexData");
				av_packet_unref(&packet);
			}
		}
		else if (packet.stream_index == movie->gAudioStreamIdx)
		{
			LOGD("decodeFrameThread gAudioStreamIdx");
			if (movie->gAudioThread != 0)
			{
				AudioQ_lock();
				AudioQ_push(packet);
				AudioQ_unlock();
			}
		}
		else
		{
			// 처리하지 못했을때 자체적으로 packet을 free 함
			av_packet_unref(&packet);
		}
		usleep(1);
	}

	LOGW("decodeFrameThread END");
}

// for OpenGL texture
void copyFrameYUVTexData(Movie *movie)
{
	LOGD("copyFrameYUVTexData BEGIN");
	int width = getWidth(movie);
	int height = getHeight(movie);

	memcpy(movie->gData[0], movie->gFrame->data[0], width * height);
	memcpy(movie->gData[1], movie->gFrame->data[1], width * height / 4);
	memcpy(movie->gData[2], movie->gFrame->data[2], width * height / 4);
	LOGD("copyFrameYUVTexData END");
}
