package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.AttributeSet;
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
    private Timer mTimer;
    private Context mContext;
    private long mInterval;

    public MoviePlayView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        closeMovie();
        init(context);
    }
    public MoviePlayView(Context context) {
        this(context, null);

    }

    public void init(Context context) {
        Log.d(TAG,"init");

        if (initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        initAudioTrack();
    }

    private void initRenderTimer() {
        double fps = getFps();
        Log.d(TAG, "fps="+fps);

        mInterval = (long) (1000./fps);
        if(mInterval == 0)
            mInterval = 1;
        Log.d(TAG, "mInterval="+mInterval);
    }

    public void openFile(String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = openMovie(filename);
        if (openResult < 0) {
            Toast.makeText(mContext, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
        } else {
            mMovieWidth = getMovieWidth();
            mMovieHeight = getMovieHeight();
            mBitmap = Bitmap.createBitmap(mMovieWidth, mMovieHeight, Bitmap.Config.ARGB_8888);
            Log.d(TAG,"init createBitmap");

            initRenderTimer();
            resume();
        }
    }

    public void pause() {
        mTimer.cancel();
    }

    public void resume() {
        // 렌더링 타이머 24fps
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.d(TAG, "Timer");

                MoviePlayView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        invalidate();
                    }
                });
            }
        };

        mTimer = new Timer();
        mTimer.schedule(task, 0, mInterval);
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
        Log.d(TAG,"onDraw");
        if(mBitmap != null) {
            int ret = renderFrame(mBitmap);

            // 렌더링 종료
            if(ret > 0) {
                pause();
            }
            else {
                // 항상 풀스크린으로 채우는 것은 안된다
                // 종횡비를 맞춰서 채워야 한다
                canvas.drawBitmap(mBitmap, new Rect(0,0,mBitmap.getWidth(), mBitmap.getHeight()), new Rect(0,0,getWidth(),getHeight()), null);
            }


            // 최초 그려지고 나서 항상 그려지게 한다.
//            invalidate();
//            Log.d(TAG,"onDraw invalidate");
        }
        Log.d(TAG,"onDraw END");
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