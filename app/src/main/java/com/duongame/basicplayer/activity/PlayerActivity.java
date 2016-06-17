package com.duongame.basicplayer.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.duongame.basicplayer.MoviePlayView;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FullscreenManager;

public class PlayerActivity extends Activity {
    private final static String TAG="PlayerActivity";

    MoviePlayView playView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_player);
        playView = (MoviePlayView)findViewById(R.id.moviePlay);
        playView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenManager.setFullscreen(PlayerActivity.this, !FullscreenManager.isFullscreen());
            }
        });
        String filename = getIntent().getStringExtra("filename");
        playView.openFile(filename);

        FullscreenManager.setFullscreen(this, true);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
        playView.pause();
        playView.closeMovie();
    }
}
