package com.duongame.basicplayer.renderer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.duongame.basicplayer.controller.PlayerController;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer extends PlayerRenderer implements GLSurfaceView.Renderer {
    private static String TAG = GLRenderer.class.getSimpleName();
    private Square mSquare;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private int mTextureId;

    public GLRenderer(PlayerController playerController) {
        super(playerController);
    }

//    @Override
//    public void initBitmap(int movieWidth, int movieHeight) {
//        super.initBitmap(movieWidth, movieHeight);
//        Log.e(TAG, "GLRenderer.initBitmap=" + getBitmap());
//
//        mBitmap = getBitmap();
//    }

//    @Override
//    public void renderFrame(Player player) {
//        Log.e(TAG, "GLRenderer.renderFrame=" + getBitmap());
//    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "GLRenderer.onSurfaceCreated threadId=" + Thread.currentThread().getId());

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        mSquare = new Square();

        Log.e(TAG, "GLRenderer.onSurfaceCreated mTextureId=" + mTextureId + " " + getBitmap());
        if (mTextureId == 0) {
            playerController.preparePlaying();
            mTextureId = initTexture(getBitmap());
            Log.e(TAG, "GLRenderer.onSurfaceCreated mTextureId=" + mTextureId + " getBitmap=" + getBitmap());
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, height, 0, -10, 10);
        mSquare.updateBB(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        renderFrame();
        if (mTextureId != 0) {
            updateTexture(getBitmap(), mTextureId);
        }

        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        // Draw square
        mSquare.draw(mMVPMatrix, mTextureId);
    }

    int initTexture(Bitmap bitmap) {
        Log.e(TAG, "initTexture bitmap=" + bitmap + " threadId=" + Thread.currentThread().getId());
        if (bitmap == null) {
            Log.e(TAG, "initTexture bitmap is null");
            return 0;
        }
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] == 0) {
            throw new RuntimeException("Error loading texture.");
        }
        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        return textureHandle[0];
    }

    void updateTexture(Bitmap bitmap, int texId) {
        // init 되기전에 update가 호출될수 있음
        if (bitmap == null)
            return;

        Log.e(TAG, "updateTexture=" + bitmap);

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

        GLUtils.texSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, bitmap);
    }
}
