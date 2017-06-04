package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by namjungsoo on 16. 6. 11..
 */
public class Player {
    private final static String TAG = "Player";

    public static void init(Context context) {
        Log.d(TAG, "init");

        if (Player.initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        Player.initAudioTrack();
    }

    //ndk에서 불러준다.
    private static AudioTrack prepareAudioTrack(int audioFormat, int sampleRateInHz,
                                         int numberOfChannels) {

        while(true) {
            int channelConfig;
            if (numberOfChannels == 1) {
                channelConfig = AudioFormat.CHANNEL_OUT_MONO;
            } else if (numberOfChannels == 2) {
                channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            } else if (numberOfChannels == 3) {
                channelConfig = AudioFormat.CHANNEL_OUT_FRONT_CENTER
                        | AudioFormat.CHANNEL_OUT_FRONT_RIGHT
                        | AudioFormat.CHANNEL_OUT_FRONT_LEFT;
            } else if (numberOfChannels == 4) {
                channelConfig = AudioFormat.CHANNEL_OUT_QUAD;
            } else if (numberOfChannels == 5) {
                channelConfig = AudioFormat.CHANNEL_OUT_QUAD
                        | AudioFormat.CHANNEL_OUT_LOW_FREQUENCY;
            } else if (numberOfChannels == 6) {
                channelConfig = AudioFormat.CHANNEL_OUT_5POINT1;
            } else if (numberOfChannels == 8) {
                channelConfig = AudioFormat.CHANNEL_OUT_7POINT1;
            } else {
                channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
            }
            try {
//                int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
//                        channelConfig, AudioFormat.ENCODING_PCM_16BIT);
//                AudioTrack audioTrack = new AudioTrack(
//                        AudioManager.STREAM_MUSIC, sampleRateInHz,
//                        channelConfig, AudioFormat.ENCODING_PCM_16BIT,
//                        minBufferSize, AudioTrack.MODE_STREAM);

                // 동적으로 audioFormat을 넣어준다
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfig, audioFormat, minBufferSize, AudioTrack.MODE_STREAM);
                return audioTrack;
            } catch (IllegalArgumentException e) {
                if (numberOfChannels > 2) {
                    numberOfChannels = 2;
                } else if (numberOfChannels > 1) {
                    numberOfChannels = 1;
                } else {
                    throw e;
                }
            }
        }
    }

    static {
        System.loadLibrary("basicplayer");
    }

    public static native void initAudioTrack();
    public static native int initBasicPlayer();

    public static native int openMovie(String filePath);
    public static native int openMovieWithAudio(String filePath, int audio);
    public static native int renderFrame(Bitmap bitmap);

    public static native int getMovieWidth();
    public static native int getMovieHeight();
    public static native void closeMovie();

    public static native void pauseMovie();
    public static native void resumeMovie();
    public static native int seekMovie(long positionUs);

    public static native long getMovieDurationUs();
    public static native double getFps();

    public static native long getCurrentPositionUs();
}