/*
 * Main functions of BasicPlayer
 * 2011-2011 Jaebong Lee (novaever@gmail.com)
 *
 * BasicPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

//#include "avcodec.h"
//#include "avformat.h"
//#include "swscale.h"
//#include "BasicPlayer.h"


AVFormatContext *gFormatCtx = NULL;

AVCodecContext *gVideoCodecCtx = NULL;
AVCodec *gVideoCodec = NULL;
int gVideoStreamIdx = -1;

AVFrame *gFrame = NULL;
AVFrame *gFrameRGB = NULL;

struct SwsContext *gImgConvertCtx = NULL;

int gPictureSize = 0;
uint8_t *gVideoBuffer = NULL;


AVDictionary    *optionsDict = NULL;


int openMovie(const char filePath[])
{
	int i;

	if (gFormatCtx != NULL)
		return -1;

	if (avformat_open_input(&gFormatCtx, filePath, NULL, NULL) != 0)
		return -2;

	if (avformat_find_stream_info(gFormatCtx, NULL) < 0)
		return -3;

	for (i = 0; i < gFormatCtx->nb_streams; i++) {
		if (gFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
			gVideoStreamIdx = i;

			break;
		}
	}
	if (gVideoStreamIdx == -1)
		return -4;

	gVideoCodecCtx = gFormatCtx->streams[gVideoStreamIdx]->codec;

	gVideoCodec = avcodec_find_decoder(gVideoCodecCtx->codec_id);
	if (gVideoCodec == NULL)
		return -5;

	if (avcodec_open2(gVideoCodecCtx, gVideoCodec, &optionsDict) < 0)
		return -6;

	gFrame = av_frame_alloc();
	if (gFrame == NULL)
		return -7;

	gFrameRGB = av_frame_alloc();
	if (gFrameRGB == NULL)
		return -8;

	gPictureSize = avpicture_get_size(AV_PIX_FMT_RGB565LE, gVideoCodecCtx->width, gVideoCodecCtx->height);
	gVideoBuffer = (uint8_t*)(malloc(sizeof(uint8_t) * gPictureSize));

	avpicture_fill((AVPicture*)gFrameRGB, gVideoBuffer, AV_PIX_FMT_RGB565LE, gVideoCodecCtx->width, gVideoCodecCtx->height);
	
	return 0;
}

int decodeFrame()
{
	int frameFinished = 0;
	AVPacket packet;
	
	while (av_read_frame(gFormatCtx, &packet) >= 0) {
		if (packet.stream_index == gVideoStreamIdx) {
			avcodec_decode_video2(gVideoCodecCtx, gFrame, &frameFinished, &packet);
			
			if (frameFinished) {
				gImgConvertCtx = sws_getCachedContext(gImgConvertCtx,
					gVideoCodecCtx->width, gVideoCodecCtx->height, gVideoCodecCtx->pix_fmt,
					gVideoCodecCtx->width, gVideoCodecCtx->height, AV_PIX_FMT_RGB565LE, SWS_BICUBIC, NULL, NULL, NULL);
				
				sws_scale(gImgConvertCtx, gFrame->data, gFrame->linesize, 0, gVideoCodecCtx->height, gFrameRGB->data, gFrameRGB->linesize);
				
				av_free_packet(&packet);
		
				return 0;
			}
		}
		
		av_free_packet(&packet);
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
