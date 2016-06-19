package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.activity.PlayerActivity;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by namjungsoo on 16. 6. 18..
 */
public class PlayerView extends View {
    private final static String TAG = "PlayerView";

    private Bitmap mBitmap;
    private int mMovieWidth;
    private int mMovieHeight;
    private Timer mTimer;
    private Context mContext;
    private long mInterval;
    private boolean mPlaying;
//    private Player mPlayer = new Player();

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        Player.closeMovie();

        init(context);
    }

    public PlayerView(Context context) {
        this(context, null);
    }

    public void init(Context context) {
        Log.d(TAG, "init");

        if (Player.initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        Player.initAudioTrack();
    }

    private void initRenderTimer() {
        double fps = Player.getFps();
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

        int openResult = Player.openMovie(filename);
        if (openResult < 0) {
            Toast.makeText(mContext, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
            return false;
        } else {
            mMovieWidth = Player.getMovieWidth();
            mMovieHeight = Player.getMovieHeight();

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
        Player.pauseMovie();
    }

    public void resume() {
        mPlaying = true;
        resumeTimer();
        Player.resumeMovie();
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

                PlayerView.this.post(new Runnable() {
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

    public int seekMovie(long positionUs) {
        return Player.seekMovie(positionUs);
    }

    public long getMovieDurationUs() {
        return Player.getMovieDurationUs();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw BEGIN");

        canvas.drawColor(Color.BLACK);

        if (mBitmap != null) {
            if (mPlaying) {
                int ret = Player.renderFrame(mBitmap);
                // 렌더링 종료
                if (ret > 0) {
                    pause();
                    final PlayerActivity activity = (PlayerActivity)mContext;
                    if(activity != null) {
                        activity.updatePlayButton();
                    }

//                    // 플레이 끝났을시 액티비티 종료
//                    ((PlayerActivity)mContext).finish();
                }
                else {
                    final long currentPositionUs = Player.getCurrentPositionUs();
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

}
