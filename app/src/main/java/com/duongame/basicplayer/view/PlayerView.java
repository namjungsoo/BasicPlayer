package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.VelocityTracker;
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

import static com.duongame.basicplayer.view.PlayerView.Axis.AXIS_X;
import static com.duongame.basicplayer.view.PlayerView.Axis.AXIS_Y;

/**
 * Created by namjungsoo on 16. 6. 18..
 */
public class PlayerView extends View implements IPlayerView {
    private final static String TAG = "PlayerView";

    private Bitmap bitmap;
    private Timer timer;
    private Context context;
    private long interval;
    private boolean isPlaying;
    private boolean isSeeking;
    private int rotation = Surface.ROTATION_0;
    private boolean isPortrait = true;
    private ArrayList<SmiParser.Subtitle> subtitleList;
    private String filename;
    private Player player = new Player();

    //region
    // Touch
    protected enum Axis {
        AXIS_X,
        AXIS_Y,
        AXIS_BOTH
    }

    Axis touchAxis = AXIS_X;

    // touch
    boolean isBeingDragged = false;
    PointF lastMotionPt = new PointF();
    private PointF initialMotionPt = new PointF();

    // configuration
    private VelocityTracker velocityTracker = null;
    private int touchSlop = 0;

    private void startDragXIfNeeded(MotionEvent ev) {
        final float x = ev.getX(0);
        final float xSignedDiff = x - initialMotionPt.x;
        final float xDiff = Math.abs(xSignedDiff);
        if (xDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    private void startDragYIfNeeded(MotionEvent ev) {
        final float y = ev.getY(0);
        final float ySignedDiff = y - initialMotionPt.y;
        final float yDiff = Math.abs(ySignedDiff);
        if (yDiff < touchSlop) {
            isBeingDragged = false;
            return;
        }
        isBeingDragged = true;
    }

    public boolean handleTouch(View v, MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastMotionPt.x = initialMotionPt.x = ev.getX(0);
                lastMotionPt.y = initialMotionPt.y = ev.getY(0);
                return true;
                //break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (!isBeingDragged) {
                    if (touchAxis == AXIS_X) {
                        startDragXIfNeeded(ev);
                    } else if (touchAxis == AXIS_Y) {
                        startDragYIfNeeded(ev);
                    }
                }
                final float x = ev.getX(0);
                final float y = ev.getY(0);
                lastMotionPt.x = x;
                lastMotionPt.y = y;
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }

                // 내가 캡쳐 했으면 true
                if (handleActionUp()) {
                    return true;
                } else {
                    v.performClick();
                }
                break;
            }
        }

        // 하위 뷰에게 전달하려면 false
        return false;
    }

    protected boolean handleActionUp() {
        return false;
    }

    public boolean onTouch(View v, MotionEvent ev) {
        return handleTouch(v, ev);
    }
    //endregion

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        player.init();
    }

    public PlayerView(Context context) {
        this(context, null);
    }

    private void initRenderTimer() {
        double fps = player.getFps();
        Log.d(TAG, "fps=" + fps);

        interval = (long) (1000. / fps);
        if (interval == 0)
            interval = 1;
        Log.d(TAG, "interval=" + interval);
    }

    public boolean openFile(final String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = player.openMovie(filename, 0, 0);
        if (openResult < 0) {
            Toast.makeText(context, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
            return false;
        } else {
            final int movieWidth = player.getMovieWidth();
            final int movieHeight = player.getMovieHeight();

            bitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.ARGB_8888);
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

        PreferenceManager.saveRecentFile(context, filename, player.getCurrentPositionUs(), getBitmapRotation());
    }

    public void resume() {
        isPlaying = true;
        resumeTimer();
        Player.resumeMovie();
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

    public int seekMovie(long positionUs) {
        return player.seekMovie(positionUs);
    }

    public long getMovieDurationUs() {
        return player.getMovieDurationUs();
    }

    public void close() {
        player.closeMovie();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw BEGIN");

        canvas.drawColor(Color.BLACK);

        long currentPositionUs = -1;
        if (bitmap != null) {
            currentPositionUs = player.getCurrentPositionUs();

            // 항상 풀스크린으로 채우는 것은 안된다
            final int bmWidth = bitmap.getWidth();
            final int bmHeight = bitmap.getHeight();

            if (isPlaying || isSeeking) {
                int ret = player.renderFrame(bitmap);
                // 렌더링 종료
                if (ret > 0) {
                    pause(true);
                    final PlayerActivity activity = (PlayerActivity) context;
                    if (activity != null) {
                        activity.updatePlayButton();
                    }
                } else {
                    final PlayerActivity activity = (PlayerActivity) context;
                    if (activity != null) {
                        activity.updatePosition(currentPositionUs);
                    }
                }
            }

            boolean degree90 = false;
            final int width = getWidth();
            final int height = getHeight();

            if (rotation != Surface.ROTATION_0) {
                canvas.save();
                float rotation = 0.0f;

                switch (this.rotation) {
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

                final int rotation = ((PlayerActivity) context).getWindowManager().getDefaultDisplay().getRotation();
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

            canvas.drawBitmap(bitmap, new Rect(0, 0, bmWidth, bmHeight), target, null);

            if (rotation != Surface.ROTATION_0) {
                canvas.restore();
            }

            drawSubtitle(canvas, currentPositionUs);
        }
//        Log.d(TAG, "onDraw END");
    }

    private void drawSubtitle(Canvas canvas, long currentPositionUs) {
//        Log.d(TAG, "drawSubtitle currentPositionUs="+currentPositionUs);

        // 자막이 있으면 렌더링 하자
        if (subtitleList != null && currentPositionUs > -1) {
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
            if (isPortrait) {
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

                if (isPortrait) {
                    int actionBarHeight = ScreenManager.getNavigationBarHeight(context);
                    if (actionBarHeight == 0) {
                        actionBarHeight = UnitConverter.dpToPx(48);
                    }

                    subtitleY -= actionBarHeight;
                }
            }

            for (int i = subtitleList.size() - 1; i >= 0; i--) {
                // 역순으로 자막을 가져와서
                SmiParser.Subtitle subtitle = subtitleList.get(i);

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
