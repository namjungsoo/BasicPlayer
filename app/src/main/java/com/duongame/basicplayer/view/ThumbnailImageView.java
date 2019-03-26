package com.duongame.basicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

import com.duongame.basicplayer.util.UnitConverter;

/**
 * Created by namjungsoo on 2016-06-21.
 */
public class ThumbnailImageView extends RoundedImageView {
    private static final String TAG = ThumbnailImageView.class.getSimpleName();
    private final int FONT_SIZE_DP = 13;
    private final int FONT_MINUS_DP = 2;

    private String timeText;
    private boolean isNew;

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
        this.timeText = timeText;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setNew(boolean bNew) {
        isNew = bNew;
    }

    public boolean getNew() {
        return isNew;
    }

    private void drawTimeText(Canvas canvas) {
        final Paint paint = new Paint();
        paint.setColor(0xff000000);

        final int fontSizeDp = UnitConverter.dpToPx(FONT_SIZE_DP);
        paint.setTextSize(fontSizeDp);
        paint.setAntiAlias(true);

        if (timeText == null) {
            Log.e(TAG, "timeText is null " + getTag());
            return;
        }

        int length = (int) paint.measureText(timeText);

        // 백판 검정
        final int fontMinusDp = UnitConverter.dpToPx(FONT_MINUS_DP);
        canvas.drawRect(new Rect(getWidth() - length, getHeight() - fontSizeDp + fontMinusDp, getWidth(), getHeight()), paint);

        // 시간 텍스트 렌더링
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.WHITE);
        canvas.drawText(timeText, getWidth(), getHeight(), paint);

        if (isNew) {
            paint.setTextSkewX(-0.25f);
            paint.setColor(Color.RED);

            canvas.drawRect(new Rect(getWidth() - length, getHeight() - fontSizeDp + fontMinusDp, getWidth(), getHeight()), paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawTimeText(canvas);
    }
}