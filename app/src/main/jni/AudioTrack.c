#include "AudioTrack.h"
#include "Log.h" 

jfieldID java_get_field(JNIEnv *env, char * class_name, JavaField field) {
	jclass clazz = (*env)->FindClass(env, class_name);
	jfieldID jField = (*env)->GetFieldID(env, clazz, field.name, field.signature);
	(*env)->DeleteLocalRef(env, clazz);
	return jField;
}

jmethodID java_get_method(JNIEnv *env, jclass class, JavaMethod method) {
	return (*env)->GetMethodID(env, class, method.name, method.signature);
}

typedef struct {
    jclass player_class;//MoviePlayView
    jmethodID player_prepare_audio_track_method;//prepareAudioTrack

    // AudioTrack
    jclass audio_track_class;
    jmethodID audio_track_write_method;
    jmethodID audio_track_play_method;
    jmethodID audio_track_pause_method;
    jmethodID audio_track_flush_method;
    jmethodID audio_track_stop_method;
    jmethodID audio_track_get_channel_count_method;
    jmethodID audio_track_get_sample_rate_method;

    jobject audio_track; 
} Player;

Player player;

void initAudioTrack(JNIEnv *env, jobject thiz)
{
    LOGD("initAudioTrack");

	jclass player_class = (*env)->FindClass(env, player_class_path_name);
	LOGD("initAudioTrack player_class=%d", player_class);

	player.player_prepare_audio_track_method = java_get_method(env, player_class, player_prepare_audio_track);
	LOGD("initAudioTrack player_prepare_audio_track_method=%d", player.player_prepare_audio_track_method);

    (*env)->DeleteLocalRef(env, player_class);
}

void prepareAudioTrack(JNIEnv *env, jobject thiz, int sampleRate, int channels)
{
    LOGD("prepareAudioTrack sampleRate=%d channels=%d", sampleRate, channels);

    // object AudioTrack
    jobject audio_track = (*env)->CallObjectMethod(env, thiz, player.player_prepare_audio_track_method, sampleRate, channels);
    LOGD("prepareAudioTrack audio_track=%d", audio_track);

    player.audio_track = (*env)->NewGlobalRef(env, audio_track);
    (*env)->DeleteLocalRef(env, audio_track);


	// class AudioTrack 
	jclass audio_track_class = (*env)->FindClass(env, android_track_class_path_name);
	player.audio_track_class = (*env)->NewGlobalRef(env, audio_track_class);

    player.audio_track_write_method = java_get_method(env, player.audio_track_class, audio_track_write);
    player.audio_track_pause_method = java_get_method(env, player.audio_track_class, audio_track_pause);
    player.audio_track_play_method = java_get_method(env, player.audio_track_class, audio_track_play);
    player.audio_track_flush_method = java_get_method(env, player.audio_track_class, audio_track_flush);
    player.audio_track_stop_method = java_get_method(env, player.audio_track_class, audio_track_stop);

	// call play
	(*env)->CallVoidMethod(env, player.audio_track, player.audio_track_play_method);
	LOGD("prepareAudioTrack play");
}