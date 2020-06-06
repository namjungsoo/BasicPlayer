#include "Player.h"
#include "Audio.h"
#include "Log.h"

Player::Player() : formatCtx(NULL), currentTimeUs(0L)
{
    // video = new Video;
    // audio = new Audio;
}

int Player::open(const char *path, int isAudio, int targetWidth, int targetHeight)
{
   	int i;
	char errbuf[128];

	// 최초에 컨텍스트가 null이 맞는지 확인한다.
	if (formatCtx != NULL)
		return -1;

	// 파일을 연다.
	int err = avformat_open_input(&formatCtx, path, NULL, NULL);
	if (err < 0) {
		av_strerror(err, errbuf, sizeof(errbuf));
		LOGD("%s", errbuf);
		return -2;
	}

	// 스트짐 정보를 포맷 컨텍스트에 리턴한다.
	if (avformat_find_stream_info(formatCtx, NULL) < 0)
		return -3;

	for (i = 0; i < formatCtx->nb_streams; i++)
	{
		if (formatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
		{
            video = new Video(i);
			// movie->gVideoStreamIdx = i;
			// LOGD("gVideoStreamIdx=%d", movie->gVideoStreamIdx);
		}

		if (formatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO)
		{
            if(isAudio) {
                audio = new Audio(i);
            }
			// movie->gAudioStreamIdx = i;
			// LOGD("gAudioStreamIdx=%d", movie->gAudioStreamIdx);
		}
	}

    int ret = video->openStream(formatCtx, targetWidth, targetWidth);
    if(ret < 0)
        return ret;

    // 오디오가 있을때 실제로 플레이를 위한 것
    // 없으면 썸네일 추출, 정보획득 등을 위한 것
    if(isAudio) {
        audio->openStream(formatCtx);
        audio->prepare();
        audio->startThread();

		video->startThread();
    }

    return 0;
}

void Player::close() 
{
    if(video)
        video->close();
    if(audio)
        audio->close();
}

int Player::decodeFrame() 
{

}

void Player::copyFrame(uint8_t *pixels)
{
    
}