package com.duongame.basicplayer.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.duongame.basicplayer.renderer.GLRenderer;

public class GLPlayerView extends GLSurfaceView {
    private final static String TAG = "GLPlayerView";

    private TouchHandler touchHandler = new TouchHandler();
    private GLRenderer renderer;

    public GLPlayerView(Context context) {
        this(context, null);
    }

    public GLPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        renderer = new GLRenderer();

        setEGLContextClientVersion(2);
        setRenderer(new GLRenderer());
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public GLRenderer getRenderer() {
        return renderer;
    }

    //region
    public boolean onTouch(View v, MotionEvent ev) {
        return touchHandler.handleTouch(v, ev);
    }
    //endregion
}
