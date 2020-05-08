package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.activity.PlayerActivity;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.renderer.SubtitleRenderer;
import com.duongame.basicplayer.util.SmiParser;

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

    private Timer timer;// GL은 Timer로 렌더링 하지 않음
    private long interval;// Timer interval
    //private Context context;// View는 getContext가 있음

    // 순수하게 Player 관련 항목
    //private Bitmap bitmap;
    private Bitmap bitmapY, bitmapU, bitmapV;
    private boolean isPlaying;
    private boolean isSeeking;
    private int rotation = Surface.ROTATION_0;
    private boolean isPortrait = true;
    private ArrayList<SmiParser.Subtitle> subtitleList;
    private String filename;
    private Player player = new Player();
    private SubtitleRenderer subtitleRenderer = new SubtitleRenderer();
    private TouchHandler touchHandler = new TouchHandler();
    final Rect target = new Rect();
    final Rect src = new Rect();

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.context = context;

        player.init();
    }

    public PlayerView(Context context) {
        this(context, null);
    }

    public boolean onTouch(View v, MotionEvent ev) {
        return touchHandler.handleTouch(v, ev);
    }

    public boolean openFile(final String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = player.openMovie(filename, 0, 0);
        if (openResult < 0) {
            Toast.makeText(getContext(), "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) getContext()).finish();
            return false;
        } else {
            final int movieWidth = player.getMovieWidth();
            final int movieHeight = player.getMovieHeight();

            bitmapY = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.ALPHA_8);
            bitmapU = Bitmap.createBitmap(movieWidth/2, movieHeight/2, Bitmap.Config.ALPHA_8);
            bitmapV = Bitmap.createBitmap(movieWidth/2, movieHeight/2, Bitmap.Config.ALPHA_8);
            Log.d(TAG, "init createBitmap");

            subtitleList = null;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 자막이 있으면 자막을 로딩하자
                    final String smiFile = filename.substring(0, filename.lastIndexOf(".")) + ".smi";
                    Log.d(TAG, "smiFile=" + smiFile);

                    final SmiParser parser = new SmiParser();
                    try {
                        parser.load(smiFile);
                        subtitleList = parser.getSubtitleList();
                    } catch (IOException e) {
                        e.printStackTrace();
                        subtitleList = null;
                    }
                }
            }).start();

            initRenderTimer();
            resume();

            this.filename = filename;
            return true;
        }
    }

    public boolean getPlaying() {
        return isPlaying;
    }

    public void setSeeking(boolean b) {
        isSeeking = b;
    }

    public void setBitmapRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getBitmapRotation() {
        return rotation;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public void pause(boolean end) {
        isPlaying = false;
        pauseTimer();
        Player.pauseMovie();

        PreferenceManager.saveRecentFile(getContext(), filename, player.getCurrentPositionUs(), getBitmapRotation());
    }

    public void resume() {
        isPlaying = true;
        resumeTimer();
        Player.resumeMovie();
    }

    public int seekMovie(long positionUs) {
        return player.seekMovie(positionUs);
    }

    public long getMovieDurationUs() {
        return player.getMovieDurationUs();
    }

    public void close() {
        player.closeMovie();
    }

    //beginregion
    //Timer
    private void initRenderTimer() {
        double fps = player.getFps();
        Log.d(TAG, "fps=" + fps);

        interval = (long) (1000. / fps);
        if (interval == 0)
            interval = 1;
        Log.d(TAG, "interval=" + interval);
    }

    private void pauseTimer() {
        timer.cancel();
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

        timer = new Timer();
        timer.schedule(task, 0, interval);
    }
    //endregion


    protected boolean canvasApply(Canvas canvas, int width, int height, int rotation) {
        boolean degree90 = false;
        if (rotation != Surface.ROTATION_0) {
            canvas.save();
            float degree = 0.0f;

            switch (this.rotation) {
                case Surface.ROTATION_90:
                    degree90 = true;
                    degree = 90.0f;
                    break;
                case Surface.ROTATION_180:
                    degree = 180.0f;
                    break;
                case Surface.ROTATION_270:
                    degree90 = true;
                    degree = 270.0f;
                    break;
            }


            // 전체 화면의 기준으로 회전한다
            // 이미지가 화면에 꽉찼을 경우에
            canvas.rotate(degree, width / 2, height / 2);
        }
        return degree90;
    }

    protected void canvasDraw(Canvas canvas) {
        canvas.drawBitmap(bitmapY, src, target, null);
    }

    protected void canvasRestore(Canvas canvas, int rotation) {
        if (rotation != Surface.ROTATION_0) {
            canvas.restore();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw BEGIN");

        canvas.drawColor(Color.BLACK);

        long currentPositionUs = -1;
        if (bitmapY != null) {
            currentPositionUs = player.getCurrentPositionUs();

            // 항상 풀스크린으로 채우는 것은 안된다
            final int bmWidth = bitmapY.getWidth();
            final int bmHeight = bitmapY.getHeight();

            if (isPlaying || isSeeking) {
                int ret = player.renderFrame(bitmapY);
                //int ret = player.renderFrameYUV(bitmapY, bitmapU, bitmapV);
                // 렌더링 종료
                if (ret > 0) {
                    pause(true);
                    final PlayerActivity activity = (PlayerActivity) getContext();
                    if (activity != null) {
                        activity.updatePlayButton();
                    }
                } else {
                    final PlayerActivity activity = (PlayerActivity) getContext();
                    if (activity != null) {
                        activity.updatePosition(currentPositionUs);
                    }
                }
            }

            boolean degree90;
            final int width = getWidth();
            final int height = getHeight();

            degree90 = canvasApply(canvas, width, height, rotation);

            final float bmRatioInverse = (float) bmWidth / bmHeight;
            final float bmRatio = (float) bmHeight / bmWidth;
            final float ratioInverse = (float) width / height;

            final int adjustedHeight = (int) (width * bmRatio);
            final int startHeight = (height - adjustedHeight) >> 1;

            final int adjustedWidth = (int) (height * bmRatioInverse);
            final int startWidth = (width - adjustedWidth) >> 1;

//            Log.d(TAG, "adjustedHeight=" + adjustedHeight + " startHeight=" + startHeight + " adjustedWidth=" + adjustedWidth + " startWidth=" + startWidth);

            if (degree90) {
                // 화면은 변함이 없다
                // 화면에 회전할 사이즈대로 그리자

                boolean landscapeImage = bmRatioInverse < ratioInverse;

                final int rotation = ((PlayerActivity) getContext()).getWindowManager().getDefaultDisplay().getRotation();
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

            src.set(0, 0, bmWidth, bmHeight);

            canvasDraw(canvas);

            canvasRestore(canvas, rotation);

            if (subtitleList != null && currentPositionUs > -1) {
                subtitleRenderer.render(getContext(), canvas, currentPositionUs, isPortrait, getWidth(), getHeight());
            }
        }
//        Log.d(TAG, "onDraw END");
    }

}
