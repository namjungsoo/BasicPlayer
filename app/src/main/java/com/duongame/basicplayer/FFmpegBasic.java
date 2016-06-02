package com.duongame.basicplayer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;

public class FFmpegBasic extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        MoviePlayView playView = new MoviePlayView(this);
        setContentView(playView);
    }
}

class MoviePlayView extends View {
    private Bitmap mBitmap;

    public MoviePlayView(Context context) {
        super(context);
        
        if (initBasicPlayer() < 0) {
        	Toast.makeText(context, "CPU doesn't support NEON", Toast.LENGTH_LONG).show();
        	
        	((Activity)context).finish();
        }
        
        //String fname = "/mnt/sdcard/HSTest/T4_MVI_1498.AVI";
        String fname = "/mnt/sdcard/mediaweb.mp4";
        File file = new File(fname);
        Log.d("jungsoo", String.valueOf(file.exists()));

        
        int openResult = openMovie(fname);
        if (openResult < 0) {
        	Toast.makeText(context, "Open Movie Error: " + openResult, Toast.LENGTH_LONG).show();
        	
        	((Activity)context).finish();
        }
        else
        	mBitmap = Bitmap.createBitmap(getMovieWidth(), getMovieHeight(), Bitmap.Config.RGB_565);
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	renderFrame(mBitmap);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        invalidate();
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