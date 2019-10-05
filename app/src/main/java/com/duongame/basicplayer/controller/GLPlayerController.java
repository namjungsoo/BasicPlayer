package com.duongame.basicplayer.controller;

import android.view.View;

import com.duongame.basicplayer.view.GLPlayerView;

public class GLPlayerController extends PlayerController {
    private static String TAG = GLPlayerController.class.getSimpleName();
    GLPlayerView glPlayerView;

    public GLPlayerController(View playerView) {
        super(playerView);

        glPlayerView = (GLPlayerView) playerView;
        glPlayerView.initRenderer(this);
    }

    @Override
    public void initPlayerRendererer() {
        playerRenderer = glPlayerView.getRenderer();
    }

    @Override
    protected void requestRender() {
        glPlayerView.requestRender();
    }
}
