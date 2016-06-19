package com.duongame.basicplayer.manager;

import android.graphics.Bitmap;

import java.util.HashMap;

/**
 * Created by namjungsoo on 16. 6. 19..
 */
public class ThumbnailManager {

    // 파일을 png로 저장해야함
    // 근데 로컬에 저장해야함
    // /를 전부 _로 변경후 로컬에 저장
    //private static HashMap<String, String> cacheFileMap;
    private static HashMap<String, Bitmap> cacheBitmapMap = new HashMap<String, Bitmap>();

    public static void addBitmap(String path, Bitmap bitmap) {
        cacheBitmapMap.put(path, bitmap);
    }

    public static void clear() {
        cacheBitmapMap.clear();
    }

    public static Bitmap getBitmap(String path) {
        return cacheBitmapMap.get(path);
    }
}