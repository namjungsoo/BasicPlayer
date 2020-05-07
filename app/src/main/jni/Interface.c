#include <android/bitmap.h>
#include <jni.h>
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

#define PLAYER_RESULT_OK (0)
#define PLAYER_RESULT_ERROR (-1)
#define PLAYER_RESULT_END (1)

int tcgetattr(int fd, struct termios *s)    
{
	return ioctl(fd, TCGETS, s);
}
    
int tcsetattr(int fd, int __opt, const struct termios *s)
{
	return ioctl(fd, __opt, (void *)s);
}

char *stpcpy (char *dst, const char *src)
{
	const size_t len = strlen (src);
	return (char *) memcpy (dst, src, len + 1) + len;
}


// typedef void (*sighandler_t)(int);

// sighandler_t signal(int signum, sighandler_t handler) {
// 	return bsd_signal(signum, handler);
// }

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGD("Hello");
    return JNI_VERSION_1_6;
}

void Java_com_duongame_basicplayer_Player_initAudioTrack(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN initAudioTrack");
	initAudioTrack(env, thiz);
	LOGD("END initAudioTrack");
}

jint Java_com_duongame_basicplayer_Player_initBasicPlayer(JNIEnv *env, jobject thiz)
{
	LOGD("BEGIN initBasicPlayer");
	// ARM 패밀리 이고, NEON 피쳐가 있을경우 av_register_all을 해준다. 
	//Initializes libavformat and registers all the muxers, demuxers and protocols. 
	av_register_all();
	initAudioFormatMap();
	AudioQ_init();

	Movie *gMovie;
	gMovie = (Movie*)malloc(sizeof(Movie));
	initMovie(gMovie);
	int id = MovieMap_insert(gMovie);

	LOGD("END initBasicPlayer id=%d", id);
	return id;
}

jint Java_com_duongame_basicplayer_Player_openMovieWithAudio(JNIEnv *env, jobject thiz, int id, jstring filePath, int audio, int width, int height)
{
	LOGD("BEGIN openMovieWithAudio");
	const jbyte *str;
	int result;

	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return -1;

	// 문자열 사용하고 나서 삭제 
	str = (*env)->GetStringUTFChars(env, filePath, NULL);
	result = openMovieWithAudio(gMovie, str, audio, width, height);
	(*env)->ReleaseStringUTFChars(env, filePath, str);

	LOGD("END openMovieWithAudio");
	return result;
}

jint Java_com_duongame_basicplayer_Player_openMovie(JNIEnv *env, jobject thiz, int id, jstring filePath, int width, int height)
{
	LOGD("BEGIN openMovie id=%d filePath=%s", id, filePath);

	Movie *gMovie = MovieMap_get(id);
	LOGD("openMovie id=%d gMovie=%d", id, gMovie);
	if(gMovie == NULL)
		return -1;

	const jbyte *str;
	int result;

	// 문자열 사용하고 나서 삭제 
	str = (*env)->GetStringUTFChars(env, filePath, NULL);
	LOGD("openMovie str=%s", str);
	result = openMovie(gMovie, str, width, height);
	(*env)->ReleaseStringUTFChars(env, filePath, str);

	LOGD("END openMovie");
	return result;
}

// GLPlayerView.renderFrameYUVTexId 
jint Java_com_duongame_basicplayer_Player_renderFrameYUVTexId(JNIEnv *env, jobject thiz, int id, int width, int height, int texIdY, int texIdU, int texIdV) {
	int result;

	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return -1;

	// 영상이 종료된 상태임 
	// if(decodeFrame(gMovie) < 0) {
	// 	return 1;// 종료 상태 
	// }
	// else {
	// 	Movie *movie = gMovie;

	// 	glBindTexture(GL_TEXTURE_2D, texIdY);
	// 	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[0]);
	// 	glBindTexture(GL_TEXTURE_2D, texIdU);
	// 	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[1]);
	// 	glBindTexture(GL_TEXTURE_2D, texIdV);
	// 	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[2]);
	// }

	// decodeFrame은 항상 thread에서 수행되고 있다.
	Movie *movie = gMovie;

	glBindTexture(GL_TEXTURE_2D, texIdY);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[0]);
	glBindTexture(GL_TEXTURE_2D, texIdU);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[1]);
	glBindTexture(GL_TEXTURE_2D, texIdV);
	glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width/2, height/2, GL_LUMINANCE, GL_UNSIGNED_BYTE, movie->gFrame->data[2]);

	return 0;
}

// jint Java_com_duongame_basicplayer_Player_renderFrameYUVArray(JNIEnv *env, jobject thiz, int id, jbyteArray arrayY, jbyteArray arrayU, jbyteArray arrayV) {
// //    void *pixelsY, *pixelsU, *pixelsV;
// 	int result;

// 	Movie *gMovie = MovieMap_get(id);
// 	if(gMovie == NULL)
// 		return -1;

// 	// 영상이 종료된 상태임 
// 	if(decodeFrame(gMovie) < 0) {
// 		return 1;// 종료 상태 
// 	}
// 	else {
// 		Movie *movie = gMovie;
// 		(*env)->SetByteArrayRegion(env, arrayY, 0, movie->gVideoPictureSize*2/3, movie->gFrame->data[0]);
// 		(*env)->SetByteArrayRegion(env, arrayU, 0, movie->gVideoPictureSize/6,   movie->gFrame->data[1]);
// 		(*env)->SetByteArrayRegion(env, arrayV, 0, movie->gVideoPictureSize/6,   movie->gFrame->data[2]);
		
// 		// pixelsY = (*env)->GetByteArrayElements(env, arrayY, 0);
// 		// pixelsU = (*env)->GetByteArrayElements(env, arrayU, 0);
// 		// pixelsV = (*env)->GetByteArrayElements(env, arrayV, 0);
// 		// copyPixelsYUV(gMovie, (uint8_t*)pixelsY, (uint8_t*)pixelsU, (uint8_t*)pixelsV);

// 		// (*env)->ReleaseByteArrayElements(env, arrayY, pixelsY, 0);
// 		// (*env)->ReleaseByteArrayElements(env, arrayU, pixelsU, 0);
// 		// (*env)->ReleaseByteArrayElements(env, arrayV, pixelsV, 0);
// 	}

// 	return 0;
// }

// jint Java_com_duongame_basicplayer_Player_renderFrameYUV(JNIEnv *env, jobject thiz, int id, jobject bitmapY, jobject bitmapU, jobject bitmapV) {
//     void *pixelsY, *pixelsU, *pixelsV;
// 	int result;

// 	Movie *gMovie = MovieMap_get(id);
// 	if(gMovie == NULL)
// 		return -1;

// 	// 영상이 종료된 상태임 
// 	if(decodeFrame(gMovie) < 0) {
// 		return 1;// 종료 상태 
// 	}
// 	else {
// 		if ((result = AndroidBitmap_lockPixels(env, bitmapY, &pixelsY)) < 0)
// 			return result;
// 		if ((result = AndroidBitmap_lockPixels(env, bitmapU, &pixelsU)) < 0)
// 			return result;
// 		if ((result = AndroidBitmap_lockPixels(env, bitmapV, &pixelsV)) < 0)
// 			return result;

// 		copyPixelsYUV(gMovie, (uint8_t*)pixelsY, (uint8_t*)pixelsU, (uint8_t*)pixelsV);

// 		AndroidBitmap_unlockPixels(env, bitmapY);
// 		AndroidBitmap_unlockPixels(env, bitmapU);
// 		AndroidBitmap_unlockPixels(env, bitmapV);
// 	}

// 	return 0;
// }

// jint Java_com_duongame_basicplayer_Player_renderFrame(JNIEnv *env, jobject thiz, int id, jobject bitmap)
// {
//     void *pixels;
// 	int result;

// 	Movie *gMovie = MovieMap_get(id);
// 	if(gMovie == NULL)
// 		return -1;

// 	// 영상이 종료된 상태임 
// 	if(decodeFrame(gMovie) < 0) {
// 		return 1;// 종료 상태 
// 	}
// 	else {
// 		if ((result = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0)
// 			return result;

// 		copyPixels(gMovie, (uint8_t*)pixels);

// 		AndroidBitmap_unlockPixels(env, bitmap);
// 	}
// 	return 0;
// }

jint Java_com_duongame_basicplayer_Player_getMovieWidth(JNIEnv *env, jobject thiz, int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	return getWidth(gMovie);
}

jint Java_com_duongame_basicplayer_Player_getMovieHeight(JNIEnv *env, jobject thiz, int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	return getHeight(gMovie);
}

void Java_com_duongame_basicplayer_Player_closeMovie(JNIEnv *env, jobject thiz, int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return;

	LOGD("BEGIN closeMovie");
	
	closeMovie(gMovie);
	free(gMovie);
	MovieMap_remove(id);
	
	LOGD("END closeMovie");
}

void Java_com_duongame_basicplayer_Player_pauseMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN pauseMovie");
	pauseMovie(env, thiz);
//	LOGD("END pauseMovie");
}

void Java_com_duongame_basicplayer_Player_resumeMovie(JNIEnv *env, jobject thiz)
{
//	LOGD("BEGIN resumeMovie");
	resumeMovie(env, thiz);
//	LOGD("END resumeMovie");
}

int Java_com_duongame_basicplayer_Player_seekMovie(JNIEnv *env, jobject thiz, int id, jlong positionUs)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

//	LOGD("BEGIN seekMovie");
	int ret = seekMovie(gMovie, positionUs);
//	LOGD("END seekMovie");
	return ret;
}

jdouble Java_com_duongame_basicplayer_Player_getFps(JNIEnv *env, jobject thiz, int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0.0;

	jdouble fps = getFps(gMovie);
	LOGD("interface fps=%f", fps);
	return fps;
}

jlong Java_com_duongame_basicplayer_Player_getMovieDurationUs(JNIEnv *env, jobject thiz, int id)
{
	LOGD("BEGIN getMovieDurationUs");
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	jlong ret = getDuration(gMovie);
	LOGD("END getMovieDurationUs");
	return ret;
}

jlong Java_com_duongame_basicplayer_Player_getCurrentPositionUs(JNIEnv *env, jobject thiz, int id)
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return 0;

	jlong ret = getPosition(gMovie);
	return ret;
}

void Java_com_duongame_basicplayer_Player_setTexIdYUV(JNIEnv *env, jobject thiz, int id, int texIdY, int texIdU, int texIdV) 
{
	Movie *gMovie = MovieMap_get(id);
	if(gMovie == NULL)
		return;

	gMovie->gTexId[0] = texIdY;
	gMovie->gTexId[1] = texIdU;
	gMovie->gTexId[2] = texIdV;
}
