package com.duongame.basicplayer;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by namjungsoo on 2016. 5. 24..
 */
public class PermissionManager {
    private final static int PERMISSION_STORAGE = 1;

    public static boolean checkStoragePermissions(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                return false;
            }
        }
        return true;
    }

    public static boolean onRequestPermissionsResult(Activity context, int requestCode, String[] permissions, int[] grantResults) {
        final String read = Manifest.permission.READ_EXTERNAL_STORAGE;
        final String write = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        boolean readEnable = false;
        boolean writeEnable = false;

        for (int i = 0; i < permissions.length; i++) {
            if (read.equals(permissions[i]) && grantResults[i] == 0)
                readEnable = true;
            if (write.equals(permissions[i]) && grantResults[i] == 0)
                writeEnable = true;
        }

        if (readEnable && writeEnable)
            return true;
        return false;
    }
}
