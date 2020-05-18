
class AVCodecContext;
class AVCodec;
class AVFrame;

class Video {
public:
    // 비디오 관련 
    AVCodecContext *gVideoCodecCtx;
    AVCodec *gVideoCodec;

    AVFrame *gFrame;
    AVFrame *gFrameRGB;

    SwsContext *gImgConvertCtx;

    int gVideoStreamIdx;
    int gPictureSize;
    uint8_t *gVideoBuffer;

    int gPixelFormat;
    double gFps;

    int gTargetWidth;
    int gTargetHeight;

    // 프레임 디코딩 쓰레드
    pthread_t gFrameThread;
    int gFrameThreadRunning;

    // YUV 데이터
    uint8_t *gData[3];
};
