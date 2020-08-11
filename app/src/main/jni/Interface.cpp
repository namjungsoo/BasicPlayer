#include <jni.h>
#include "Main.h"

#ifdef __cplusplus
extern "C"
{
#endif
#include <libavformat/avformat.h>

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    // LOGD("Hello");
    return JNI_VERSION_1_6;
}

void Java_com_duongame_basicplayer_Player_initAudioTrack(JNIEnv *env, jobject thiz)
{
	Main::initAudioTrack(env, thiz);
}

jint Java_com_duongame_basicplayer_Player_initBasicPlayer(JNIEnv *env, jobject thiz)
{
	av_register_all();
	return Main::initBasicPlayer();
}

jint Java_com_duongame_basicplayer_Player_openMovieWithAudio(JNIEnv *env, jobject thiz, int id, jstring filePath, int audio, int width, int height)
{
	return Main::openMovieWithAudio(env, thiz, id, filePath, audio, width, height);
}

jint Java_com_duongame_basicplayer_Player_openMovie(JNIEnv *env, jobject thiz, int id, jstring filePath, int width, int height)
{
	return Main::openMovie(env, thiz, id, filePath, width, height);
}

jint Java_com_duongame_basicplayer_Player_renderFrame(JNIEnv *env, jobject thiz, int id, jobject bitmap)
{
	return Main::renderFrame(env, thiz, id, bitmap);
}

jint Java_com_duongame_basicplayer_Player_getMovieWidth(JNIEnv *env, jobject thiz, int id)
{
	return Main::getMovieWidth(id);
}

jint Java_com_duongame_basicplayer_Player_getMovieHeight(JNIEnv *env, jobject thiz, int id)
{
	return Main::getMovieHeight(id);
}

void Java_com_duongame_basicplayer_Player_closeMovie(JNIEnv *env, jobject thiz, int id)
{
	Main::closeMovie(id);
}

void Java_com_duongame_basicplayer_Player_pauseMovie(JNIEnv *env, jobject thiz)
{
	Main::pauseMovie(env, thiz);
}

void Java_com_duongame_basicplayer_Player_resumeMovie(JNIEnv *env, jobject thiz)
{
	Main::resumeMovie(env, thiz);
}

int Java_com_duongame_basicplayer_Player_seekMovie(JNIEnv *env, jobject thiz, int id, jlong positionUs)
{
	return Main::seekMovie(id, positionUs);
}

jdouble Java_com_duongame_basicplayer_Player_getFps(JNIEnv *env, jobject thiz, int id)
{
	return Main::getFps(id);
}

jlong Java_com_duongame_basicplayer_Player_getMovieDurationUs(JNIEnv *env, jobject thiz, int id)
{
	return Main::getMovieDurationUs(id);
}

jlong Java_com_duongame_basicplayer_Player_getCurrentPositionUs(JNIEnv *env, jobject thiz, int id)
{
	return Main::getCurrentPositionUs(id);
}

// GLPlayerView.renderFrameYUVTexId
jint Java_com_duongame_basicplayer_Player_renderFrameYUVTexId(JNIEnv *env, jobject thiz, int id, int width, int height, int texIdY, int texIdU, int texIdV) 
{
	return Main::renderFrameYUVTexId(id, width, height, texIdY, texIdU, texIdV);
}

#ifdef __cplusplus
}
#endif
