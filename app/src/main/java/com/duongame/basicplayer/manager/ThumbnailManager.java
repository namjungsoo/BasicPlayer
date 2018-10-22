package com.duongame.basicplayer.manager;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import java.util.HashMap;

/**
 * Created by namjungsoo on 16. 6. 19..
 */
public class ThumbnailManager {

    // 파일을 png로 저장해야함
    // 근데 로컬에 저장해야함
    // /를 전부 _로 변경후 로컬에 저장
    //private static HashMap<String, String> cacheFileMap;
    private static HashMap<String, Bitmap> miniCacheBitmapMap = new HashMap<>();
    private static HashMap<String, Bitmap> microCacheBitmapMap = new HashMap<>();

    public static void addBitmap(int kind, String path, Bitmap bitmap) {
        if (kind == MediaStore.Video.Thumbnails.MINI_KIND)
            miniCacheBitmapMap.put(path, bitmap);
        else if (kind == MediaStore.Video.Thumbnails.MICRO_KIND)
            microCacheBitmapMap.put(path, bitmap);
    }

    public static void clear() {
        miniCacheBitmapMap.clear();
        microCacheBitmapMap.clear();
    }

    public static Bitmap getBitmap(int kind, String path) {
        if (kind == MediaStore.Video.Thumbnails.MINI_KIND)
            return miniCacheBitmapMap.get(path);
        else if (kind == MediaStore.Video.Thumbnails.MICRO_KIND)
            return microCacheBitmapMap.get(path);
        return null;
    }
}
