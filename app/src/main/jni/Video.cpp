#include <unistd.h> //pthread_t
#include "Video.h"

//ffmpeg
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

Video::Video(int idx) 
{

}


Video::~Video() 
{

}

int Video::openStream(AVFormatContext *formatCtx, int targetWidth, int targetHeight)
{

}

void Video::close()
{
    
}