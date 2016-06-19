package com.duongame.basicplayer.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.duongame.basicplayer.Player;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.AdBannerManager;
import com.duongame.basicplayer.manager.AdInterstitialManager;
import com.duongame.basicplayer.manager.PermissionManager;
import com.duongame.basicplayer.manager.ShortcutManager;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private ListView mListMovie;
    private MovieAdapter mMovieAdapter;
    private SwipeRefreshLayout mSwipeLayout;
    private String mExtRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);
        initView();

        // 런타임 퍼미션 체크
        if (PermissionManager.checkStoragePermissions(this, true, false)) {
            initAdapter();
        }

        ShortcutManager.checkShortcut(this);

        // 그림자를 없앤다.
        getSupportActionBar().setElevation(0);

        Player.init(this);
    }

    private void initToolbar() {
        // 툴바 세팅
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        // 폰트 세팅
        Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
        TextView toolbarTitle = null;
        for (int i = 0; i < toolbar.getChildCount(); ++i) {
            View child = toolbar.getChildAt(i);

            // assuming that the title is the first instance of TextView
            // you can also check if the title string matches
            if (child instanceof TextView) {
                toolbarTitle = (TextView)child;
                break;
            }
        }
        toolbarTitle.setTypeface(tf);
    }

    private TextView getActionBarTitle() {
        View v = getWindow().getDecorView();
        int resId = getResources().getIdentifier("action_bar_title", "id", "android");
        return (TextView)v.findViewById(resId);
    }

    private void initAdapter() {
        mMovieAdapter = new MovieAdapter();
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

    private void initView() {
        // 광고들 초기화
        AdBannerManager.init(this);
        AdInterstitialManager.init(this);

        // 루트 레이아웃을 얻어서
        View root = getLayoutInflater().inflate(R.layout.activity_main, null);
        mSwipeLayout = (SwipeRefreshLayout)root.findViewById(R.id.swipe);
//        mSwipeLayout = (SwipeRefreshLayout) getLayoutInflater().inflate(R.layout.activity_main, null);

        final RelativeLayout relativeLayout = (RelativeLayout) mSwipeLayout.findViewById(R.id.relative);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeLayout.setRefreshing(false);
            }
        });

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
        mListMovie = (ListView) relativeLayout.findViewById(R.id.listMovie);
        mListMovie.setLayoutParams(params);
        mListMovie.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                final View child = mListMovie.getChildAt(0);
                if (child != null) {
                    int scrollY = -child.getTop();
                    if (scrollY == 0) {
                        mSwipeLayout.setEnabled(true);
                        return;
                    }
                }

                mSwipeLayout.setEnabled(false);
            }
        });

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

    private void findFiles(File path, String[] ext, ArrayList<File> result) {
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
            if(each.getAbsolutePath().startsWith(mExtRoot+"/Android/data/")) {
                Log.d(TAG, "System Folder "+each.getAbsolutePath());
                continue;
            }

            if (each.isDirectory()) {
                findFiles(each, ext, result);
            }

            for (int j = 0; j < ext.length; j++) {
                // 확장자가 맞으면
                if (each.getName().endsWith(ext[j])) {
                    result.add(each);

                    // 썸네일에 등록하자
                    if(Player.openMovieWithAudio(each.getAbsolutePath(), 0) >= 0) {
                        final int width = Player.getMovieWidth();
                        final int height = Player.getMovieHeight();

                        final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Player.renderFrame(bitmap);

                        ThumbnailManager.addBitmap(each.getPath(), bitmap);
                        Player.closeMovie();
                    }

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
                final String[] ext = {".avi", ".mp4", ".mov", ".mkv", ".wmv", ".asf", ".flv"};

                // 찾는 위치는 external root에서부터 찾는다
                final String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                mExtRoot = root;

                // 썸네일도 등록해야 되는데 일단 파일 이름만
                final ArrayList<File> files = new ArrayList<File>();

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
        private ArrayList<File> movieList;

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

        class ViewHolder {
            public ImageView iv;
            public TextView tvName;
            public TextView tvPath;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //TODO: ViewHolder
            ViewHolder viewHolder;

            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.iv = (ImageView) convertView.findViewById(R.id.thumbnail);
                viewHolder.tvName = (TextView) convertView.findViewById(R.id.textName);
                viewHolder.tvPath = (TextView) convertView.findViewById(R.id.textPath);

                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            viewHolder.iv.setImageBitmap(ThumbnailManager.getBitmap(movieList.get(position).getPath()));
            viewHolder.tvName.setText(movieList.get(position).getName());
            viewHolder.tvPath.setText(movieList.get(position).getParent());

            return convertView;
        }
    }
}
