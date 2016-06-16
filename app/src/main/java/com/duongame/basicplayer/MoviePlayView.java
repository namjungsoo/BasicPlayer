package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by namjungsoo on 16. 6. 11..
 */
public class MoviePlayView extends View {
    private final static String TAG="MoviePlayView";

    private Bitmap mBitmap;
    private int mMovieWidth;
    private int mMovieHeight;

    public MoviePlayView(Context context, String filename) {
        super(context);

        closeMovie();
        init(context, filename);
    }

    private void initRenderTimer() {
        double fps = getFps();
        Log.d(TAG, "fps="+fps);
        long interval = (long) (1000./fps);
        Log.d(TAG, "interval="+interval);

        // 렌더링 타이머 24fps
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                MoviePlayView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        };

        final Timer timer;
        timer = new Timer();
        timer.schedule(task, 0, interval);
    }

    public void init(Context context, String filename) {
        Log.d(TAG,"init");

        if (initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        initAudioTrack();

//        final String fname = "/mnt/sdcard/ar18-1.avi";
//        final String fname = "/mnt/sdcard/Download/dd 022.avi";
//        String fname = "/mnt/sdcard/mediaweb.mp4";

        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = openMovie(filename);
        if (openResult < 0) {
            Toast.makeText(context, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();

            ((Activity) context).finish();
        } else {
            mMovieWidth = getMovieWidth();
            mMovieHeight = getMovieHeight();
            mBitmap = Bitmap.createBitmap(mMovieWidth, mMovieHeight, Bitmap.Config.ARGB_8888);
            Log.d(TAG,"init createBitmap");

            initRenderTimer();
        }

    }

    //ndk에서 불러준다.
    private AudioTrack prepareAudioTrack(int sampleRateInHz,
                                         int numberOfChannels) {

        for (;;) {
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
                int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz,
                        channelConfig, AudioFormat.ENCODING_PCM_16BIT);
                AudioTrack audioTrack = new AudioTrack(
                        AudioManager.STREAM_MUSIC, sampleRateInHz,
                        channelConfig, AudioFormat.ENCODING_PCM_16BIT,
                        minBufferSize, AudioTrack.MODE_STREAM);
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

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.d(TAG,"onDraw");
        if(mBitmap != null) {
            renderFrame(mBitmap);

            // 항상 풀스크린으로 채우는 것은 안된다
            // 종횡비를 맞춰서 채워야 한다
            canvas.drawBitmap(mBitmap, new Rect(0,0,mBitmap.getWidth(), mBitmap.getHeight()), new Rect(0,0,getWidth(),getHeight()), null);

            // 최초 그려지고 나서 항상 그려지게 한다.
//            invalidate();
//            Log.d(TAG,"onDraw invalidate");
        }
    }

    static {
        System.loadLibrary("basicplayer");
    }

    public native void initAudioTrack();

    public native int initBasicPlayer();

    public native int openMovie(String filePath);

    public native int renderFrame(Bitmap bitmap);

    public native int getMovieWidth();

    public native int getMovieHeight();

    public native void closeMovie();

    public native double getFps();
}