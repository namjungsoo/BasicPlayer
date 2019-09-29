package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.renderer.BitmapRenderer;
import com.duongame.basicplayer.renderer.SubtitleRenderer;
import com.duongame.basicplayer.util.SmiParser;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerController {
    private final static String TAG = "PlayerController";

    private Timer timer;
    private long interval;
    private boolean isPlaying;
    private boolean isSeeking;

    private Player player = new Player();
    private View playerView;

    private String filename;

    private BitmapRenderer bitmapRenderer;
    private SubtitleRenderer subtitleRenderer = new SubtitleRenderer();

    public PlayerController(View playerView) {
        this.playerView = playerView;
        player.init();
    }

    private void initRenderTimer() {
        double fps = player.getFps();
        Log.d(TAG, "fps=" + fps);

        interval = (long) (1000. / fps);
        if (interval == 0)
            interval = 1;
        Log.d(TAG, "interval=" + interval);
    }

    public boolean openFile(Context context, final String filename) {
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

            //bitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.ARGB_8888);
            bitmapRenderer = new BitmapRenderer(movieWidth, movieHeight);
            Log.d(TAG, "init createBitmap");
            bitmapRenderer.setSubtitleRenderer(subtitleRenderer);

            subtitleRenderer.setSubtitleList(null);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 자막이 있으면 자막을 로딩하자
                    final String smiFile = filename.substring(0, filename.lastIndexOf(".")) + ".smi";
                    Log.d(TAG, "smiFile=" + smiFile);

                    final SmiParser parser = new SmiParser();
                    try {
                        parser.load(smiFile);
                        subtitleRenderer.setSubtitleList(parser.getSubtitleList());
                    } catch (IOException e) {
                        e.printStackTrace();
                        subtitleRenderer.setSubtitleList(null);
                    }
                }
            }).start();

            initRenderTimer();
            resume(context);

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
        //this.rotation = rotation;
        if (bitmapRenderer == null)
            return;
        bitmapRenderer.setRotation(rotation);
    }

    public int getBitmapRotation() {
        //return rotation;
        return bitmapRenderer.getRotation();
    }

    public void setPortrait(boolean portrait) {
        if (bitmapRenderer == null)
            return;
        bitmapRenderer.setPortrait(portrait);
    }

    public void pause(Context context, boolean end) {
        isPlaying = false;
        pauseTimer();
        Player.pauseMovie();

        PreferenceManager.saveRecentFile(context, filename, player.getCurrentPositionUs(), getBitmapRotation());
    }

    public void resume(Context context) {
        isPlaying = true;
        resumeTimer(context);
        Player.resumeMovie();
    }

    private void pauseTimer() {
        timer.cancel();
    }

    private void resumeTimer(final Context context) {
        // 렌더링 타이머 24fps
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
//                Log.d(TAG, "Timer");

                // 미리 렌더링후 invalidate 호출
                long currentPositionUs = player.getCurrentPositionUs();
                if (isPlaying || isSeeking) {
                    if (bitmapRenderer == null)
                        return;

                    bitmapRenderer.renderFrame(player);
                    //bitmapRenderer.updateUI(context);
                }

                PlayerController.this.playerView.setTag(bitmapRenderer);
                PlayerController.this.playerView.post(new Runnable() {
                    @Override
                    public void run() {
                        PlayerController.this.playerView.invalidate();
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
}
