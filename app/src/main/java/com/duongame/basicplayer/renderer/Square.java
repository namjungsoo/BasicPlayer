package com.duongame.basicplayer.renderer;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by js296 on 2017-04-26.
 */

public class Square {
    private final static String TAG = "Square";

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 vTexCoord;" +
                    "varying vec2 TexCoord;" +
                    "void main() {" +
                    "  TexCoord = vTexCoord;" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D texY;" +
                    "uniform sampler2D texU;" +
                    "uniform sampler2D texV;" +
                    "varying vec2 TexCoord;" +
                    "void main() {" +
                    "   highp float y = texture2D(texY, TexCoord).r;" +
                    "   highp float u = texture2D(texU, TexCoord).r;" +
                    "   highp float v = texture2D(texV, TexCoord).r;" +
                    "   y = 1.1643 * (y - 0.0625);" +
                    "   u = u - 0.5;" +
                    "   v = v - 0.5;" +
                    "   highp float r = y + 1.5958 * v;" +
                    "   highp float g = y - 0.39173 * u - 0.81290 * v;" +
                    "   highp float b = y + 2.017 * u;" +
                    "   gl_FragColor = vec4(r, g, b, 1.0);" +
                    "}";
    private FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static final int TEX_COORDS_PER_VERTEX = 2;
    static final float scale = 600.0f;
    static float squareCoords[] = {
            0, 1, 0.0f,   // top left
            0, 0, 0.0f,   // bottom left
            1, 0, 0.0f,   // bottom right
            1, 1, 0.0f};  // top right

    private final short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    float color[] = {0.2f, 0.709803922f, 0.898039216f, 1.0f};

    private int mTexCoordHandle;
    static float texCoords[] = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };
    private final FloatBuffer texCoordBuffer;

    private int texY, texU, texV;

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */

    public Square() {
        updateBB(1, 1);

        ByteBuffer tbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                texCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        texCoordBuffer = tbb.asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        // prepare shaders and OpenGL program
        int vertexShader = loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }

    public static int loadShader(int type, String shaderCode) {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void updateBB(int width, int height) {
        for (int i = 0; i < squareCoords.length; i++) {
            if (squareCoords[i] != 0.0f) {
                if (i % 3 == 0) {
                    squareCoords[i] = width;
                } else {
                    squareCoords[i] = height;
                }
            }
        }
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     *                  this shape.
     */
    public void draw(float[] mvpMatrix, int texIdY, int texIdU, int texIdV) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);


        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        checkGlError("glGetAttribLocation vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
                mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "vTexCoord");
        checkGlError("glGetAttribLocation vTexCoord");
        Log.d("TAG", "mTexCoordHandle=" + mTexCoordHandle);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(
                mTexCoordHandle, TEX_COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, texCoordBuffer);

        // 텍스처 유닛별 바인딩
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdY);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdU);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texIdV);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        // 텍스처 바인딩
        texY = GLES20.glGetUniformLocation(mProgram, "texY");
        texU = GLES20.glGetUniformLocation(mProgram, "texU");
        texV = GLES20.glGetUniformLocation(mProgram, "texV");
        GLES20.glUniform1i(texY, 0);
        GLES20.glUniform1i(texU, 1);
        GLES20.glUniform1i(texV, 2);

        // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordHandle);
    }
}
