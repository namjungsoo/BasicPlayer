package com.duongame.basicplayer.task;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.data.MovieFile;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.view.ThumbnailImageView;

import java.lang.ref.WeakReference;

import io.realm.Realm;

/**
 * Created by js296 on 2017-06-06.
 */

public class LoadThumbnailTask extends AsyncTask<Void, Integer, Boolean> {
    private final static String TAG = LoadThumbnailTask.class.getSimpleName();

    private MovieFile movieFile;
    private WeakReference<ThumbnailImageView> imageViewReference;
    private WeakReference<Context> contextWeakReference;
    private int kind;
    private static int MINI_KIND_WIDTH = 512;
    private static int MINI_KIND_HEIGHT = 384;
    private Realm realm;

    public LoadThumbnailTask(Context context, int kind, MovieFile movieFile, ThumbnailImageView imageView, Realm realm) {
        this.kind = kind;
        this.movieFile = movieFile;
        this.imageViewReference = new WeakReference<>(imageView);
        this.realm = realm;
        this.contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return loadThumbnail(contextWeakReference.get(), movieFile, kind);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            try {
                if (imageViewReference.get().getTag().equals(movieFile.path)) {
                    Bitmap bitmap = ThumbnailManager.getBitmap(kind, movieFile.path);
                    if (bitmap != null) {
                        imageViewReference.get().setImageBitmap(bitmap);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 현재 사용안함
    private boolean loadThumbnailByPlayer(MovieFile movieFile) {
        // 썸네일에 등록하자
        final Player player = new Player();
        player.init();

        // FFmpeg NDK 라이브러리에서 로딩한다.
        // audio 사용여부는 false이다.
        int ret = player.openMovieWithAudio(movieFile.absolutePath, 0, 512, 384);
        //int ret = Player.openMovie(each.getAbsolutePath());

        Log.d(TAG, "loadThumbnailByPlayer openMovieWithAudio filename=" + movieFile.absolutePath + " ret=" + ret);

        // 파일 열기가 성공했으면 렌더링 한다.
        if (ret >= 0) {
//            final int width = player.getMovieWidth();
//            final int height = player.getMovieHeight();
            final int width = 512;
            final int height = 384;

            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            player.renderFrame(bitmap);
            ThumbnailManager.addBitmap(kind, movieFile.path, bitmap);
            player.closeMovie();

            SaveThumbnailTask task = new SaveThumbnailTask(contextWeakReference.get(), realm, movieFile, bitmap);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            Log.d(TAG, "loadThumbnailByPlayer SaveThumbnailTask");
            return true;
        } else {
            return false;
        }
    }

    private boolean loadThumbnail(Context context, MovieFile movieFile, int kind) {
        // 새로운 방식
        // 시스템에 있는 것을 가져온다.
        // 없으면 새로 생성해야 한다.
        Log.e(TAG, "loadThumbnail name=" + movieFile.name + " thumbnail=" + movieFile.thumbnail);

        if (!TextUtils.isEmpty(movieFile.thumbnail)) {
            Log.e(TAG, "loadThumbnail thumbnail=" + movieFile.thumbnail);
            Bitmap bitmap = BitmapFactory.decodeFile(movieFile.thumbnail);
            if (bitmap != null) {
                Log.e(TAG, "loadThumbnail thumbnail load ok");
                ThumbnailManager.addBitmap(kind, movieFile.path, bitmap);
                return true;
            }
        }

        String[] proj = {
                MediaStore.Video.VideoColumns._ID,
        };

        String fileName = movieFile.path;
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                proj,
                MediaStore.Video.VideoColumns.DATA + "=?",
                new String[]{fileName},
                null);

        cursor.moveToFirst();
        if (cursor.getCount() == 0) {
            cursor.close();
            Log.e(TAG, "loadThumbnail false path=" + movieFile.path);

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(movieFile.path, kind);
            if (thumb != null) {
                ThumbnailManager.addBitmap(kind, movieFile.path, thumb);

                SaveThumbnailTask task = new SaveThumbnailTask(contextWeakReference.get(), realm, movieFile, thumb);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            } else {
                return loadThumbnailByPlayer(movieFile);
            }
        }

        long long_fileID = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID));
        Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(context.getContentResolver(), long_fileID, kind, null);
        ThumbnailManager.addBitmap(kind, movieFile.path, bitmap);
        Log.e(TAG, "loadThumbnail true path=" + movieFile.path);
        cursor.close();

        return true;
    }
}
