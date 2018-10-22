package com.duongame.basicplayer.task;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.duongame.basicplayer.adapter.MovieAdapter;
import com.duongame.basicplayer.data.MovieFile;

import java.io.File;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by js296 on 2017-06-06.
 */

public class FindFileTask extends AsyncTask<Void, Integer, Void> {
    private final static String TAG = FindFileTask.class.getSimpleName();
    private final static String cameraPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Camera";
    private final static String downloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download";
    private final static String videoPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video";

    private MovieAdapter movieAdapter;
    private ArrayList<MovieFile> movieFileList;
    private File path;
    private String[] ext;
    private Realm realm;

    private ArrayList<String> preloadFolderList;

    public FindFileTask(Realm realm, MovieAdapter movieAdapter, File path, String[] ext) {
        this.realm = realm;
        this.movieAdapter = movieAdapter;

        this.path = path;
        this.ext = ext;

        movieFileList = new ArrayList<>();
        preloadFolderList = new ArrayList<>();
        preloadFolderList.add(cameraPath);
        preloadFolderList.add(downloadPath);
        preloadFolderList.add(videoPath);
    }

    @Override
    protected Void doInBackground(Void... params) {
        for(String folder : preloadFolderList) {
            findFiles(new File(folder), ext, movieFileList, true);
        }
        findFiles(path, ext, movieFileList, false);
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        if (movieAdapter != null) {
            movieAdapter.setMovieList(movieFileList);
            movieAdapter.notifyDataSetChanged();
            Log.e(TAG, "onPostExecute notifyDataSetChanged");
            // 모든 MovieFile을 지운 다음에
            // add를 하자
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(MovieFile.class);

                    for (MovieFile movie : movieFileList) {
                        realm.copyToRealmOrUpdate(movie);
                    }
                }
            });
        }
    }

    @Override
    protected void onProgressUpdate(Integer... params) {
        super.onProgressUpdate(params);

        if (movieAdapter != null) {
            ArrayList<MovieFile> arrayList = (ArrayList<MovieFile>) movieFileList.clone();
            movieAdapter.setMovieList(arrayList);
            movieAdapter.notifyDataSetChanged();
            Log.e(TAG, "onProgressUpdate notifyDataSetChanged");
        }
    }

    private void findFiles(File path, String[] ext, ArrayList<MovieFile> result, boolean isPreload) {
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
            if (name.startsWith(".")) {
                continue;
            }

            // 다운로드 모드가 아닐경우 download 패스는 패스한다.
            if (!isPreload && preloadFolderList.contains(each.getAbsolutePath())) {
                continue;
            }

            // 시스템 폴더일 경우 패스
            // SDCARD일 경우 생각해야 함
            if (each.getAbsolutePath().startsWith("/Android/data/")) {
                Log.d(TAG, "System Folder " + each.getAbsolutePath());
                continue;
            }

            if (each.isDirectory()) {
                findFiles(each, ext, result, isPreload);
            } else {
                for (int j = 0; j < ext.length; j++) {
                    // 확장자가 맞으면
                    if (each.getName().endsWith(ext[j])) {
                        MovieFile movieFile = new MovieFile(each, "");
                        result.add(movieFile);

                        // 파일이 들어오는 족족 화면에 업데이트 한다.
                        publishProgress(null);
                        break;
                    }
                }
            }
        }
    }
}
