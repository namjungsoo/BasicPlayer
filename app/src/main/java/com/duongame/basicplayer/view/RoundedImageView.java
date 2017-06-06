package com.duongame.basicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.duongame.basicplayer.util.UnitConverter;

/**
 * Created by namjungsoo on 2016-06-19.
 */
public class RoundedImageView extends android.support.v7.widget.AppCompatImageView {
    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Path clipPath = new Path();

        final float radius = UnitConverter.dpToPx(5);
        final float padding = 0;
        final int w = this.getWidth();
        final int h = this.getHeight();

        clipPath.addRoundRect(new RectF(padding, padding, w - padding, h - padding), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        super.onDraw(canvas);
    }
}
