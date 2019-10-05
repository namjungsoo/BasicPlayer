package com.duongame.basicplayer.renderer;

import com.duongame.basicplayer.controller.PlayerController;

// canvas와 연관이 있는 것은 onDraw에서 직접 호출됨
public class BitmapRenderer extends PlayerRenderer {
    public BitmapRenderer(PlayerController playerController) {
        super(playerController);
    }
}
