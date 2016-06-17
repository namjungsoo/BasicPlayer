#ifndef __AUDIOTRACK_H__
#define __AUDIOTRACK_H__

#include <jni.h>

typedef struct {
    const char* name;
    const char* signature;
} JavaMethod;

typedef struct {
    char* name;
    char* signature;
} JavaField;

jfieldID java_get_field(JNIEnv *env, char * class_name, JavaField field);
jmethodID java_get_method(JNIEnv *env, jclass class, JavaMethod method);

// FFmpegPlayer
static char *player_class_path_name = "com/duongame/basicplayer/MoviePlayView";
//static JavaField player_m_native_player = {"mNativePlayer", "I"};
//static JavaMethod player_on_update_time = {"onUpdateTime","(JJZ)V"};
static JavaMethod player_prepare_audio_track = {"prepareAudioTrack", "(II)Landroid/media/AudioTrack;"};
//static JavaMethod player_prepare_frame = {"prepareFrame", "(II)Landroid/graphics/Bitmap;"};
//static JavaMethod player_set_stream_info = {"setStreamsInfo", "([Lcom/appunite/ffmpeg/FFmpegStreamInfo;)V"};

// AudioTrack
static char *android_track_class_path_name = "android/media/AudioTrack";
static JavaMethod audio_track_write = {"write", "([BII)I"};
static JavaMethod audio_track_pause = {"pause", "()V"};
static JavaMethod audio_track_play = {"play", "()V"};
static JavaMethod audio_track_flush = {"flush", "()V"};
static JavaMethod audio_track_stop = {"stop", "()V"};
static JavaMethod audio_track_get_channel_count = {"getChannelCount", "()I"};
static JavaMethod audio_track_get_sample_rate = {"getSampleRate", "()I"};

void initAudioTrack(JNIEnv *env, jobject thiz);
void prepareAudioTrack(int sampleRate, int channels);
void writeAudioTrack(char* data, int data_size);

void pauseAudioTrack(JNIEnv *env, jobject thiz);
void resumeAudioTrack(JNIEnv *env, jobject thiz);

#endif __AUDIOTRACK_H__