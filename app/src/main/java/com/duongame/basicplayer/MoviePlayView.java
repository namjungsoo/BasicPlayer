package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.activity.PlayerActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by namjungsoo on 16. 6. 11..
 */
public class MoviePlayView extends View {
    private final static String TAG = "MoviePlayView";

    private Bitmap mBitmap;
    private int mMovieWidth;
    private int mMovieHeight;
    private Timer mTimer;
    private Context mContext;
    private long mInterval;
    private boolean mPlaying;

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
        Log.d(TAG, "init");

        if (initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        initAudioTrack();
    }

    private void initRenderTimer() {
        double fps = getFps();
        Log.d(TAG, "fps=" + fps);

        mInterval = (long) (1000. / fps);
        if (mInterval == 0)
            mInterval = 1;
        Log.d(TAG, "mInterval=" + mInterval);
    }

    public boolean openFile(String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = openMovie(filename);
        if (openResult < 0) {
            Toast.makeText(mContext, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
            return false;
        } else {
            mMovieWidth = getMovieWidth();
            mMovieHeight = getMovieHeight();
            mBitmap = Bitmap.createBitmap(mMovieWidth, mMovieHeight, Bitmap.Config.ARGB_8888);
            Log.d(TAG, "init createBitmap");

            initRenderTimer();
            resume();
            return true;
        }
    }

    public boolean getPlaying() {
        return mPlaying;
    }

    public void pause() {
        mPlaying = false;
        pauseTimer();
        pauseMovie();
    }

    public void resume() {
        mPlaying = true;
        resumeTimer();
        resumeMovie();
    }

    private void pauseTimer() {
        mTimer.cancel();
    }

    private void resumeTimer() {
        // 렌더링 타이머 24fps
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                Log.d(TAG, "Timer");

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
    private AudioTrack prepareAudioTrack(int audioFormat, int sampleRateInHz,
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw BEGIN");

        canvas.drawColor(Color.BLACK);

        if (mBitmap != null) {
            if (mPlaying) {
                int ret = renderFrame(mBitmap);
                // 렌더링 종료
                if (ret > 0) {
                    pause();
//                    // 플레이 끝났을시 액티비티 종료
//                    ((PlayerActivity)mContext).finish();
                }
                else {
                    final long currentPositionUs = getCurrentPositionUs();
                    final PlayerActivity activity = (PlayerActivity)mContext;
                    if(activity != null) {
                        activity.updatePosition(currentPositionUs);
                    }
                }
            }

            // 항상 풀스크린으로 채우는 것은 안된다
            //TODO: 종횡비를 맞춰서 채워야 한다
            final int width = getWidth();
            final int height = getHeight();

            final int bmWidth = mBitmap.getWidth();
            final int bmHeight = mBitmap.getHeight();

            // 가로
            if (width > height) {
                final float bmRatioInverse = (float) bmWidth / bmHeight;

                // 비트맵 가로
                if (bmWidth > bmHeight) {
                    final float ratioInverse = (float) width / height;

                    // 비트맵이 더 길쭉할때
                    if(bmRatioInverse > ratioInverse) {
                        final float bmRatio = (float) bmHeight / bmWidth;
                        final int adjustedHeight = (int) (width * bmRatio);
                        final int startHeight = (height >> 1) - (adjustedHeight >> 1);
                        canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(0, startHeight, width, startHeight + adjustedHeight), null);
                    }
                    else {// 비트맵 세로랑 동일하다
                        final int adjustedWidth = (int) (height * bmRatioInverse);
                        final int startWidth = (width >> 1) - (adjustedWidth >> 1);
                        canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(startWidth, 0, startWidth + adjustedWidth, height), null);
                    }
                }
                // 비트맵 세로
                else {
                    final int adjustedWidth = (int) (height * bmRatioInverse);
                    final int startWidth = (width >> 1) - (adjustedWidth >> 1);
                    canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(startWidth, 0, startWidth + adjustedWidth, height), null);
                }
            }
            // 세로
            else {
                final float bmRatio = (float) bmHeight / bmWidth;

                // 비트맵 가로
                if (bmWidth > bmHeight) {
                    // 가로를 맞춘다
                    final int adjustedHeight = (int) (width * bmRatio);
                    final int startHeight = (height >> 1) - (adjustedHeight >> 1);
                    canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(0, startHeight, width, startHeight + adjustedHeight), null);
                }
                // 비트맵 세로
                else {
                    // 세로-세로 이므로 비율을 계산해야 한다
                    final float ratio = (float) height / width;

                    // 비트맵이 더 길쭉할때
                    if(bmRatio > ratio) {
                        // 세로룰 맞춘다
                        // 세로는 화면 길이
                        // 가로는 비율에 맞게
                        final float bmRatioInverse = (float) bmWidth / bmHeight;
                        final int adjustedWidth = (int) (height * bmRatioInverse);
                        final int startWidth = (width >> 1) - (adjustedWidth >> 1);
                        canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(startWidth, 0, startWidth + adjustedWidth, height), null);
                    }
                    else {// 비트맵 가로랑 동일하다
                        // 가로를 맞춘다
                        // 가로는 화면 길이
                        // 세로는 비율에 맞게
                        final int adjustedHeight = (int) (width * bmRatio);
                        final int startHeight = (height >> 1) - (adjustedHeight >> 1);
                        canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), new Rect(0, startHeight, width, startHeight + adjustedHeight), null);
                    }
                }
            }
        }
//        Log.d(TAG, "onDraw END");
    }

    static {
        System.loadLibrary("basicplayer");
    }

    private native void initAudioTrack();
    private native int initBasicPlayer();

    private native int openMovie(String filePath);
    private native int renderFrame(Bitmap bitmap);

    private native int getMovieWidth();
    private native int getMovieHeight();
    public native void closeMovie();

    private native void pauseMovie();
    private native void resumeMovie();
    private native int seekMovie(long positionUs);

    public native long getMovieDurationUs();
    private native double getFps();

    public native long getCurrentPositionUs();
}