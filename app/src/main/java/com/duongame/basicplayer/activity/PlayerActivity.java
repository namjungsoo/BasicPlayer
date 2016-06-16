package com.duongame.basicplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.duongame.basicplayer.MoviePlayView;
import com.duongame.basicplayer.manager.FullscreenManager;

public class PlayerActivity extends Activity {
    private final static String TAG="PlayerActivity";

    MoviePlayView playView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String filename = getIntent().getStringExtra("filename");
        playView = new MoviePlayView(this, filename);
        playView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
            }
        });

        setContentView(playView);

        FullscreenManager.setFullscreen(this, true);
    }
}
