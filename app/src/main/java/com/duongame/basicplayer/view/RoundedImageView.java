package com.duongame.basicplayer.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by namjungsoo on 2016-06-19.
 */
public class RoundedImageView extends ImageView {
    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final Path clipPath = new Path();

        final float radius = dpToPx(5);
        final float padding = 0;
        final int w = this.getWidth();
        final int h = this.getHeight();

        clipPath.addRoundRect(new RectF(padding, padding, w - padding, h - padding), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        super.onDraw(canvas);
    }
}
