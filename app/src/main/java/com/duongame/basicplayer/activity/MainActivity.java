package com.duongame.basicplayer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.duongame.basicplayer.manager.AlertManager;
import com.duongame.basicplayer.manager.PermissionManager;
import com.duongame.basicplayer.manager.PreferenceManager;
import com.duongame.basicplayer.manager.ShortcutManager;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.util.TimeConverter;
import com.duongame.basicplayer.view.ThumbnailImageView;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;

//import com.crashlytics.android.Crashlytics;

//import io.fabric.sdk.android.Fabric;

public class MainActivity extends BaseActivity {
    private final static String TAG = "MainActivity";
    private final static int ADVIEW_ID = 1;

    private SwipeRefreshLayout mSwipeLayout;
    private String mExtRoot;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    private MovieAdapter mMovieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        Player.init(this);

//        Fabric.with(this, new Crashlytics());

        //setContentView(R.layout.activity_main);
        initView();

        // 런타임 퍼미션 체크
        if (PermissionManager.checkStoragePermissions(this, true, false)) {
            initAdapter();
        }

        ShortcutManager.checkShortcut(this);

        // 그림자를 없앤다.
        getSupportActionBar().setElevation(0);

        checkRecentFile();
    }

    private boolean checkRecentFile(final String newFilename) {
        final String filename = PreferenceManager.getRecentFilename(this);
        final long time = PreferenceManager.getRecentTime(this);
        final int rotation = PreferenceManager.getRecentRotation(this);

        if(newFilename.equals(filename)) {
            AlertManager.showAlertRecentFile(this, filename, time, rotation, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 처음부터 읽자
                    openFile(newFilename, 0L, 0);
                }
            });
            return true;
        }
        return false;
    }

    private boolean checkRecentFile() {
        final String filename = PreferenceManager.getRecentFilename(this);
        final long time = PreferenceManager.getRecentTime(this);
        final int rotation = PreferenceManager.getRecentRotation(this);

        if (filename.length() > 0) {
            // 확인해보고 열자
//            openFile(filename, time);
            AlertManager.showAlertRecentFile(this, filename, time, rotation, null);
            return true;
        }
        return false;
    }

    private void initAdapter() {
        mMovieAdapter = new MovieAdapter();
        mRecyclerView.setAdapter(mMovieAdapter);
    }

    private void initListView(RelativeLayout relativeLayout) {
        mRecyclerView = (RecyclerView) relativeLayout.findViewById(R.id.listMovie);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    private void initAd(RelativeLayout relativeLayout) {
        // AdView 생성
        final AdView adView = AdBannerManager.getAdBottomBannerView();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(params);
        adView.setId(R.id.admob);// 아이디를 꼭 생성해 주어야 한다
        relativeLayout.addView(adView, 0);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, adView.getId());
        mRecyclerView.setLayoutParams(params);
    }

    private void initView() {
        // 광고들 초기화
        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        // 루트 레이아웃을 얻어서
        View root = getLayoutInflater().inflate(R.layout.activity_main, null);
        mSwipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe);
//        mSwipeLayout = (SwipeRefreshLayout) getLayoutInflater().inflate(R.layout.activity_main, null);

        final RelativeLayout relativeLayout = (RelativeLayout) mSwipeLayout.findViewById(R.id.relative);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(false);
            }
        });


        // 파일리스트뷰를 불러오자
        initListView(relativeLayout);

        // 하단 배너 광고를 추가해 주자
        //PRO
        initAd(relativeLayout);

        setContentView(root);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            Log.d(TAG, "onRequestPermissionsResult");
            initAdapter();
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

            // 시스템 폴더일 경우 패스
            if (each.getAbsolutePath().startsWith(mExtRoot + "/Android/data/")) {
                Log.d(TAG, "System Folder " + each.getAbsolutePath());
                continue;
            }

            if (each.isDirectory()) {
                findFiles(each, ext, result);
            }

            for (int j = 0; j < ext.length; j++) {
                // 확장자가 맞으면
                if (each.getName().endsWith(ext[j])) {
                    MovieFile movieFile = new MovieFile(each, "");

                    // 썸네일에 등록하자
                    int ret = Player.openMovieWithAudio(each.getAbsolutePath(), 0);
                    //int ret = Player.openMovie(each.getAbsolutePath());

                    Log.d(TAG, "openMovieWithAudio filename=" + each.getAbsolutePath() + " ret=" + ret);
                    if (ret >= 0) {
                        final int width = Player.getMovieWidth();
                        final int height = Player.getMovieHeight();

                        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Player.renderFrame(bitmap);

                        ThumbnailManager.addBitmap(each.getPath(), bitmap);

                        movieFile.timeText = TimeConverter.convertUsToString(Player.getMovieDurationUs());
                        Player.closeMovie();
                    } else {

                    }

                    result.add(movieFile);
                    break;
                }
            }
        }
    }

    private void refreshList(ArrayList<MovieFile> files) {
        // 동적으로 refeshList를 호출한다
//        new Thread(new Runnable() {
//            @Override
//            public void run() {

        // 확장자 별로 파일을 찾자
        final String[] ext = {".avi", ".mp4", ".mov", ".mkv", ".wmv", ".asf", ".flv"};

        // 찾는 위치는 external root에서부터 찾는다
        final String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        mExtRoot = root;

        // 썸네일도 등록해야 되는데 일단 파일 이름만
//                final ArrayList<MovieFile> files = new ArrayList<MovieFile>();

        findFiles(new File(root), ext, files);

        // 썸네일은 첫번째 이미지 프레임을 가지고 온다
//                mMovieAdapter.setMovieList(files);

//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mMovieAdapter.notifyDataSetChanged();
//                    }
//                });
//            }
//        }).start();
    }

    public class MovieFile {
        public File file;
        public String timeText;

        public MovieFile(File f, String t) {
            file = f;
            timeText = t;
        }
    }

    public void openFile(String filename, long time, int rotation) {
        final Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("time", time);
        intent.putExtra("rotation", rotation);
        startActivity(intent);
    }

    //RECYCLERVIEW
    public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
        private ArrayList<MovieFile> movieList = new ArrayList<MovieFile>();

        public MovieAdapter() {
            refreshList(movieList);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ThumbnailImageView iv;
            public TextView tvName;
            public TextView tvPath;

            public ViewHolder(View itemView) {
                super(itemView);
            }
        }

        public void setMovieList(ArrayList<MovieFile> movieList) {
            this.movieList = movieList;
        }

        @Override
        public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View v = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            final ViewHolder holder = new ViewHolder(v);

            holder.iv = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
            holder.tvName = (TextView) v.findViewById(R.id.textName);
            holder.tvPath = (TextView) v.findViewById(R.id.textPath);

            return holder;
        }

        @Override
        public void onBindViewHolder(MovieAdapter.ViewHolder holder, int position) {
            final File file = movieList.get(position).file;

            Log.v(TAG, file.getPath());
            Bitmap bitmap = ThumbnailManager.getBitmap(file.getPath());

            if (bitmap != null) {
                holder.iv.setImageBitmap(bitmap);
            }
            holder.iv.setTimeText(movieList.get(position).timeText);

            String name = file.getName();
            name = name.substring(0, name.lastIndexOf("."));

            holder.tvName.setText(name);
            holder.tvPath.setText(file.getParent());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // 무조건 열지 말고 기존에 읽었던 파일인지 테스트
                    if(!checkRecentFile(file.getAbsolutePath())) {
                        openFile(file.getAbsolutePath(), 0L, 0);
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            if (movieList != null)
                return movieList.size();
            return 0;
        }
    }
}
