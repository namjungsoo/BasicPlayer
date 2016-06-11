package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

/**
 * Created by namjungsoo on 16. 6. 11..
 */
class MoviePlayView extends View {
    private final static String TAG="MoviePlayView";
    private Bitmap mBitmap;

    public MoviePlayView(Context context) {
        super(context);

        closeMovie();
    }

    public void init(Context context) {
        Log.d(TAG,"init");

        if (initBasicPlayer() < 0) {
            Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        }

        String fname = "/mnt/sdcard/ar18-1.avi";
        File file = new File(fname);
        Log.d("jungsoo", String.valueOf(file.exists()));

        int openResult = openMovie(fname);
        if (openResult < 0) {
            Toast.makeText(context, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
            ((Activity) context).finish();
        } else {
            mBitmap = Bitmap.createBitmap(getMovieWidth(), getMovieHeight(), Bitmap.Config.ARGB_8888);
            Log.d(TAG,"init createBitmap");
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.d(TAG,"onDraw");
        if(mBitmap != null) {
            renderFrame(mBitmap);
            canvas.drawBitmap(mBitmap, 0, 0, null);

            invalidate();
//            Log.d(TAG,"onDraw invalidate");
        }
    }

    static {
        System.loadLibrary("basicplayer");
    }

    public static native int initBasicPlayer();

    public static native int openMovie(String filePath);

    public static native int renderFrame(Bitmap bitmap);

    public static native int getMovieWidth();

    public static native int getMovieHeight();

    public static native void closeMovie();
}