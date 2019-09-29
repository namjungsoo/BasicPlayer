package com.duongame.basicplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duongame.basicplayer.renderer.BitmapRenderer;

/**
 * Created by namjungsoo on 16. 6. 18..
 */
public class PlayerView extends View {
    private final static String TAG = "PlayerView";

    private TouchHandler touchHandler = new TouchHandler();

    //region
    public boolean onTouch(View v, MotionEvent ev) {
        return touchHandler.handleTouch(v, ev);
    }
    //endregion

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(Context context) {
        this(context, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        BitmapRenderer bitmapRenderer = (BitmapRenderer) getTag();
        if (bitmapRenderer == null)
            return;

        bitmapRenderer.updateUI(getContext());

        boolean degree90 = bitmapRenderer.applyRotation(canvas, getWidth(), getHeight());
        bitmapRenderer.beginRendering(getContext(), getWidth(), getHeight(), degree90);
        bitmapRenderer.render(canvas);
        bitmapRenderer.endRendering(canvas);

        bitmapRenderer.renderSubtitle(getContext(), canvas, getWidth(), getHeight());
    }
}
