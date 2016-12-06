package com.duongame.basicplayer.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by namjungsoo on 2016. 5. 24..
 */
public class PermissionManager {
    private final static String TAG = "PermissionManager";

    private final static int PERMISSION_STORAGE = 1;
    private static boolean readRequest;
    private static boolean writeRequest;

    public static boolean checkStoragePermissions(Activity context, boolean read, boolean write) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            readRequest = read;
            writeRequest = write;

            final ArrayList<String> requestList = new ArrayList<String>();

            if (writeRequest) {
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
            if (readRequest) {
                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }

            if (requestList.size() > 0) {
                final String[] requestArray = new String[requestList.size()];
                for (int i = 0; i < requestList.size(); i++) {
                    requestArray[i] = requestList.get(i);
                }

                context.requestPermissions(requestArray, PERMISSION_STORAGE);
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

        if (readEnable == readRequest && writeEnable == writeRequest) {
            Log.d(TAG, "onRequestPermissionsResult true");
            return true;
        }

        Log.d(TAG, "onRequestPermissionsResult false");
        return false;
    }
}
