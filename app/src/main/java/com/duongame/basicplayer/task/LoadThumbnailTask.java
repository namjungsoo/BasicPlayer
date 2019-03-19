package com.duongame.basicplayer.task;

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
    private int kind;

    public LoadThumbnailTask(int kind, MovieFile movieFile, ImageView imageView) {
        this.kind = kind;
        this.movieFile = movieFile;
        this.imageViewRef = new WeakReference<>(imageView);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return loadThumbnail(movieFile, kind);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            try {
                if(imageViewRef.get().getTag().equals(movieFile.path)) {
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

    private boolean loadThumbnailByPlayer(MovieFile movieFile) {
        // 썸네일에 등록하자
        final Player player = new Player();
        player.init();

        // FFmpeg NDK 라이브러리에서 로딩한다.
        int ret = player.openMovieWithAudio(movieFile.absolutePath, 0);
        //int ret = Player.openMovie(each.getAbsolutePath());

        Log.d(TAG, "openMovieWithAudio filename=" + movieFile.absolutePath + " ret=" + ret);

        // 파일 열기가 성공했으면 렌더링 한다.
        if (ret >= 0) {
            final int width = player.getMovieWidth();
            final int height = player.getMovieHeight();

            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            player.renderFrame(bitmap);

            ThumbnailManager.addBitmap(MediaStore.Video.Thumbnails.MINI_KIND, movieFile.path, bitmap);

            String timeText = TimeConverter.convertUsToString(player.getMovieDurationUs());
            TimeTextManager.addTimeText(movieFile.path, timeText);

            player.closeMovie();
            return true;
        } else {
            return false;
        }
    }

    public static boolean loadThumbnail(MovieFile movieFile, int kind) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(movieFile.path, kind);
        ThumbnailManager.addBitmap(kind, movieFile.path, thumb);
        return true;
        //return loadThumbnailByPlayer(movieFile);
    }
}
