#ifndef __PLAYER_H__
#define __PLAYER_H__

#pragma once

#include "SharedQueue.h"

#ifdef __cplusplus
extern "C"
{
#endif

#include <libavcodec/avcodec.h>
// #include <libavformat/avformat.h>
// #include <libswscale/swscale.h>
// #include <libavutil/pixfmt.h>

// #include <libavutil/imgutils.h> // av_image_fill_arrays, av_image_get_buffer_size
// #include <libavutil/mem.h>

#ifdef __cplusplus
}
#endif

struct AVFormatContext;

//class Video;
#include "Video.h"
class Audio;

class Player {
public:
    Player();// init 포함 

    // width, height은 0이 아니라면 target 이고, 0이면 원본을 그대로 사용 
    int open(const char *path, int isAudio=0, int targetWidth=0, int targetHeight=0);
    void close();

    // play
    int decodeFrame();// 수동으로 한 프레임 decode한다 
    void copyFrame(uint8_t *pixels);

    // play thread
    static void *decodeFrameThread(void *param);
    void copyFrameYUVTexData();
    void startThread();

    // control
    void pause();
    void resume();
    int seek(int64_t positionUs);

    // property
    int getWidth() {
        if(video)
            return video->getWidth();
        return 0;
    }
    int getHeight() {
        if(video)
            return video->getHeight();
        return 0;
    }

    int getFps() {
        if(video)
            return video->getFps();
        return 0;
    }
    int64_t getDuration() {
        
    }
    int64_t getPosition() {
        return currentTimeUs;
    }

private:
    AVFormatContext *formatCtx;
    int64_t currentTimeUs;

    Video *video;
    Audio *audio;

    SharedQueue<AVPacket> videoQueue;
    SharedQueue<AVPacket> audioQueue;
};

#endif //__PLAYER_H__