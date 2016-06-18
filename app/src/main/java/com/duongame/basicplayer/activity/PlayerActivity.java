package com.duongame.basicplayer.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
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

    private MoviePlayView mPlayerView;
    private ViewGroup mToolBox;
    private float mAlpha;
    private ImageButton mPlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);
        mPlayerView = (MoviePlayView) findViewById(R.id.moviePlay);
        mToolBox = (ViewGroup) findViewById(R.id.toolBox);

        mPlay = (ImageButton) findViewById(R.id.play);
        if (mPlay != null) {// 일시정지를 시키자
            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlayerView.getPlaying()) {
                        mPlayerView.pause();
                    } else {
                        mPlayerView.resume();
                    }
                    updatePlayButton();
                }
            });
        }

        if (mPlayerView != null) {
            mPlayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setmToolBox(!FullscreenManager.isFullscreen());
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
                }
            });
            final String filename = getIntent().getStringExtra("filename");
            mPlayerView.openFile(filename);
            updatePlayButton();
        }

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
        setmToolBox(true);

        // 타이틀바 반투명
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));

        // 타이틀바 백버튼 보이기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
        mPlayerView.pause();
        updatePlayButton();
//        mPlayerView.closeMovie();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        Log.d(TAG, "onConfigurationChanged mAlpha="+mAlpha);
        super.onConfigurationChanged(newConfig);

        // 현재 풀스크린일때
//        if (FullscreenManager.isFullscreen()) {
//            mToolBox.setAlpha(0.0f);
//            Log.d(TAG, "onConfigurationChanged 0.0");
//        } else {
//            mToolBox.setAlpha(1.0f);
//            Log.d(TAG, "onConfigurationChanged 1.0");
//        }
        Log.d(TAG, "" + mToolBox.getAlpha());

        mToolBox.setAlpha(mAlpha);

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

    private void setmToolBox(boolean newFullscreen) {
        final AlphaAnimation animation;

        // 기본값으로 설정후에 애니메이션 한다
        mToolBox.setAlpha(1.0f);
        if (newFullscreen) {
            mAlpha = 0.0f;
            animation = new AlphaAnimation(1.f, 0.0f);
        } else {
            mAlpha = 1.0f;
            animation = new AlphaAnimation(0.0f, 1.0f);
        }
//        Log.d(TAG, "setmToolBox newFullscreen="+newFullscreen +" mAlpha="+mAlpha);
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateInterpolator());
        mToolBox.startAnimation(animation);
//        Log.d(TAG, "setmToolBox newFullscreen="+newFullscreen + " END");
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

    private void updatePlayButton() {
        if (mPlayerView.getPlaying()) {
            mPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause, getApplicationContext().getTheme()));
        }
        else {
            mPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play, getApplicationContext().getTheme()));
        }
    }
}
