package com.duongame.basicplayer.task;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.duongame.basicplayer.MovieFile;
import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.util.TimeConverter;

/**
 * Created by js296 on 2017-06-06.
 */

public class LoadThumbnailTask extends AsyncTask<Void, Integer, Boolean> {
    private final static String TAG = LoadThumbnailTask.class.getSimpleName();

    private MovieFile movieFile;
    private ImageView imageView;

    public LoadThumbnailTask(MovieFile movieFile, ImageView imageView) {
        this.movieFile = movieFile;
        this.imageView = imageView;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return loadThumbnail(movieFile);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result.booleanValue()) {
            Bitmap bitmap = ThumbnailManager.getBitmap(movieFile.file.getPath());
            if (bitmap != null)
                imageView.setImageBitmap(bitmap);
        } else {

        }
    }

    private boolean loadThumbnail(MovieFile movieFile) {
        // 썸네일에 등록하자
        final Player player = new Player();
        player.init();

        int ret = player.openMovieWithAudio(movieFile.file.getAbsolutePath(), 0);
        //int ret = Player.openMovie(each.getAbsolutePath());

        Log.d(TAG, "openMovieWithAudio filename=" + movieFile.file.getAbsolutePath() + " ret=" + ret);
        if (ret >= 0) {
            final int width = player.getMovieWidth();
            final int height = player.getMovieHeight();

            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            player.renderFrame(bitmap);

            ThumbnailManager.addBitmap(movieFile.file.getPath(), bitmap);

            movieFile.timeText = TimeConverter.convertUsToString(player.getMovieDurationUs());
            player.closeMovie();
            return true;
        } else {
            return false;
        }

    }

}
