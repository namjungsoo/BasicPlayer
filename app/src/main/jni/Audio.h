
class AVCodecContext;
class AVCodec;
class AVFrame;

class Audio {
public:
    AVCodecContext *gAudioCodecCtx;
    AVCodec *gAudioCodec;
    AVFrame *gFrameAudio;

    int gAudioStreamIdx;

    pthread_t gAudioThread;
    int gAudioThreadRunning;

//    enum AVSampleFormat sfmt;
}