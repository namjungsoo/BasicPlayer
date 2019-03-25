package com.duongame.basicplayer.task;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.duongame.basicplayer.data.MovieFile;
import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.manager.TimeTextManager;
import com.duongame.basicplayer.util.TimeConverter;

import java.lang.ref.WeakReference;

/**
 * Created by js296 on 2017-06-06.
 */

public class LoadThumbnailTask extends AsyncTask<Void, Integer, Boolean> {
    private final static String TAG = LoadThumbnailTask.class.getSimpleName();

    private MovieFile movieFile;
    private WeakReference<ImageView> imageViewRef;
    private WeakReference<Context> contextWeakReference;
    private int kind;

    public LoadThumbnailTask(Context context, int kind, MovieFile movieFile, ImageView imageView) {
        this.kind = kind;
        this.movieFile = movieFile;
        this.imageViewRef = new WeakReference<>(imageView);
        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return loadThumbnail(contextWeakReference.get(), movieFile, kind);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            try {
                if (imageViewRef.get().getTag().equals(movieFile.path)) {
                    Bitmap bitmap = ThumbnailManager.getBitmap(kind, movieFile.path);
                    if (bitmap != null) {
                        imageViewRef.get().setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 현재 사용안함
//    private boolean loadThumbnailByPlayer(MovieFile movieFile) {
//        // 썸네일에 등록하자
//        final Player player = new Player();
//        player.init();
//
//        // FFmpeg NDK 라이브러리에서 로딩한다.
//        int ret = player.openMovieWithAudio(movieFile.absolutePath, 0);
//        //int ret = Player.openMovie(each.getAbsolutePath());
//
//        Log.d(TAG, "openMovieWithAudio filename=" + movieFile.absolutePath + " ret=" + ret);
//
//        // 파일 열기가 성공했으면 렌더링 한다.
//        if (ret >= 0) {
//            final int width = player.getMovieWidth();
//            final int height = player.getMovieHeight();
//
//            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            player.renderFrame(bitmap);
//
//            ThumbnailManager.addBitmap(MediaStore.Video.Thumbnails.MINI_KIND, movieFile.path, bitmap);
//
//            String timeText = TimeConverter.convertUsToString(player.getMovieDurationUs());
//            TimeTextManager.addTimeText(movieFile.path, timeText);
//
//            player.closeMovie();
//            return true;
//        } else {
//            return false;
//        }
//    }

    public static boolean loadThumbnail(Context context, MovieFile movieFile, int kind) {
        // 새로운 방식
        // 시스템에 있는 것을 가져온다.
        // 없으면 새로 생성해야 한다.
        String[] proj = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATA
        };

        String fileName = movieFile.path;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                proj,
                MediaStore.Video.Media.DISPLAY_NAME + "=?",
                new String[]{fileName},
                null);

        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            return false;
        }

        long long_fileID = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), long_fileID, MediaStore.Video.Thumbnails.MICRO_KIND, null);
        ThumbnailManager.addBitmap(kind, movieFile.path, bitmap);
        cursor.close();
        return true;

        // 이전에 사용하던 매번 Utils로 생성하던 방식
//        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(movieFile.path, kind);
//        ThumbnailManager.addBitmap(kind, movieFile.path, thumb);
//        return true;
//        //return loadThumbnailByPlayer(movieFile);
    }
}
