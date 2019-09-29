package com.duongame.basicplayer.controller;

import android.view.View;

import com.duongame.basicplayer.view.GLPlayerView;

public class GLPlayerController extends PlayerController {
    GLPlayerView glPlayerView;

    public GLPlayerController(View playerView) {
        super(playerView);

        glPlayerView = (GLPlayerView) playerView;
    }

    @Override
    protected void initPlayerRenderer(final int movieWidth, final int movieHeight) {
        playerRenderer = glPlayerView.getRenderer();
        glPlayerView.post(new Runnable() {
            @Override
            public void run() {
                playerRenderer.initBitmap(movieWidth, movieHeight);
            }
        });
    }

    @Override
    protected void requestRender() {
        glPlayerView.requestRender();
    }
}
