package com.duongame.basicplayer.manager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.duongame.basicplayer.activity.PlayerActivity;

/**
 * Created by js296 on 2017-06-06.
 */

public class FileManager {
    public static void openFile(Context context, String filename, long time, int rotation) {
        final Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("time", time);
        intent.putExtra("rotation", rotation);
        context.startActivity(intent);
    }

    public static boolean checkRecentFile(final Context context, final String newFilename) {
        final String filename = PreferenceManager.getRecentFilename(context);
        final long time = PreferenceManager.getRecentTime(context);
        final int rotation = PreferenceManager.getRecentRotation(context);

        if(newFilename.equals(filename)) {
            AlertManager.showAlertRecentFile(context, filename, time, rotation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 처음부터 읽자
                    FileManager.openFile(context, newFilename, 0L, 0);
                }
            });
            return true;
        }
        return false;
    }

    public static boolean checkRecentFile(final Context context) {
        final String filename = PreferenceManager.getRecentFilename(context);
        final long time = PreferenceManager.getRecentTime(context);
        final int rotation = PreferenceManager.getRecentRotation(context);

        if (filename.length() > 0) {
            // 확인해보고 열자
//            openFile(filename, time);
            AlertManager.showAlertRecentFile(context, filename, time, rotation, null);
            return true;
        }
        return false;
    }

}
