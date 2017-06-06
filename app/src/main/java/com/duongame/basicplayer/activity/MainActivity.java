package com.duongame.basicplayer.activity;

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
import com.duongame.basicplayer.manager.ShortcutManager;
import com.duongame.basicplayer.task.FindFileTask;
import com.google.android.gms.ads.AdView;

import java.io.File;

public class MainActivity extends BaseActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int ADVIEW_ID = 1;
    private final static String[] mMovieExt = {".avi", ".mp4", ".mov", ".mkv", ".wmv", ".asf", ".flv"};

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

        initView();

        // 런타임 퍼미션 체크
        if (PermissionManager.checkStoragePermissions(this, true, false)) {
            initAdapter();
        }

        ShortcutManager.checkShortcut(this);

        // 그림자를 없앤다.
        getSupportActionBar().setElevation(0);

        FileManager.checkRecentFile(this);
    }

    private void initAdapter() {
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        mExtRoot = Environment.getExternalStorageDirectory().getAbsolutePath();

        FindFileTask task = new FindFileTask(this, mMovieAdapter, new File(mExtRoot), mMovieExt);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
}
