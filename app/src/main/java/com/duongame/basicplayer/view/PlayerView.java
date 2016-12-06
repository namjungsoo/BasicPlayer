package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.activity.PlayerActivity;
import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.manager.ScreenManager;
import com.duongame.basicplayer.util.SmiParser;
import com.duongame.basicplayer.util.UnitConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by namjungsoo on 16. 6. 18..
 */
public class PlayerView extends View {
    private final static String TAG = "PlayerView";

    private Bitmap mBitmap;
//    private int mMovieWidth;
//    private int mMovieHeight;
    private Timer mTimer;
    private Context mContext;
    private long mInterval;
    private boolean mPlaying;
    private boolean mSeeking;
    private int mRotation = Surface.ROTATION_0;
    private boolean mPortrait = true;
    private ArrayList<SmiParser.Subtitle> mSubtitleList;
    private String mFilename;

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;

        Player.closeMovie();
    }

    public PlayerView(Context context) {
        this(context, null);
    }

    private void initRenderTimer() {
        double fps = Player.getFps();
        Log.d(TAG, "fps=" + fps);

        mInterval = (long) (1000. / fps);
        if (mInterval == 0)
            mInterval = 1;
        Log.d(TAG, "mInterval=" + mInterval);
    }

    public boolean openFile(final String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = Player.openMovie(filename);
        if (openResult < 0) {
            Toast.makeText(mContext, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) mContext).finish();
            return false;
        } else {
            final int movieWidth = Player.getMovieWidth();
            final int movieHeight = Player.getMovieHeight();

            mBitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.ARGB_8888);
            Log.d(TAG, "init createBitmap");

            mSubtitleList = null;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 자막이 있으면 자막을 로딩하자
                    final String smiFile = filename.substring(0, filename.lastIndexOf(".")) + ".smi";
                    Log.d(TAG, "smiFile=" + smiFile);

                    final SmiParser parser = new SmiParser();
                    try {
                        parser.load(smiFile);
                        mSubtitleList = parser.getSubtitleList();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mSubtitleList = null;
                    }
                }
            }).start();

            initRenderTimer();
            resume();

            mFilename = filename;
            return true;
        }
    }

    public boolean getPlaying() {
        return mPlaying;
    }

    public void setSeeking(boolean b) {
        mSeeking = b;
    }

    public void setBitmapRotation(int rotation) {
        mRotation = rotation;
    }

    public int getBitmapRotation() {
        return mRotation;
    }

    public void setPortrait(boolean portrait) {
        mPortrait = portrait;
    }

    public void pause(boolean end) {
        mPlaying = false;
        pauseTimer();
        Player.pauseMovie();

        PreferenceManager.saveRecentFile(mContext, mFilename, Player.getCurrentPositionUs(), getBitmapRotation());
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

        long currentPositionUs = -1;
        if (mBitmap != null) {
            currentPositionUs = Player.getCurrentPositionUs();
            if (mPlaying || mSeeking) {
                int ret = Player.renderFrame(mBitmap);
                // 렌더링 종료
                if (ret > 0) {
                    pause(true);
                    final PlayerActivity activity = (PlayerActivity) mContext;
                    if (activity != null) {
                        activity.updatePlayButton();
                    }
                } else {
                    final PlayerActivity activity = (PlayerActivity) mContext;
                    if (activity != null) {
                        activity.updatePosition(currentPositionUs);
                    }
                }
            }

            boolean degree90 = false;
            final int width = getWidth();
            final int height = getHeight();

            // 항상 풀스크린으로 채우는 것은 안된다
            final int bmWidth = mBitmap.getWidth();
            final int bmHeight = mBitmap.getHeight();

            if (mRotation != Surface.ROTATION_0) {
                canvas.save();
                float rotation = 0.0f;

                switch (mRotation) {
                    case Surface.ROTATION_90:
                        degree90 = true;
                        rotation = 90.0f;
                        break;
                    case Surface.ROTATION_180:
                        rotation = 180.0f;
                        break;
                    case Surface.ROTATION_270:
                        degree90 = true;
                        rotation = 270.0f;
                        break;
                }


                // 전체 화면의 기준으로 회전한다
                // 이미지가 화면에 꽉찼을 경우에
                canvas.rotate(rotation, width / 2, height / 2);
            }

            final float bmRatioInverse = (float) bmWidth / bmHeight;
            final float bmRatio = (float) bmHeight / bmWidth;
            final float ratioInverse = (float) width / height;

            final int adjustedHeight = (int) (width * bmRatio);
            final int startHeight = (height - adjustedHeight) >> 1;

            final int adjustedWidth = (int) (height * bmRatioInverse);
            final int startWidth = (width - adjustedWidth) >> 1;

//            Log.d(TAG, "adjustedHeight=" + adjustedHeight + " startHeight=" + startHeight + " adjustedWidth=" + adjustedWidth + " startWidth=" + startWidth);
            final Rect target = new Rect();

            if (degree90) {
                // 화면은 변함이 없다
                // 화면에 회전할 사이즈대로 그리자

                boolean landscapeImage = bmRatioInverse < ratioInverse;

                final int rotation = ((PlayerActivity) mContext).getWindowManager().getDefaultDisplay().getRotation();
                switch (rotation) {
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        landscapeImage = !landscapeImage;
                        break;
                }

                //if (bmRatioInverse < ratioInverse) {// 가로 이미지
                if (landscapeImage) {// 가로 이미지
                    int newHeight = (int) (bmRatio * height);
                    int newWidth = height;
                    int newStartY = (height - newHeight) >> 1;
                    int newStartX = (width - newWidth) >> 1;

                    target.set(newStartX, newStartY, newStartX + newWidth, newStartY + newHeight);
                } else {// 세로 이미지
                    // width가 미래의 height가 될 것이므로
                    int newWidth = (int) (bmRatioInverse * width);
                    int newHeight = width;
                    int newStartY = (height - newHeight) >> 1;
                    int newStartX = (width - newWidth) >> 1;

                    target.set(newStartX, newStartY, newStartX + newWidth, newStartY + newHeight);
                }
            } else {
                // 화면은 변함이 없다
                if (bmRatioInverse > ratioInverse) {// 가로 이미지
                    target.set(0, startHeight, width, startHeight + adjustedHeight);

                } else {// 세로 이미지
                    target.set(startWidth, 0, startWidth + adjustedWidth, height);
                }
            }

            canvas.drawBitmap(mBitmap, new Rect(0, 0, bmWidth, bmHeight), target, null);

            if (mRotation != Surface.ROTATION_0) {
                canvas.restore();
            }

            drawSubtitle(canvas, currentPositionUs);
        }
//        Log.d(TAG, "onDraw END");
    }

    private void drawSubtitle(Canvas canvas, long currentPositionUs) {
//        Log.d(TAG, "drawSubtitle currentPositionUs="+currentPositionUs);

        // 자막이 있으면 렌더링 하자
        if (mSubtitleList != null && currentPositionUs > -1) {
            final int width = getWidth();
            final int height = getHeight();

            final Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);

            final Paint strokePaint = new Paint();
            strokePaint.setColor(Color.BLACK);
            strokePaint.setAntiAlias(true);
            strokePaint.setStyle(Paint.Style.STROKE);
            strokePaint.setTextAlign(Paint.Align.CENTER);

            float textSize;
            float strokeWidth;
            if (mPortrait) {
                textSize = UnitConverter.dpToPx(13);
                strokeWidth = UnitConverter.dpToPx(2);
            } else {
                textSize = UnitConverter.dpToPx(20);
                strokeWidth = UnitConverter.dpToPx(3);
            }
            paint.setTextSize(textSize);
            strokePaint.setTextSize(textSize);
            strokePaint.setStrokeWidth(strokeWidth);

            float subtitleY;

            // 풀스크린은 위치를 조정 안한다.
            if (FullscreenManager.isFullscreen()) {
                subtitleY = height - UnitConverter.dpToPx(60);
            } else {
                subtitleY = height - UnitConverter.dpToPx(120);

                if (mPortrait) {
                    int actionBarHeight = ScreenManager.getNavigationBarHeight(mContext);
                    if (actionBarHeight == 0) {
                        actionBarHeight = UnitConverter.dpToPx(48);
                    }

                    subtitleY -= actionBarHeight;
                }
            }

            for (int i = mSubtitleList.size() - 1; i >= 0; i--) {
                // 역순으로 자막을 가져와서
                SmiParser.Subtitle subtitle = mSubtitleList.get(i);

                // 현재 시간이 현재 자막보다 크고
                if (currentPositionUs > subtitle.start * 1000) {
                    if (subtitle.end == -1 || currentPositionUs < subtitle.end * 1000) {
                        canvas.drawText(subtitle.content, width / 2, subtitleY, strokePaint);
                        canvas.drawText(subtitle.content, width / 2, subtitleY, paint);
//                            Log.d(TAG, "currentPositionUs=" + currentPositionUs + " start=" + subtitle.start * 1000 + " end=" + subtitle.end * 1000);
                        break;
                    }
                }
            }
        }
    }
}
