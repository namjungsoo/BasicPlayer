#ifndef __AUDIOQ_H__
#define __AUDIOQ_H__


#ifdef __cplusplus
extern "C" {
#endif

#include <libavcodec/avcodec.h>

void AudioQ_init();
void AudioQ_lock();
void AudioQ_unlock();

AVPacket AudioQ_pop();
void AudioQ_push(AVPacket packet);
size_t AudioQ_size();
void AudioQ_clear();

#ifdef __cplusplus
}
#endif

#endif//__AUDIOQ_H__