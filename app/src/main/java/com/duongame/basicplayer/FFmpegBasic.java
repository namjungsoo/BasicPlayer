package com.duongame.basicplayer;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class FFmpegBasic extends Activity {
    private final static String TAG="FFmpegBasic";

    MoviePlayView playView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        playView = new MoviePlayView(this);

        if(PermissionManager.checkStoragePermissions(this)) {
            Log.d(TAG,"onCreate checkStoragePermissions");
            playView.init(this);
            setContentView(playView);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(PermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            Log.d(TAG,"onRequestPermissionsResult");
            playView.init(this);
            setContentView(playView);
        }
    }
}
