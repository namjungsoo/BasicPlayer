package com.duongame.basicplayer.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.duongame.basicplayer.MoviePlayView;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FullscreenManager;

public class PlayerActivity extends Activity {
    private final static String TAG = "PlayerActivity";

    MoviePlayView playView;
    ViewGroup toolBox;
    class ToolBoxAnimation extends Animation {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);
        playView = (MoviePlayView) findViewById(R.id.moviePlay);
        toolBox = (ViewGroup )findViewById(R.id.toolBox);

        if(playView != null) {
            playView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setToolBox(!FullscreenManager.isFullscreen());
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
                }
            });
            String filename = getIntent().getStringExtra("filename");
            playView.openFile(filename);
        }

        final ImageButton play = (ImageButton)findViewById(R.id.play);
        if(play != null) {// 일시정지를 시키자
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(playView.getPlaying()) {
                        playView.pause();
                        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.pause, getApplicationContext().getTheme()));
                    }
                    else {
                        playView.resume();
                        play.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.play, getApplicationContext().getTheme()));
                    }
                }
            });
        }

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
        setToolBox(true);
    }

    private void setToolBox(boolean newFullscreen) {
        AlphaAnimation animation;
        if(newFullscreen) {
            animation = new AlphaAnimation(1.f, 0.0f);
        }
        else {
            animation = new AlphaAnimation(0.0f, 1.f);
        }
        animation.setFillAfter(true);
        animation.setFillEnabled(true);
        animation.setDuration(300);
        animation.setInterpolator(new AccelerateInterpolator());
        toolBox.startAnimation(animation);
    }

    private void applyNavigationBarHeight(boolean portrait) {
        Resources resources = this.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            int deviceHeight = resources.getDimensionPixelSize(resourceId);

            LinearLayout layout = (LinearLayout) findViewById(R.id.toolBox);

            // 수직일때는 하단
            if(portrait) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(0, 0, 0, deviceHeight);
                layout.setLayoutParams(params);
            }
            // 수평일때는 우측
            else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) layout.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                layout.setLayoutParams(params);
            }
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
