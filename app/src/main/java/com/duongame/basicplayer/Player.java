package com.duongame.basicplayer;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by namjungsoo on 16. 6. 11..
 */
public class Player {
    private final static String TAG = "Player";
    private int id = -1;

    public void init() {
        id = initBasicPlayer();
        Log.d(TAG, "init id=" + id);
    }

    //ndk에서 불러준다.
    private static AudioTrack prepareAudioTrack(int audioFormat, int sampleRateInHz,
                                                int numberOfChannels) {

        while (true) {
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

    public int openMovie(String path, int width, int height) {
        return openMovie(id, path, width, height);
    }

    public int openMovieWithAudio(String path, int audio, int width, int height) {
        return openMovieWithAudio(id, path, audio, width, height);
    }

    public int renderFrame(Bitmap bitmap) {
        return renderFrame(id, bitmap);
    }

    public int renderFrameYUV(Bitmap bitmapY, Bitmap bitmapU, Bitmap bitmapV) {
        return renderFrameYUV(id, bitmapY, bitmapU, bitmapV);
    }

    public int getMovieWidth() {
        return getMovieWidth(id);
    }

    public int getMovieHeight() {
        return getMovieHeight(id);
    }

    public void closeMovie() {
        closeMovie(id);
    }

    public int seekMovie(long positionUs) {
        return seekMovie(id, positionUs);
    }

    public long getMovieDurationUs() {
        return getMovieDurationUs(id);
    }

    public double getFps() {
        return getFps(id);
    }

    public long getCurrentPositionUs() {
        return getCurrentPositionUs(id);
    }

    // 오디오 관련(static)
    public static native void initAudioTrack();

    public static native void pauseMovie();

    public static native void resumeMovie();

    private native int initBasicPlayer();

    private native int openMovie(int id, String filePath, int width, int height);

    private native int openMovieWithAudio(int id, String filePath, int audio, int width, int height);

    private native int renderFrame(int id, Bitmap bitmap);

    private native int renderFrameYUV(int id, Bitmap bitmapY, Bitmap bitmapU, Bitmap bitmapV);

    private native int getMovieWidth(int id);

    private native int getMovieHeight(int id);

    private native void closeMovie(int id);

    private native int seekMovie(int id, long positionUs);

    private native long getMovieDurationUs(int id);

    private native double getFps(int id);

    private native long getCurrentPositionUs(int id);
}