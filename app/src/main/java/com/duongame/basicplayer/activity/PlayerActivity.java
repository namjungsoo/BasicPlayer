package com.duongame.basicplayer.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.duongame.basicplayer.BuildConfig;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.manager.ScreenManager;
import com.duongame.basicplayer.util.TimeConverter;
import com.duongame.basicplayer.util.UnitConverter;
import com.duongame.basicplayer.view.GLPlayerView;
import com.duongame.basicplayer.view.PlayerView;
import com.google.android.gms.ads.AdView;

import java.io.File;

import static com.duongame.basicplayer.manager.AdInterstitialManager.MODE_EXIT;

public class PlayerActivity extends BaseActivity {
    private final static String TAG = "PlayerActivity";

    //TEST
    //private GLPlayerView playerView;
    private PlayerView playerView;

    private ViewGroup toolBox;
    //private float alpha;

    private ImageButton play;
    private ImageButton rotate;

    private TextView currentTime;
    private TextView durationTime;
    private TextView degree;

    private SeekBar seekBar;
    private TextView seekTime;

    private TextView debugCurrent;
    private FrameLayout playerFrame;

    private AdView adView;

    //private int actionBarHeight;
    //private int statusBarHeight;
    //private FirebaseRemoteConfig firebaseRemoteConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player);

//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
//        setSupportActionBar(toolbar);

        playerFrame = (FrameLayout) findViewById(R.id.playerFrame);
        //playerView = new GLPlayerView(this);
        playerView = new PlayerView(this);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        playerView.setLayoutParams(params);
        playerFrame.addView(playerView, 0);
        //TEST
        //playerView = findViewById(R.id.playerView);
        toolBox = (ViewGroup) findViewById(R.id.toolBox);

        currentTime = (TextView) findViewById(R.id.currentTime);
        durationTime = (TextView) findViewById(R.id.durationTime);
        degree = (TextView) findViewById(R.id.degree);
        debugCurrent = (TextView) findViewById(R.id.debugCurrent);

        play = (ImageButton) findViewById(R.id.play);
        rotate = (ImageButton) findViewById(R.id.rotate);

        // 전체화면에 그려지는 흰색 탐색 시간
        seekTime = (TextView) findViewById(R.id.seekTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        // 광고 처리
        //PRO
        if (BuildConfig.SHOW_AD)
            initAd();

        initSeekBar();

        initRotation();

        initFullscreen();

        initPause();

        initActionBar();

//        initConfigs();

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);

//        setToolBox(false);

        // 최초에 GONE으로 초기화 해야 초반에 튀는 화면이 보이지 않는다.
        if (adView != null) {
            adView.setVisibility(View.GONE);
        }

        toolBox.setVisibility(View.GONE);

        updateRotation();

        Log.e(TAG, "PlayerActivity.onCreate threadId=" + Thread.currentThread().getId());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "PlayerActivity.runOnUiThread threadId=" + Thread.currentThread().getId());
                openFile();
            }
        });
    }


//    private void initConfigs() {
//        // Get Remote Config instance.
//        // [START get_remote_config_instance]
//        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
//        // [END get_remote_config_instance]
//
//        // Create a Remote Config Setting to enable developer mode, which you can use to increase
//        // the number of fetches available per hour during development. See Best Practices in the
//        // README for more information.
//        // [START enable_dev_mode]
//        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
//                .setDeveloperModeEnabled(BuildConfig.DEBUG)
//                .build();
//        firebaseRemoteConfig.setConfigSettings(configSettings);
//        // [END enable_dev_mode]
//
//        // Set default Remote Config parameter values. An app uses the in-app default values, and
//        // when you need to adjust those defaults, you set an updated value for only the values you
//        // want to change in the Firebase console. See Best Practices in the README for more
//        // information.
//        // [START set_default_values]
//        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
//        // [END set_default_values]
//    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        playerView.pause(false);
        updatePlayButton();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        updateFullscreenAd();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        playerView.close();
        if (adView != null) {
            playerFrame.removeView(adView);
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences pref = getSharedPreferences("player", MODE_PRIVATE);
        int count = pref.getInt("exit_count", 0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("exit_count", count + 1);
        edit.apply();

        if (count % 2 == 0) {
            AdInterstitialManager.showAd(this, MODE_EXIT, new AdInterstitialManager.OnFinishListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        } else {
            //finish();
            super.onBackPressed();
        }
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

        if (toolBox.getVisibility() == View.GONE)
            toolBox.setAlpha(0.0f);
        else {
            if (FullscreenManager.isFullscreen())
                toolBox.setAlpha(0.0f);
            else
                toolBox.setAlpha(1.0f);
        }

        updateRotation();
    }

    private void initAd() {
        if (playerFrame != null) {
            int actionBarHeight = ScreenManager.getActionBarHeight(this);
            int statusBarHeight = ScreenManager.getStatusBarHeight(this);
            final AdView adView = AdBannerManager.getAdTopBannerView();
            final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.TOP;
            adView.setLayoutParams(params);
            adView.setY(actionBarHeight + statusBarHeight);
            playerFrame.addView(adView, 1);
            this.adView = adView;
        }
    }

    private void initSeekBar() {
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                private boolean startAtPaused = false;

                @Override
                public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                    // 유저가 움직였을 경우에만 탐색
                    if (fromUser) {
                        final long positionUs = progress * TimeConverter.SEC_TO_US;
                        playerView.seekMovie(positionUs);
                        seekTime.setText(TimeConverter.convertUsToString(positionUs));
                        playerView.invalidate();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    if (playerView != null) {
                        playerView.setSeeking(true);
                        if (!playerView.getPlaying())
                            startAtPaused = true;
                        else {
                            startAtPaused = false;
                            playerView.pause(false);
                            updatePlayButton();
                        }
                        seekTime.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (playerView != null) {
                        // 플레이 상태 복구
                        if (!startAtPaused) {
                            playerView.resume();
                            updatePlayButton();
                        }
                        playerView.setSeeking(false);
                        seekTime.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }

    private void updateFullscreenAd() {
        if (!FullscreenManager.isFullscreen()) {
            // 광고를 보여줌
            setAdView(true);
        } else {
            setAdView(false);
        }
    }

    private void initFullscreen() {
        if (playerView != null) {
            // 풀스크린 처리
            playerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 풀스크린 모드를 반전한다.
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());

                    // 풀스크린에서 광고 체크
                    updateFullscreenAd();

                    // 현재가 풀스크린이면 보여주고
                    // 현재가 풀스크린이 아니면 숨겨준다.
                    setToolBox(!FullscreenManager.isFullscreen());

                    // 포즈 상태이면
                    if (!playerView.getPlaying()) {
                        if (adView != null && adView.getVisibility() == View.VISIBLE)
                            setAdView(!FullscreenManager.isFullscreen());
                    }

                    playerView.invalidate();
                }
            });
        }
    }

    private void initRotation() {
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playerView != null) {
                    final int newRotation = (playerView.getBitmapRotation() + 1) % (Surface.ROTATION_270 + 1);
                    playerView.setBitmapRotation(newRotation);
                    updateBitmapRotation();
                    playerView.invalidate();
                }
            }
        });
    }

    private void initPause() {
        if (play != null) {// 일시정지를 시키자
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playerView != null) {
                        Animation animation;
                        if (playerView.getPlaying()) {
                            playerView.pause(false);
                        } else {
                            playerView.resume();
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

    private void openFile() {
        final String filename = getIntent().getStringExtra("filename");
        final long time = getIntent().getLongExtra("time", 0L);
        final int rotation = getIntent().getIntExtra("rotation", 0);

        boolean result;

        // 파일 읽기 성공일때
        if (playerView.openFile(filename)) {
            playerView.setBitmapRotation(rotation);
            updateBitmapRotation();

            playerView.seekMovie(time);

            final long durationUs = playerView.getMovieDurationUs();
            long durationSec = durationUs / TimeConverter.SEC_TO_US;
            final String duration = TimeConverter.convertUsToString(durationUs);

            if (durationTime != null)
                durationTime.setText(duration);

            if (seekBar != null)
                seekBar.setMax((int) durationSec);
            updatePlayButton();

            setTitle(new File(filename).getName());

            PreferenceManager.saveRecentFile(this, filename, time, playerView.getBitmapRotation());

            result = true;
        } else {
            result = false;
        }

        sendEventOpenFile(filename, result);
    }

    private void sendEventOpenFile(String filename, boolean result) {
        final Bundle bundle = new Bundle();
        bundle.putString("filename", filename);
        bundle.putBoolean("result", result);
//        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void updateBitmapRotation() {
        final int rotation = playerView.getBitmapRotation();
        if (rotation == Surface.ROTATION_0) {
            degree.setVisibility(View.INVISIBLE);
            rotate.setColorFilter(Color.WHITE);
        } else {
            switch (rotation) {
                case Surface.ROTATION_90:
                    degree.setText("90°");
                    break;
                case Surface.ROTATION_180:
                    degree.setText("180°");
                    break;
                case Surface.ROTATION_270:
                    degree.setText("270°");
                    break;
            }
            degree.setVisibility(View.VISIBLE);
            rotate.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
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
            currentTime.setText(position);

            long positionSec = positionUs / TimeConverter.SEC_TO_US;

            // SeekBar 포지션 업데이트
            seekBar.setProgress((int) positionSec);

            debugCurrent.setText("" + positionSec * 1000);
        }
    }

    private Animation createAlphaAnimation(boolean showing) {
        AlphaAnimation animation;

        if (showing) {
            animation = new AlphaAnimation(0.0f, 1.0f);
        } else {
            animation = new AlphaAnimation(1.0f, 0.0f);
        }
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateInterpolator());
        return animation;
    }

    private void setAdView(boolean showing) {
        Log.d(TAG, "setAdView " + showing);

        if (adView != null) {
            adView.setVisibility(View.VISIBLE);
            // 기본값으로 설정후에 애니메이션 한다
            adView.setAlpha(1.0f);
            final Animation animation = createAlphaAnimation(showing);

            adView.startAnimation(animation);
        }
    }

    private void setToolBox(boolean showing) {
        Log.d(TAG, "setToolBox " + showing);

        toolBox.setVisibility(View.VISIBLE);
        // 기본값으로 설정후에 애니메이션 한다
        toolBox.setAlpha(1.0f);

        final Animation animation = createAlphaAnimation(showing);

        toolBox.startAnimation(animation);
    }

    private void applyNavigationBarHeight(boolean portrait) {
        // 플레이어에게 회전정보를 입력
        if (playerView != null) {
            playerView.setPortrait(portrait);
        }

        if (portrait) {
            seekTime.setTextSize(UnitConverter.dpToPx(24));
        } else {
            seekTime.setTextSize(UnitConverter.dpToPx(32));
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
        if (playerView.getPlaying()) {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause, getApplicationContext().getTheme()));
        } else {
            play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play, getApplicationContext().getTheme()));
        }
    }

    public void pause() {
        playerView.pause(false);
    }
}
