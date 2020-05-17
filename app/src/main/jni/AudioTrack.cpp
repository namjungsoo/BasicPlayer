#include "AudioTrack.h"
#include "Log.h" 

#include <stdio.h>

#include <libavformat/avformat.h>

jfieldID java_get_field(JNIEnv *env, char *class_name, JavaField field) {
	jclass clazz = env->FindClass(class_name);
	jfieldID jField = env->GetFieldID(clazz, field.name, field.signature);
	env->DeleteLocalRef(clazz);
	return jField;
}

jmethodID java_get_method(JNIEnv *env, jclass cls, JavaMethod method) {
	return env->GetMethodID(cls, method.name, method.signature);
}

jmethodID java_get_static_method(JNIEnv *env, jclass cls, JavaMethod method) {
    return env->GetStaticMethodID(cls, method.name, method.signature);
}

typedef struct {
    jclass player_class;// com.duongame.basicplayer.Player
    jmethodID player_prepare_audio_track_method;// prepareAudioTrack

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
} JavaPlayer;

JavaPlayer *player = NULL;

void initAudioTrack(JNIEnv *env, jobject thiz)
{
    player = (JavaPlayer*)malloc(sizeof(JavaPlayer));
    memset(player, 0, sizeof(*player));

    LOGD("initAudioTrack");

    int ret = env->GetJavaVM(&player->javavm);
    player->env = env;
    player->thiz = env->NewGlobalRef(thiz);

	jclass player_class = env->FindClass(player_class_path_name);
    player->player_class = (jclass)env->NewGlobalRef(player_class);
//	LOGD("initAudioTrack player_class=%d", player_class);

	player->player_prepare_audio_track_method = java_get_static_method(env, player->player_class, player_prepare_audio_track);
//	LOGD("initAudioTrack player_prepare_audio_track_method=%d", player->player_prepare_audio_track_method);

    env->DeleteLocalRef(player_class);
}

void prepareAudioTrack(int audioFormat, int sampleRate, int channels)
{
    JNIEnv *env = player->env;
    jobject thiz = player->thiz;

    LOGD("prepareAudioTrack audioFormat=%d sampleRate=%d channels=%d", audioFormat, sampleRate, channels);

    /** Audio data format: PCM 16 bit per sample. Guaranteed to be supported by devices. */
    const int ENCODING_PCM_16BIT = 2;
    /** Audio data format: PCM 8 bit per sample. Not guaranteed to be supported by devices. */
    const int ENCODING_PCM_8BIT = 3;
    /** Audio data format: single-precision floating-point per sample */
    const int ENCODING_PCM_FLOAT = 4;

    int audioTrackFormat = ENCODING_PCM_16BIT;
    //TODO: 나중에 해결하자 
//     switch(audioFormat) {
//         case AV_SAMPLE_FMT_U8:
//         case AV_SAMPLE_FMT_U8P:
//             audioTrackFormat = ENCODING_PCM_8BIT;
//             break;
//         case AV_SAMPLE_FMT_S16:
//         case AV_SAMPLE_FMT_S16P:
//             audioTrackFormat = ENCODING_PCM_16BIT;
//             break;
//         case AV_SAMPLE_FMT_FLT:
//         case AV_SAMPLE_FMT_FLTP:
// //            audioTrackFormat = ENCODING_PCM_FLOAT;
//             audioTrackFormat = ENCODING_PCM_16BIT;
//             break;
//     }
    LOGD("prepareAudioTrack audioTrackFormat=%d", audioTrackFormat);

    // object AudioTrack
//    jobject audio_track = (*env)->CallObjectMethod(env, thiz, player->player_prepare_audio_track_method, audioTrackFormat, sampleRate, channels);
    jobject audio_track = env->CallStaticObjectMethod(player->player_class, player->player_prepare_audio_track_method, audioTrackFormat, sampleRate, channels);
//    LOGD("prepareAudioTrack audio_track=%d", audio_track);

    player->audio_track = env->NewGlobalRef(audio_track);
    env->DeleteLocalRef(audio_track);

	// class AudioTrack 
	jclass audio_track_class = env->FindClass(android_track_class_path_name);
	player->audio_track_class = (jclass)env->NewGlobalRef(audio_track_class);

    player->audio_track_write_method = java_get_method(env, player->audio_track_class, audio_track_write);
    player->audio_track_pause_method = java_get_method(env, player->audio_track_class, audio_track_pause);
    player->audio_track_play_method = java_get_method(env, player->audio_track_class, audio_track_play);
    player->audio_track_flush_method = java_get_method(env, player->audio_track_class, audio_track_flush);
    player->audio_track_stop_method = java_get_method(env, player->audio_track_class, audio_track_stop);

	// call play
	env->CallVoidMethod(player->audio_track, player->audio_track_play_method);
	LOGD("prepareAudioTrack play");
}

JNIEnv *attachThread() 
{
    JNIEnv *env;
    char title[512];
    sprintf(title, "basicplayer");
    JavaVMAttachArgs thread_spec = { JNI_VERSION_1_4, title, NULL };
    int ret = player->javavm->AttachCurrentThread(&env, &thread_spec);
    return env;
}

void detatchThread()
{
    int ret = player->javavm->DetachCurrentThread();
}

void writeAudioTrack(char* data, int data_size) 
{
    // 쓰레드에서 호출되므로 javavm에서 env를 호출해야함 
    JNIEnv *env = attachThread();
    jobject thiz = player->thiz;

    // 오디오 트랙에 데이터를 쓰면 된다 여기서
    // 어떤 소리가 나던지 일단은 써보자 
    jbyteArray samples_byte_array = env->NewByteArray(data_size);
    env->SetByteArrayRegion(samples_byte_array, 0, data_size, (const jbyte*)data);

	// jbyte *jni_samples = env->GetByteArrayElements(samples_byte_array, 0);
	// memcpy(jni_samples, data, data_size);
	// env->ReleaseByteArrayElements(samples_byte_array, jni_samples, 0);

    if(player->audio_track != NULL) {
	   int ret = env->CallIntMethod(player->audio_track, player->audio_track_write_method, samples_byte_array, 0, data_size);
    }

    detatchThread();
}

void pauseAudioTrack(JNIEnv *env, jobject thiz) 
{
    if(player->audio_track != NULL) {
        env->CallVoidMethod(player->audio_track, player->audio_track_pause_method);
    }
}

void resumeAudioTrack(JNIEnv *env, jobject thiz) 
{
    if(player->audio_track != NULL) {
        env->CallVoidMethod(player->audio_track, player->audio_track_play_method);
    }
}
