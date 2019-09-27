package com.duongame.basicplayer.renderer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.duongame.basicplayer.manager.FullscreenManager;
import com.duongame.basicplayer.manager.ScreenManager;
import com.duongame.basicplayer.util.SmiParser;
import com.duongame.basicplayer.util.UnitConverter;

import java.util.ArrayList;

public class SubtitleRenderer {
    public void render(Context context, Canvas canvas, ArrayList<SmiParser.Subtitle> subtitleList, long currentPositionUs, boolean isPortrait, int width, int height) {
//        Log.d(TAG, "drawSubtitle currentPositionUs="+currentPositionUs);

        // 자막이 있으면 렌더링 하자

        final Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

        final Paint strokePaint = new Paint();
        strokePaint.setColor(Color.BLACK);
        strokePaint.setAntiAlias(true);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextAlign(Paint.Align.CENTER);

        float textSize;
        float strokeWidth;
        if (isPortrait) {
            textSize = UnitConverter.dpToPx(13);
            strokeWidth = UnitConverter.dpToPx(2);
        } else {
            textSize = UnitConverter.dpToPx(20);
            strokeWidth = UnitConverter.dpToPx(3);
        }
        paint.setTextSize(textSize);
        strokePaint.setTextSize(textSize);
        strokePaint.setStrokeWidth(strokeWidth);

        float subtitleY;

        // 풀스크린은 위치를 조정 안한다.
        if (FullscreenManager.isFullscreen()) {
            subtitleY = height - UnitConverter.dpToPx(60);
        } else {
            subtitleY = height - UnitConverter.dpToPx(120);

            if (isPortrait) {
                int actionBarHeight = ScreenManager.getNavigationBarHeight(context);
                if (actionBarHeight == 0) {
                    actionBarHeight = UnitConverter.dpToPx(48);
                }

                subtitleY -= actionBarHeight;
            }
        }

        for (int i = subtitleList.size() - 1; i >= 0; i--) {
            // 역순으로 자막을 가져와서
            SmiParser.Subtitle subtitle = subtitleList.get(i);

            // 현재 시간이 현재 자막보다 크고
            if (currentPositionUs > subtitle.start * 1000) {
                if (subtitle.end == -1 || currentPositionUs < subtitle.end * 1000) {
                    canvas.drawText(subtitle.content, width / 2, subtitleY, strokePaint);
                    canvas.drawText(subtitle.content, width / 2, subtitleY, paint);
//                            Log.d(TAG, "currentPositionUs=" + currentPositionUs + " start=" + subtitle.start * 1000 + " end=" + subtitle.end * 1000);
                    break;
                }
            }
        }
    }

}
