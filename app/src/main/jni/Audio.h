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

class Audio {
public:
    Audio(int idx);
    virtual ~Audio();

    int openStream(AVFormatContext *formatCtx);
    void close();

    // audio track을 시작
    void prepare();
    void startThread();

//    SharedQueue<AVPacket> 
private:
    // 오디오 관련 
    AVCodecContext *gAudioCodecCtx;
    AVCodec *gAudioCodec;
    AVFrame *gFrameAudio;
    int gAudioStreamIdx;
    pthread_t gAudioThread;
    int gAudioThreadRunning;
};