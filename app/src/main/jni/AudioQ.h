#ifndef __AUDIOQ_H__
#define __AUDIOQ_H__

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>
#include <libavutil/pixfmt.h>

#ifdef __cplusplus
extern "C" {
#endif
    AVPacket AudioQ_pop();
    void AudioQ_push(AVPacket packet);
    size_t AudioQ_size();
#ifdef __cplusplus
}
#endif

#endif//__AUDIOQ_H__