package com.duongame.basicplayer.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.duongame.basicplayer.data.MovieFile;
import com.duongame.basicplayer.adapter.MovieAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by js296 on 2017-06-06.
 */

public class FindFileTask extends AsyncTask<Void, Integer, Void> {
    private final static String TAG = FindFileTask.class.getSimpleName();

    private MovieAdapter movieAdapter;
    private ArrayList<MovieFile> movieFileArrayList;
    private File path;
    private String[] ext;

    public FindFileTask(Context context, MovieAdapter movieAdapter, File path, String[] ext) {
        this.movieAdapter = movieAdapter;

        this.path = path;
        this.ext = ext;
        this.movieFileArrayList = new ArrayList<>();
    }

    @Override
    protected Void doInBackground(Void... params) {
        findFiles(path, ext, movieFileArrayList);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if(movieAdapter != null) {
            movieAdapter.setMovieList(movieFileArrayList);
            movieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        super.onProgressUpdate(params);

        if(movieAdapter != null) {
            ArrayList<MovieFile> arrayList = (ArrayList<MovieFile> )movieFileArrayList.clone();
            movieAdapter.setMovieList(arrayList);
            movieAdapter.notifyDataSetChanged();
        }
    }

    private void findFiles(File path, String[] ext, ArrayList<MovieFile> result) {
        final File[] files = path.listFiles();
        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {
            final File each = files[i];
            final String name = each.getName();

            // 기본 폴더일 경우 패스
            if (name.equals(".") || name.equals("..")) {
                continue;
            }

            // 숨김 폴더일 경우 패스
            if(name.startsWith(".")) {
                continue;
            }

            //Log.e(TAG, "Each="+each.getAbsolutePath());

            // 시스템 폴더일 경우 패스
            // SDCARD일 경우 생각해야 함
            if (each.getAbsolutePath().startsWith("/Android/data/")) {
                Log.d(TAG, "System Folder " + each.getAbsolutePath());
                continue;
            }

            if (each.isDirectory()) {
                findFiles(each, ext, result);
            } else {
                for (int j = 0; j < ext.length; j++) {
                    // 확장자가 맞으면
                    if (each.getName().endsWith(ext[j])) {
                        MovieFile movieFile = new MovieFile(each, "");

                        Log.e(TAG, "FOUND MOVIE FILE "+each);
                        result.add(movieFile);
                        publishProgress(null);
                        break;
                    }
                }
            }
        }
    }
}
