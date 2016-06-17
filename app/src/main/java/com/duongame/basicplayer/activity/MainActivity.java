package com.duongame.basicplayer.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.duongame.basicplayer.manager.PermissionManager;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private ListView mListMovie;
    private MovieAdapter mMovieAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        initView();




        // 런타임 퍼미션 체크
        PermissionManager.checkStoragePermissions(this, true, false);

    }

    private void initView() {
        // 광고들 초기화
        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        // 루트 레이아웃을 얻어서
        mSwipeLayout = (SwipeRefreshLayout)getLayoutInflater().inflate(R.layout.activity_main, null);
        final RelativeLayout relativeLayout = (RelativeLayout)mSwipeLayout.findViewById(R.id.relative);
        mMovieAdapter = new MovieAdapter();

        // AdView 생성
        final AdView adView = AdBannerManager.getAdBannerView();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        adView.setLayoutParams(params);
        adView.setId(1);// 아이디를 꼭 생성해 주어야 한다
        relativeLayout.addView(adView, 0);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ABOVE, adView.getId());

        // 파일리스트뷰를 불러오자
        mListMovie = (ListView)relativeLayout.findViewById(R.id.listMovie);
        mListMovie.setLayoutParams(params);

        setContentView(mSwipeLayout);

        mListMovie.setAdapter(mMovieAdapter);
        // 아이템을 클릭하면 오픈하자
        mListMovie.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final File file = (File) mMovieAdapter.getItem(position);

                final Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("filename", file.getAbsolutePath());
                startActivity(intent);
            }
        });
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
        }
    }

    private void findFiles(File path, String[] ext, ArrayList<File> result) {
        File[] files = path.listFiles();

        for (int i = 0; i < files.length; i++) {
            File each = files[i];
            String name = each.getName();
            if (name.equals(".") || name.equals("..")) {
                continue;
            }

            if (each.isDirectory()) {
                findFiles(each, ext, result);
            }

            for (int j = 0; j < ext.length; j++) {
                // 확장자가 맞으면
                if (each.getName().endsWith(ext[j])) {
                    result.add(each);
                    break;
                }
            }
        }
    }

    private void refreshList() {
        // 동적으로 refeshList를 호출한다
        new Thread(new Runnable() {
            @Override
            public void run() {

                // 확장자 별로 파일을 찾자
                String[] ext = {".avi", ".mp4", ".mov", ".mkv", ".wmv", ".asf", ".flv"};

                // 찾는 위치는 external root에서부터 찾는다
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();

                // 썸네일도 등록해야 되는데 일단 파일 이름만
                ArrayList<File> files = new ArrayList<File>();

                findFiles(new File(root), ext, files);

                // 썸네일은 첫번째 이미지 프레임을 가지고 온다
                mMovieAdapter.setMovieList(files);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMovieAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    public class MovieAdapter extends BaseAdapter {

        ArrayList<File> movieList;

        // 동적으로 파일을 찾자
        public MovieAdapter() {
            super();
            refreshList();
        }

        public void setMovieList(ArrayList<File> movieList) {
            this.movieList = movieList;
        }

        @Override
        public int getCount() {
            if (movieList != null)
                return movieList.size();
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (movieList != null)
                return movieList.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.list_item, parent, false);
            TextView tv = (TextView) layout.findViewById(R.id.textMovie);

//            TextView tv = new TextView(MainActivity.this);
            tv.setText(movieList.get(position).getName());
            return layout;
        }
    }
}
