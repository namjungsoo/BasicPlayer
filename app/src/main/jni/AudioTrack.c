#include "AudioTrack.h"
#include "Log.h" 
#include <stdio.h>

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
    JNIEnv *env;
    jobject thiz;
    JavaVM *javavm;
} Player;

Player player;

void initAudioTrack(JNIEnv *env, jobject thiz)
{
    LOGD("initAudioTrack");

    int ret = (*env)->GetJavaVM(env, &player.javavm);
    player.env = env;
    player.thiz = (*env)->NewGlobalRef(env, thiz);

	jclass player_class = (*env)->FindClass(env, player_class_path_name);
	LOGD("initAudioTrack player_class=%d", player_class);

	player.player_prepare_audio_track_method = java_get_method(env, player_class, player_prepare_audio_track);
	LOGD("initAudioTrack player_prepare_audio_track_method=%d", player.player_prepare_audio_track_method);

    (*env)->DeleteLocalRef(env, player_class);
}

void prepareAudioTrack(int sampleRate, int channels)
{
    JNIEnv *env = player.env;
    jobject thiz = player.thiz;

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

void writeAudioTrack(char* data, int data_size) 
{
    // 쓰레드에서 호출되므로 javavm에서 env를 호출해야함 
//    LOGD("writeAudioTrack data=%d data_size=%d", data, data_size);

    JNIEnv *env;
    char title[512];
    sprintf(title, "basicplayer");
    JavaVMAttachArgs thread_spec = { JNI_VERSION_1_4, title, NULL };

    int ret = (*player.javavm)->AttachCurrentThread(player.javavm, &env, &thread_spec);

    jobject thiz = player.thiz;

    // 오디오 트랙에 데이터를 쓰면 된다 여기서
    // 어떤 소리가 나던지 일단은 써보자 
//    LOGD("writeAudioTrack samples_byte_array begin");
    jbyteArray samples_byte_array = (*env)->NewByteArray(env, data_size);
//    LOGD("writeAudioTrack samples_byte_array end");

	jbyte *jni_samples = (*env)->GetByteArrayElements(env, samples_byte_array, 0);
	memcpy(jni_samples, data, data_size);
	(*env)->ReleaseByteArrayElements(env, samples_byte_array, jni_samples, 0);
//    LOGD("writeAudioTrack jni_samples");

	ret = (*env)->CallIntMethod(env, player.audio_track,
			player.audio_track_write_method, samples_byte_array, 0, data_size);
//    LOGD("writeAudioTrack ret=%d", ret);

    ret = (*player.javavm)->DetachCurrentThread(player.javavm);

}