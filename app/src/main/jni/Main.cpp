#include "Main.h"

#include "Log.h"
#include "BasicPlayer.h"
#include "AudioTrack.h"
#include "AudioFormatMap.h"
#include "AudioQ.h"
#include "PlayerMap.h"

#include <signal.h>
#include <sys/cdefs.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <stdint.h>
#include <linux/termios.h>

#include <GLES/gl.h>
#include <android/bitmap.h>

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

namespace Main {
void initAudioTrack(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN initAudioTrack");
	::initAudioTrack(env, thiz);
	LOGD("END initAudioTrack");
}

void pauseMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN pauseMovie");
	::pauseMovie(env, thiz);
//	LOGD("END pauseMovie");

}

void resumeMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN resumeMovie");
	::resumeMovie(env, thiz);
//	LOGD("END resumeMovie");
}

int initBasicPlayer()
{
    LOGD("BEGIN initBasicPlayer");
	// ARM 패밀리 이고, NEON 피쳐가 있을경우 av_register_all을 해준다.
	//Initializes libavformat and registers all the muxers, demuxers and protocols.
	initAudioFormatMap();
	AudioQ_init();

	Movie *gMovie;
	gMovie = (Movie*)malloc(sizeof(Movie));
	initMovie(gMovie);
	int id = MovieMap_insert(gMovie);

	LOGD("END initBasicPlayer id=%d", id);
	return id;
}

int openMovieWithAudio(JNIEnv *env, jobject thiz, int id, jstring filePath, int audio, int width, int height)
{
	LOGD("BEGIN openMovieWithAudio");
	const char *str;
	int result;

	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return -1;

	// 문자열 사용하고 나서 삭제
	str = env->GetStringUTFChars(filePath, NULL);
	result = openMovieWithAudio(gMovie, str, audio, width, height);
	env->ReleaseStringUTFChars(filePath, str);

	LOGD("END openMovieWithAudio");
	return result;
}

int openMovie(JNIEnv *env, jobject thiz, int id, jstring filePath, int width, int height)
{
	LOGD("BEGIN openMovie id=%d filePath=%s", id, filePath);

	Movie *gMovie = MovieMap_get(id);
	LOGD("openMovie id=%d gMovie=%d", id, gMovie);
	if(gMovie == NULL)
		return -1;

	const char *str;
	int result;

	// 문자열 사용하고 나서 삭제
	str = env->GetStringUTFChars(filePath, NULL);
	LOGD("openMovie str=%s", str);
	result = openMovie(gMovie, str, width, height);
	env->ReleaseStringUTFChars(filePath, str);

	LOGD("END openMovie");
	return result;
}

int renderFrame(JNIEnv *env, jobject thiz, int id, jobject bitmap)
{
//	LOGD("BEGIN renderFrame");

    void *pixels;
	int result;

	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return -1;

	// LOGD("renderFrame BEGIN");

	// 영상이 종료된 상태임
	if(decodeFrame(gMovie) < 0) {
		// LOGD("closeMovie");

		// 영상이 종료되도 close하지 말자
		//closeMovie();

		// LOGD("renderFrame END");
		return 1;// 종료 상태
	}
	else {
		//TODO: 렌더링 타겟을 Bitmap이 아닌 다른것으로 설정
		// LOGD("renderFrame lockPixels");
		if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
			return result;

		copyPixels(gMovie, (uint8_t*)pixels);

		// LOGD("renderFrame unlockPixels");
		AndroidBitmap_unlockPixels(env, bitmap);
	}

//	LOGD("END renderFrame");
	return 0;
}

int renderFrameYUVTexId(JNIEnv *env, jobject thiz, int id, int width, int height, int texIdY, int texIdU, int texIdV)
{
	int result;

	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return -1;

#if THREAD_RENDER == 1
		// decodeFrame은 항상 thread에서 수행되고 있다.
		Movie *movie = gMovie;

		glBindTexture(GL_TEXTURE_2D, texIdY);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gData[0]);
		glBindTexture(GL_TEXTURE_2D, texIdU);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gData[1]);
		glBindTexture(GL_TEXTURE_2D, texIdV);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gData[2]);
#else
		// 영상이 종료된 상태임
		if(decodeFrame(gMovie) < 0) {
			return 1;// 종료 상태
		}
		else {
			Movie *movie = gMovie;

			glBindTexture(GL_TEXTURE_2D, texIdY);
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[0]);
			glBindTexture(GL_TEXTURE_2D, texIdU);
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[1]);
			glBindTexture(GL_TEXTURE_2D, texIdV);
			glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[2]);
		}
#endif

	return 0;
}

int getMovieWidth(int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	return getWidth(gMovie);
}

int getMovieHeight(int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	return getHeight(gMovie);
}

void closeMovie(int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return;

	LOGD("BEGIN closeMovie");

	closeMovie(gMovie);
	LOGD("closeMovie end");

	free(gMovie);
	LOGD("closeMovie free end");
	MovieMap_remove(id);
	LOGD("closeMovie MovieMap_remove end");

	LOGD("END closeMovie");
}

int seekMovie(int id, jlong positionUs)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

//	LOGD("BEGIN seekMovie");
	int ret = seekMovie(gMovie, positionUs);
//	LOGD("END seekMovie");
	return ret;

}

double getFps(int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0.0;

	jdouble fps = getFps(gMovie);
	LOGD("interface fps=%f", fps);
	return fps;
}

long getMovieDurationUs(int id)
{
	LOGD("BEGIN getMovieDurationUs");
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	jlong ret = getDuration(gMovie);
	LOGD("END getMovieDurationUs");
	return ret;
}

long getCurrentPositionUs(int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

//	LOGD("BEGIN getCurrentPositionUs");
	jlong ret = getPosition(gMovie);
//	LOGD("END getCurrentPositionUs");
	return ret;
}

}//Main