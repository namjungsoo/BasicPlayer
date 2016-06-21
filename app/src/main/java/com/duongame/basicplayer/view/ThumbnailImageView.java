package com.duongame.basicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.duongame.basicplayer.util.UnitConverter;

/**
 * Created by namjungsoo on 2016-06-21.
 */
public class ThumbnailImageView extends RoundedImageView {
    private String mTimeText;
    private final int FONT_SIZE_DP = 13;
    private final int FONT_MINUS_DP = 2;

    public ThumbnailImageView(Context context) {
        super(context);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThumbnailImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTimeText(String timeText) {
        mTimeText = timeText;
    }

    public String getTimeText() {
        return mTimeText;
    }

    private void drawTimeText(Canvas canvas) {
        final Paint paint = new Paint();
        paint.setColor(0xff000000);

        final int fontSizeDp = UnitConverter.dpToPx(FONT_SIZE_DP);
        paint.setTextSize(fontSizeDp);
        paint.setAntiAlias(true);

        int length = (int) paint.measureText(mTimeText);

        final int fontMinusDp = UnitConverter.dpToPx(FONT_MINUS_DP);

        canvas.drawRect(new Rect(getWidth() - length, getHeight() - fontSizeDp + fontMinusDp, getWidth(), getHeight()), paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.WHITE);

        canvas.drawText(mTimeText, getWidth(), getHeight(), paint);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTimeText(canvas);
    }
}