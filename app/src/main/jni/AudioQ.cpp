// std c++ lib
#include <queue>
#include "AudioQ.h"

std::queue<AVPacket> gAudioQ;

extern "C" {
    AVPacket AudioQ_pop() {
        AVPacket packet = gAudioQ.front();
        gAudioQ.pop();
        return packet;
    }
    void AudioQ_push(AVPacket packet) {
        gAudioQ.push(packet);
    }
    size_t AudioQ_size() {
        return gAudioQ.size();
    }
}
