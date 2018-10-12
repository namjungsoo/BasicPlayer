package com.duongame.basicplayer.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.duongame.basicplayer.BuildConfig;
import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.adapter.MovieAdapter;
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.duongame.basicplayer.manager.FileManager;
import com.duongame.basicplayer.manager.PermissionManager;
import com.duongame.basicplayer.task.FindFileTask;
import com.google.android.gms.ads.AdView;

import java.io.File;

import static com.duongame.basicplayer.manager.AdInterstitialManager.MODE_EXIT;

public class MainActivity extends BaseActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int ADVIEW_ID = 1;
    private final static String[] movieExt = {".avi", ".mp4", ".mov", ".mkv", ".wmv", ".asf", ".flv"};

    private SwipeRefreshLayout swipeLayout;
    //private String extRoot;

    private RecyclerView recyclerView;
    //private RecyclerView.LayoutManager layoutManager;

    //private MovieAdapter movieAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        Player.initAudioTrack();
        initView();

        // 런타임 퍼미션 체크
        if (PermissionManager.checkStoragePermissions(this, true, false)) {
            initAdapter();
        }

        FileManager.checkRecentFile(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initAdapter() {
        MovieAdapter movieAdapter = new MovieAdapter(this);
        recyclerView.setAdapter(movieAdapter);
        String extRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

        FindFileTask task = new FindFileTask(this, movieAdapter, new File(extRoot), movieExt);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initListView(RelativeLayout relativeLayout) {
        recyclerView = (RecyclerView) relativeLayout.findViewById(R.id.listMovie);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        recyclerView.setLayoutParams(params);
    }

    private void initView() {
        // 광고들 초기화
        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        // 루트 레이아웃을 얻어서
        View root = getLayoutInflater().inflate(R.layout.activity_main, null);
        swipeLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe);

        final RelativeLayout relativeLayout = (RelativeLayout) swipeLayout.findViewById(R.id.relative);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
            }
        });


        // 파일리스트뷰를 불러오자
        initListView(relativeLayout);

        // 하단 배너 광고를 추가해 주자
        //PRO
        if (BuildConfig.SHOW_AD)
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

    @Override
    public void onBackPressed() {
        SharedPreferences pref = getSharedPreferences("main", MODE_PRIVATE);
        int count = pref.getInt("exit_count", 0);
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("exit_count", count + 1);
        edit.apply();

        if (count % 2 == 0) {
            AdInterstitialManager.showAd(this, MODE_EXIT, new AdInterstitialManager.OnFinishListener() {
                @Override
                public void onFinish() {
                    finish();
                }
            });
        } else {
            //finish();
            super.onBackPressed();
        }
    }
}
