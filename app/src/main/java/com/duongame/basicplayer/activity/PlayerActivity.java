package com.duongame.basicplayer.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
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
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.ScreenManager;
import com.duongame.basicplayer.util.TimeConverter;
import com.duongame.basicplayer.util.UnitConverter;
import com.duongame.basicplayer.view.PlayerView;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class PlayerActivity extends AppCompatActivity {
    private final static String TAG = "PlayerActivity";

    private PlayerView mPlayerView;

    private ViewGroup mToolBox;
    private float mAlpha;

    private ImageButton mPlay;
    private ImageButton mRotate;

    private TextView mCurrentTime;
    private TextView mDurationTime;
    private TextView mDegree;

    private SeekBar mSeekBar;
    private TextView mSeekTime;

    private TextView mDebugCurrent;
    private FrameLayout mPlayerFrame;

    private AdView mAdView;

    private int mActionBarHeight;
    private int mStatusBarHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);

//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
//        setSupportActionBar(toolbar);

        mPlayerFrame = (FrameLayout) findViewById(R.id.playerFrame);

        mPlayerView = (PlayerView) findViewById(R.id.playerView);
        mToolBox = (ViewGroup) findViewById(R.id.toolBox);

        mCurrentTime = (TextView) findViewById(R.id.currentTime);
        mDurationTime = (TextView) findViewById(R.id.durationTime);
        mDegree = (TextView) findViewById(R.id.degree);
        mDebugCurrent = (TextView) findViewById(R.id.debugCurrent);

        mPlay = (ImageButton) findViewById(R.id.play);
        mRotate = (ImageButton) findViewById(R.id.rotate);

        mSeekTime = (TextView) findViewById(R.id.seekTime);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        // 광고 처리
        initAd();

        initSeekBar();

        initRotation();

        initFullscreen();

        initPause();

        initActionBar();

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
        setToolBox(true);

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
    public void onStop() {
        Log.d(TAG, "onStop");

        super.onStop();

        mPlayerFrame.removeView(mAdView);
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

    private void initAd() {
        if (mPlayerFrame != null) {
            mActionBarHeight = ScreenManager.getActionBarHeight(this);
            mStatusBarHeight = ScreenManager.getStatusBarHeight(this);
            final AdView adView = AdBannerManager.getAdTopBannerView();
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP;
            adView.setLayoutParams(params);
            adView.setY(mActionBarHeight + mStatusBarHeight);
            mPlayerFrame.addView(adView, 1);
            mAdView = adView;
        }
    }

    private void initSeekBar() {
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                private boolean startAtPaused = false;

                @Override
                public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                    // 유저가 움직였을 경우에만 탐색
                    if (fromUser) {
                        Log.d(TAG, "progress=" + progress);
                        final long positionUs = progress * TimeConverter.SEC_TO_US;
                        Log.d(TAG, "seekMovie " + positionUs);
                        mPlayerView.seekMovie(positionUs);
                        mSeekTime.setText(TimeConverter.convertUsToString(positionUs));
                        mPlayerView.invalidate();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (mPlayerView != null) {
                        mPlayerView.setSeeking(true);
                        if (!mPlayerView.getPlaying())
                            startAtPaused = true;
                        else {
                            startAtPaused = false;
                            mPlayerView.pause();
                            updatePlayButton();
                        }
                        mSeekTime.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mPlayerView != null) {
//                        int progress = seekBar.getProgress();
//                        long position = progress * SEC_TO_US;
//                        Log.d(TAG, "seekMovie "+ position);
//                        mPlayerView.seekMovie(position);

                        // 플레이 상태 복구
                        if (!startAtPaused) {
                            mPlayerView.resume();
                            updatePlayButton();
                        }
                        mPlayerView.setSeeking(false);
                        mSeekTime.setVisibility(View.INVISIBLE);

                    }
                }
            });
        }
    }

    private void initFullscreen() {
        if (mPlayerView != null) {
            // 풀스크린 처리
            mPlayerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setToolBox(!FullscreenManager.isFullscreen());
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());

                    mPlayerView.invalidate();
                }
            });
            final String filename = getIntent().getStringExtra("filename");

            // 파일 읽기 성공일때
            if (mPlayerView.openFile(filename)) {
                final long durationUs = mPlayerView.getMovieDurationUs();
                long durationSec = durationUs / TimeConverter.SEC_TO_US;
                final String duration = TimeConverter.convertUsToString(durationUs);

                if (mDurationTime != null)
                    mDurationTime.setText(duration);

                if (mSeekBar != null)
                    mSeekBar.setMax((int) durationSec);
                updatePlayButton();

                setTitle(new File(filename).getName());
            }
        }
    }

    private void initRotation() {
        mRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayerView != null) {
                    final int newRotation = (mPlayerView.getBitmapRotation() + 1) % (Surface.ROTATION_270 + 1);
                    mPlayerView.setBitmapRotation(newRotation);
                    updateBitmapRotation();
                    mPlayerView.invalidate();
                }
            }
        });
    }

    private void initPause() {
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
    }

    private void initActionBar() {
        // 타이틀바 반투명 블랙
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#66000000")));

            // 타이틀바 백버튼 보이기
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateBitmapRotation() {
        final int rotation = mPlayerView.getBitmapRotation();
        if (rotation == Surface.ROTATION_0) {
            mDegree.setVisibility(View.INVISIBLE);
        } else {
            switch (rotation) {
                case Surface.ROTATION_90:
                    mDegree.setText("90°");
                    break;
                case Surface.ROTATION_180:
                    mDegree.setText("180°");
                    break;
                case Surface.ROTATION_270:
                    mDegree.setText("270°");
                    break;
            }
            mDegree.setVisibility(View.VISIBLE);
        }
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
            final String position = TimeConverter.convertUsToString(positionUs);
            mCurrentTime.setText(position);

            long positionSec = positionUs / TimeConverter.SEC_TO_US;

            // SeekBar 포지션 업데이트
            mSeekBar.setProgress((int) positionSec);

            mDebugCurrent.setText("" + positionSec * 1000);
        }
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
        mAdView.startAnimation(animation);
    }

    private void applyNavigationBarHeight(boolean portrait) {
        if (mPlayerView != null) {
            mPlayerView.setPortrait(portrait);
        }

        if (portrait) {
            mSeekTime.setTextSize(UnitConverter.dpToPx(24));
        } else {
            mSeekTime.setTextSize(UnitConverter.dpToPx(32));
        }

        // 소프트키가 없을 경우에 패스
        if (!ScreenManager.hasSoftKeyMenu(this))
            return;

        final int size = ScreenManager.getNavigationBarHeight(this);
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
