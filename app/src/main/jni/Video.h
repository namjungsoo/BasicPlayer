#include <unistd.h> //pthread_t

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

class Video {
public:
    Video(int idx);
    virtual ~Video();

    int openStream(AVFormatContext *formatCtx, int targetWidth, int targetHeight);
    void close();

    int getWidth();
    int getHeight();
    int getFps();

    int seek(int64_t positionUs);
    void startThread() {}

private:
    // 비디오 관련 
    AVCodecContext *gVideoCodecCtx;
    AVCodec *gVideoCodec;
    AVFrame *gFrame;
    AVFrame *gFrameRGB;
    int gVideoStreamIdx;
    int gPictureSize;
    uint8_t *gVideoBuffer;
    uint8_t *gData[3];// YUV 데이터
    AVPixelFormat gPixelFormat;
    double gFps;
    pthread_t gFrameThread;
    int gFrameThreadRunning;
    int gTargetWidth;
    int gTargetHeight;

    SwsContext *gImgConvertCtx;
};
