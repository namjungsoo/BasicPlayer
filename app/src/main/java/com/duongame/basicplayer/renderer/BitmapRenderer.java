package com.duongame.basicplayer.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.activity.PlayerActivity;

// canvas와 연관이 있는 것은 onDraw에서 직접 호출됨
public class BitmapRenderer {
    private Bitmap bitmap;

    private int rotation = Surface.ROTATION_0;
    private boolean isPortrait = true;

    private final Rect target = new Rect();
    private final Rect src = new Rect();

    private int ret;
    private long currentPositionUs;
    SubtitleRenderer subtitleRenderer;

    public BitmapRenderer(int movieWidth, int movieHeight) {
        bitmap = Bitmap.createBitmap(movieWidth, movieHeight, Bitmap.Config.ARGB_8888);
    }

    // bitmap에 영상을 렌더링한다
    public void renderFrame(Player player) {
        if (bitmap == null) {
            return;
        }

        // -1: movie 없음
        // 1: 종료
        // 0: 정상
        ret = player.renderFrame(bitmap);
        currentPositionUs = player.getCurrentPositionUs();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return rotation;
    }

    public void setPortrait(boolean portrait) {
        this.isPortrait = portrait;
    }

    public boolean getPortrait() {
        return isPortrait;
    }

    public void setSubtitleRenderer(SubtitleRenderer subtitleRenderer) {
        this.subtitleRenderer = subtitleRenderer;
    }

    //if (isPlaying || isSeeking) {// 위치 업데이트가 필요함. 단, 렌더링 종료시에는 멈춤
    public void updateUI(Context context) {
        // 렌더링 종료. 정상 렌더링은 0리턴
        if (ret > 0) {
            final PlayerActivity activity = (PlayerActivity) context;
            if (activity != null) {
                activity.pause();
                activity.updatePlayButton();// pause 상황으로 버튼 변경
            }
        } else {// 플레이중이거나 탐색중이므로 현재 위치를 업데이트 해준다.
            final PlayerActivity activity = (PlayerActivity) context;
            if (activity != null) {
                activity.updatePosition(currentPositionUs);
            }
        }
    }

    // width, height는 view의 크기
    public boolean applyRotation(Canvas canvas, int width, int height) {
        boolean degree90 = false;

        if (rotation != Surface.ROTATION_0) {
            canvas.save();
            float degree = 0.0f;

            switch (rotation) {
                case Surface.ROTATION_90:
                    degree90 = true;
                    degree = 90.0f;
                    break;
                case Surface.ROTATION_180:
                    degree = 180.0f;
                    break;
                case Surface.ROTATION_270:
                    degree90 = true;
                    degree = 270.0f;
                    break;
            }

            // 전체 화면의 기준으로 회전한다
            // 이미지가 화면에 꽉찼을 경우에
            canvas.rotate(degree, width / 2, height / 2);
        }

        return degree90;
    }

    // target rect를 계산해 준다.
    public void beginRendering(Context context, int width, int height, boolean degree90) {
        final int bmWidth = bitmap.getWidth();
        final int bmHeight = bitmap.getHeight();

        final float bmRatioInverse = (float) bmWidth / bmHeight;
        final float bmRatio = (float) bmHeight / bmWidth;
        final float ratioInverse = (float) width / height;

        final int adjustedHeight = (int) (width * bmRatio);
        final int startHeight = (height - adjustedHeight) >> 1;

        final int adjustedWidth = (int) (height * bmRatioInverse);
        final int startWidth = (width - adjustedWidth) >> 1;

        if (degree90) {
            // 화면 영역의 변함이 있다
            // 화면에 회전할 사이즈대로 그리자
            boolean landscapeImage = bmRatioInverse < ratioInverse;

            final int rotation = ((PlayerActivity) context).getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    landscapeImage = !landscapeImage;
                    break;
            }

            //if (bmRatioInverse < ratioInverse) {// 가로 이미지
            if (landscapeImage) {// 가로 이미지
                int newHeight = (int) (bmRatio * height);
                int newWidth = height;
                int newStartY = (height - newHeight) >> 1;
                int newStartX = (width - newWidth) >> 1;

                target.set(newStartX, newStartY, newStartX + newWidth, newStartY + newHeight);
            } else {// 세로 이미지
                // width가 미래의 height가 될 것이므로
                int newWidth = (int) (bmRatioInverse * width);
                int newHeight = width;
                int newStartY = (height - newHeight) >> 1;
                int newStartX = (width - newWidth) >> 1;

                target.set(newStartX, newStartY, newStartX + newWidth, newStartY + newHeight);
            }
        } else {
            // 화면 영역 변함이 없다
            if (bmRatioInverse > ratioInverse) {// 가로 이미지
                target.set(0, startHeight, width, startHeight + adjustedHeight);

            } else {// 세로 이미지
                target.set(startWidth, 0, startWidth + adjustedWidth, height);
            }
        }
    }

    public void render(Canvas canvas) {
        final int bmWidth = bitmap.getWidth();
        final int bmHeight = bitmap.getHeight();
        src.set(0, 0, bmWidth, bmHeight);
        canvas.drawBitmap(bitmap, src, target, null);
    }

    public void endRendering(Canvas canvas) {
        if (rotation != Surface.ROTATION_0) {
            canvas.restore();
        }
    }

    public void renderSubtitle(Context context, Canvas canvas, int width, int height) {
        if (subtitleRenderer == null)
            return;
        subtitleRenderer.render(context, canvas, currentPositionUs, isPortrait, width, height);
    }
}
