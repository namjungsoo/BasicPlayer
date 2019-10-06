package com.duongame.basicplayer.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.renderer.Square;
import com.duongame.basicplayer.renderer.SubtitleRenderer;
import com.duongame.basicplayer.util.SmiParser;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

//TODO:
// 1. 렌더링시 UI 업데이트
// 2. Pause 처리
// 3. 자막 렌더링
// 4. 크기 딱맞게 업데이트
// 5. 회전 처리 (회전은 텍스처 좌표를 적용하자)
// 6. YUV 처리

public class GLPlayerView extends GLSurfaceView {
    private final static String TAG = "GLPlayerView";

    // 순수하게 Player 관련 항목
    private boolean isPlaying;
    private boolean isSeeking;
    private int rotation = Surface.ROTATION_0;
    private boolean isPortrait = true;

    private ArrayList<SmiParser.Subtitle> subtitleList;
    private String filename;
    private Player player = new Player();
    private SubtitleRenderer subtitleRenderer = new SubtitleRenderer();
    private TouchHandler touchHandler = new TouchHandler();

    final Rect target = new Rect();
    final Rect src = new Rect();

    byte[] arrayY;
    byte[] arrayU;
    byte[] arrayV;

    public GLPlayerView(Context context) {
        this(context, null);
    }

    public GLPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        player.init();

        setEGLContextClientVersion(2);
        setRenderer(new GLRenderer());
    }

    public boolean onTouch(View v, MotionEvent ev) {
        return touchHandler.handleTouch(v, ev);
    }

    public boolean openFile(final String filename) {
        // 파일 존재 여부 체크
        final File file = new File(filename);
        Log.d(TAG, String.valueOf(file.exists()));

        int openResult = player.openMovie(filename, 0, 0);
        if (openResult < 0) {
            Toast.makeText(getContext(), "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) getContext()).finish();
            return false;
        } else {
            final int movieWidth = player.getMovieWidth();
            final int movieHeight = player.getMovieHeight();

            arrayY = new byte[movieWidth * movieHeight];
            arrayU = new byte[movieWidth * movieHeight / 4];
            arrayV = new byte[movieWidth * movieHeight / 4];
            Log.d(TAG, "init createBitmap");

            subtitleList = null;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 자막이 있으면 자막을 로딩하자
                    final String smiFile = filename.substring(0, filename.lastIndexOf(".")) + ".smi";
                    Log.d(TAG, "smiFile=" + smiFile);

                    final SmiParser parser = new SmiParser();
                    try {
                        parser.load(smiFile);
                        subtitleList = parser.getSubtitleList();
                    } catch (IOException e) {
                        e.printStackTrace();
                        subtitleList = null;
                    }
                }
            }).start();

            //initRenderTimer();
            resume();

            this.filename = filename;
            return true;
        }
    }

    public boolean getPlaying() {
        return isPlaying;
    }

    public void setSeeking(boolean b) {
        isSeeking = b;
    }

    public void setBitmapRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getBitmapRotation() {
        return rotation;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public void pause(boolean end) {
        isPlaying = false;
        //pauseTimer();
        Player.pauseMovie();

        PreferenceManager.saveRecentFile(getContext(), filename, player.getCurrentPositionUs(), getBitmapRotation());
    }

    public void resume() {
        isPlaying = true;
        //resumeTimer();
        Player.resumeMovie();
    }

    public int seekMovie(long positionUs) {
        return player.seekMovie(positionUs);
    }

    public long getMovieDurationUs() {
        return player.getMovieDurationUs();
    }

    public void close() {
        player.closeMovie();
    }

    class GLRenderer implements Renderer {
        private String TAG = GLRenderer.class.getSimpleName();
        private Square mSquare;

        private final float[] mMVPMatrix = new float[16];
        private final float[] mProjectionMatrix = new float[16];
        private final float[] mViewMatrix = new float[16];

        private int mTextureIdY;
        private int mTextureIdU;
        private int mTextureIdV;

//        ByteBuffer bufferY;
//        ByteBuffer bufferU;
//        ByteBuffer bufferV;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.e(TAG, "GLRenderer.onSurfaceCreated threadId=" + Thread.currentThread().getId());

            // Set the background frame color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            mSquare = new Square();

            if (mTextureIdY == 0) {
                mTextureIdY = initTexture(player.getMovieWidth(), player.getMovieHeight(), arrayY);
            }
            if (mTextureIdU == 0) {
                mTextureIdU = initTexture(player.getMovieWidth() / 2, player.getMovieHeight() / 2, arrayU);
            }
            if (mTextureIdV == 0) {
                mTextureIdV = initTexture(player.getMovieWidth() / 2, player.getMovieHeight() / 2, arrayV);
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
            if (isPlaying) {
                player.renderFrameYUVTexId(player.getMovieWidth(), player.getMovieHeight(), mTextureIdY, mTextureIdU, mTextureIdV);
//                player.renderFrameYUVArray(arrayY, arrayU, arrayV);
//                if (mTextureIdY != 0) {
//                    updateTexture(player.getMovieWidth(), player.getMovieHeight(), mTextureIdY, arrayY);
//                }
//                if (mTextureIdU != 0) {
//                    updateTexture(player.getMovieWidth() / 2, player.getMovieHeight() / 2, mTextureIdU, arrayU);
//                }
//                if (mTextureIdV != 0) {
//                    updateTexture(player.getMovieWidth() / 2, player.getMovieHeight() / 2, mTextureIdV, arrayV);
//                }
            }

            // Draw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            Matrix.setIdentityM(mViewMatrix, 0);
            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
            // Draw square
            mSquare.draw(mMVPMatrix, mTextureIdY, mTextureIdU, mTextureIdV);
        }

        int initTexture(int width, int height, byte[] array) {
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

            ByteBuffer buffer = ByteBuffer.wrap(array);
            buffer.position(0);

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    width,
                    height,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    buffer);

            buffer.clear();
            return textureHandle[0];
        }

        void updateTexture(int width, int height, int texId, byte[] array) {
            // init 되기전에 update가 호출될수 있음
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);

            ByteBuffer buffer = ByteBuffer.wrap(array);
            buffer.position(0);

            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0,
                    width,
                    height,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    buffer);
            buffer.clear();
        }
    }
}
