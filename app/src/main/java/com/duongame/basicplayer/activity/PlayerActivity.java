package com.duongame.basicplayer.activity;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.duongame.basicplayer.MoviePlayView;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FullscreenManager;

public class PlayerActivity extends Activity {
    private final static String TAG = "PlayerActivity";

    MoviePlayView playView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);
        playView = (MoviePlayView) findViewById(R.id.moviePlay);
        if(playView != null) {
            playView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
                }
            });
            String filename = getIntent().getStringExtra("filename");
            playView.openFile(filename);
        }

        final Button play = (Button)findViewById(R.id.play);
        if(play != null) {// 일시정지를 시키자
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(playView.getPlaying())
                        playView.pause();
                    else
                        playView.resume();
                }
            });
        }

        applyNavigationBarHeight(true);
        FullscreenManager.setFullscreen(this, true);
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
