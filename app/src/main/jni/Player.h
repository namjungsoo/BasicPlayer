#ifndef __PLAYER_H__
#define __PLAYER_H__

#pragma once

class AVFormatContext;

class Video;
class Audio;

class Player {
public:
    Player();// init 포함 

    // width, height은 0이 아니라면 target 이고, 0이면 원본을 그대로 사용 
    int open(const char *path, int width, int height);
    int openWithAudio(const char *path, int audio, int width, int height);
    void close();

    // play
    int decodeFrame();
    void copyFrame(uint8_t *pixels);

    // control
    void pause();
    void resume();
    int seek(int64_t positionUs);

    // property
    int getWidth();
    int getHeight();

    int getFps();
    int64_t getDuration();
    int64_t getPosition();

private:
    AVFormatContext *formatCtx;
    int64_t currentTimeUs;

    Video *video;
    Audio *audio;
};

#endif //__PLAYER_H__