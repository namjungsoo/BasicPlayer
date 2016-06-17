package com.duongame.basicplayer.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.duongame.basicplayer.MoviePlayView;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.NavigationBarManager;

public class PlayerActivity extends AppCompatActivity {
    private final static String TAG = "PlayerActivity";

    private MoviePlayView playView;
    private ViewGroup toolBox;
    private float alpha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);
        playView = (MoviePlayView) findViewById(R.id.moviePlay);
        toolBox = (ViewGroup) findViewById(R.id.toolBox);

        if (playView != null) {
            playView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setToolBox(!FullscreenManager.isFullscreen());
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
                }
            });
            final String filename = getIntent().getStringExtra("filename");
            playView.openFile(filename);
        }

        final ImageButton play = (ImageButton) findViewById(R.id.play);
        if (play != null) {// 일시정지를 시키자
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playView.getPlaying()) {
                        playView.pause();
                        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause, getApplicationContext().getTheme()));
                    } else {
                        playView.resume();
                        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play, getApplicationContext().getTheme()));
                    }
                }
            });
        }

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
        setToolBox(true);

        // 타이틀바 반투명
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        Log.d(TAG, "onConfigurationChanged alpha="+alpha);
        super.onConfigurationChanged(newConfig);

        // 현재 풀스크린일때
//        if (FullscreenManager.isFullscreen()) {
//            toolBox.setAlpha(0.0f);
//            Log.d(TAG, "onConfigurationChanged 0.0");
//        } else {
//            toolBox.setAlpha(1.0f);
//            Log.d(TAG, "onConfigurationChanged 1.0");
//        }
        Log.d(TAG, "" + toolBox.getAlpha());

        toolBox.setAlpha(alpha);

        final int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                applyNavigationBarHeight(true);
                Log.d(TAG, "ROTATION_0");
                break;
            case Surface.ROTATION_90:
                Log.d(TAG, "ROTATION_90");
                applyNavigationBarHeight(false);
                break;
            case Surface.ROTATION_180:
                applyNavigationBarHeight(true);
                Log.d(TAG, "ROTATION_180");
                break;
            case Surface.ROTATION_270:
                Log.d(TAG, "ROTATION_270");
                applyNavigationBarHeight(false);
                break;

        }
    }

    private void setToolBox(boolean newFullscreen) {
        final AlphaAnimation animation;

        // 기본값으로 설정후에 애니메이션 한다
        toolBox.setAlpha(1.0f);
        if (newFullscreen) {
            alpha = 0.0f;
            animation = new AlphaAnimation(1.f, 0.0f);
        } else {
            alpha = 1.0f;
            animation = new AlphaAnimation(0.0f, 1.0f);
        }
//        Log.d(TAG, "setToolBox newFullscreen="+newFullscreen +" alpha="+alpha);
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateInterpolator());
        toolBox.startAnimation(animation);
//        Log.d(TAG, "setToolBox newFullscreen="+newFullscreen + " END");
    }

    private void applyNavigationBarHeight(boolean portrait) {
        // 소프트키가 없을 경우에 패스
        if (!NavigationBarManager.hasSoftKeyMenu(this))
            return;

        int size = NavigationBarManager.getNavigationBarHeight(this);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.toolBox);

        // 수직일때는 하단
        if (portrait) {
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, 0, size);
            layout.setLayoutParams(params);
        }
        // 수평일때는 우측
        else {
            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
            params.setMargins(0, 0, size, 0);
            layout.setLayoutParams(params);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
        playView.pause();
//        playView.closeMovie();
    }
}
