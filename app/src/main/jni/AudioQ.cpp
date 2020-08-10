// std c++ lib
#include <queue>
#include <pthread.h>
#include "AudioQ.h"

std::queue<AVPacket> gAudioQ;
pthread_mutex_t mutex;

void AudioQ_init() {
    pthread_mutex_init(&mutex, NULL);
}
void AudioQ_lock() {
    pthread_mutex_lock(&mutex); // 잠금을 생성한다. 
}
void AudioQ_unlock() {
    pthread_mutex_unlock(&mutex); // 잠금을 생성한다. 
}

AVPacket AudioQ_pop() {
    AVPacket packet = gAudioQ.front();
    gAudioQ.pop();
    return packet;
}
void AudioQ_push(AVPacket packet) {
    gAudioQ.push(packet);
}
size_t AudioQ_size() {
    size_t size = gAudioQ.size();
    return size;
}
void AudioQ_clear() {
    while(AudioQ_size()) {
        AVPacket packet = AudioQ_pop();
        av_packet_unref(&packet);
    }
}
