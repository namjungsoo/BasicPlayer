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
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.NavigationBarManager;
import com.duongame.basicplayer.view.PlayerView;

public class PlayerActivity extends AppCompatActivity {
    private final static String TAG = "PlayerActivity";
    private final static long SEC_TO_US = 1000000L;
    private final static float US_TO_SEC = 0.000001f;

    private PlayerView mPlayerView;
    private ViewGroup mToolBox;
    private float mAlpha;
    private ImageButton mPlay;
    private TextView mCurrentTime;
    private TextView mDurationTime;
    private SeekBar mSeekBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);

//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
//        setSupportActionBar(toolbar);

        mPlayerView = (PlayerView) findViewById(R.id.moviePlay);
        mToolBox = (ViewGroup) findViewById(R.id.toolBox);
        mCurrentTime = (TextView) findViewById(R.id.currentTime);
        mDurationTime = (TextView) findViewById(R.id.durationTime);
        mPlay = (ImageButton) findViewById(R.id.play);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                private boolean startAtPaused = false;
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 유저가 움직였을 경우에만 탐색
                    if (fromUser) {
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if(mPlayerView != null) {
                        if(!mPlayerView.getPlaying())
                            startAtPaused = true;
                        else {
                            startAtPaused = false;
                            mPlayerView.pause();
                            updatePlayButton();
                        }
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(mPlayerView != null) {
                        int progress = seekBar.getProgress();
                        long position = progress * SEC_TO_US;
                        Log.d(TAG, "seekMovie "+ position);
                        mPlayerView.seekMovie(position);

                        // 플레이 상태 복구
                        if(!startAtPaused) {
                            mPlayerView.resume();
                            updatePlayButton();
                        }
                    }
                }
            });
        }

        if (mPlay != null) {// 일시정지를 시키자
            mPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPlayerView != null) {
                        if (mPlayerView.getPlaying()) {
                            mPlayerView.pause();
                        } else {
                            mPlayerView.resume();
                        }
                    }
                    updatePlayButton();
                }
            });
        }

        if (mPlayerView != null) {
            mPlayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setToolBox(!FullscreenManager.isFullscreen());
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
                }
            });
            final String filename = getIntent().getStringExtra("filename");

            // 파일 읽기 성공일때
            if (mPlayerView.openFile(filename)) {
                final long durationUs = mPlayerView.getMovieDurationUs();
                long durationSec = durationUs / SEC_TO_US;
                final String duration = convertUsToString(durationUs);

                if (mDurationTime != null)
                    mDurationTime.setText(duration);

                if (mSeekBar != null)
                    mSeekBar.setMax((int) durationSec);
            }
            updatePlayButton();
        }

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
        setToolBox(true);

        // 타이틀바 반투명 블랙
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));

        // 타이틀바 백버튼 보이기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        updateRotation();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
        mPlayerView.pause();
        updatePlayButton();
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
        super.onConfigurationChanged(newConfig);

        mToolBox.setAlpha(mAlpha);
        updateRotation();
    }

    private void updateRotation() {
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

    public void updatePosition(long positionUs) {
        if (!FullscreenManager.isFullscreen()) {
            // 시간 텍스트 업데이트
            final String position = convertUsToString(positionUs);
            mCurrentTime.setText(position);

            long positionSec = positionUs / SEC_TO_US;

            // SeekBar 포지션 업데이트
            mSeekBar.setProgress((int) positionSec);
        }
    }

    private String convertUsToString(long timeUs) {
        // 초단위로 변경
        timeUs = timeUs / SEC_TO_US;

        long hour = timeUs / 3600;
        long min = (timeUs - (hour * 3600)) / 60;
        long sec = timeUs - (hour * 3600) - min * 60;

        return String.format("%01d:%02d:%02d", hour, min, sec);
    }

    private void setToolBox(boolean newFullscreen) {
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
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateInterpolator());
        mToolBox.startAnimation(animation);
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

    public void updatePlayButton() {
        if (mPlayerView.getPlaying()) {
            mPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause, getApplicationContext().getTheme()));
        } else {
            mPlay.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play, getApplicationContext().getTheme()));
        }
    }
}
