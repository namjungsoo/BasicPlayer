package com.duongame.basicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duongame.basicplayer.R;
import com.duongame.basicplayer.data.MovieFile;
import com.duongame.basicplayer.manager.FileManager;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.task.LoadThumbnailTask;
import com.duongame.basicplayer.task.LoadTimeTextTask;
import com.duongame.basicplayer.view.ThumbnailImageView;

import java.util.List;

import io.realm.Realm;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

/**
 * Created by js296 on 2017-06-06.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final static String TAG = MovieAdapter.class.getSimpleName();

    private List<MovieFile> movieList;
    private Context context;
    private final int kind = MINI_KIND;
    private Realm realm;

    public MovieAdapter(Context context, Realm realm) {
        this.context = context;
        this.realm = realm;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ThumbnailImageView iv;
        TextView tvName;
        TextView tvPath;

        ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public List<MovieFile> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<MovieFile> movieList) {
        this.movieList = movieList;
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = layoutInflater.inflate(R.layout.list_item, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        holder.iv = v.findViewById(R.id.thumbnail);
        holder.tvName = v.findViewById(R.id.textName);
        holder.tvPath = v.findViewById(R.id.textPath);

        return holder;
    }

    @Override
    public void onBindViewHolder(MovieAdapter.ViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder " + position);
        final MovieFile file = movieList.get(position);

        // 1. 메모리 캐시에 있으면
        // 2. 파일로 저장된 썸네일이 있으면 그것을 로딩
        // 3. 파일에 없으면 안드로이드 시스템에서 로딩
        // 4. 백그라운드로 NDK 라이브러리로 고화질 이미지를 로딩하여 캐쉬 및 파일로 저장

        //mini only
        Bitmap bitmap = ThumbnailManager.getBitmap(kind, file.path);
        if (bitmap != null) {
            holder.iv.setImageBitmap(bitmap);
        } else {
            // getBitmap으로 확인시는 아직 로딩이 안되었지만 여러번 연속으로 호출될수가 있다.
            // 그러니 기다리자.
            if (!file.isLoadingThumbnail) {
                Log.d(TAG, "isLoadingThumbnail true " + file.path);
                // 비트맵을 읽어들인 후에 설정하자
                holder.iv.setTag(file.path);

                LoadThumbnailTask task = new LoadThumbnailTask(context, kind, file, holder.iv);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                file.isLoadingThumbnail = true;
            }
        }

        // 파일 시간을 셋팅하자
        //String timeText = TimeTextManager.getTimeText(file.path);
        if(!TextUtils.isEmpty(file.timeText)) {
            holder.iv.setTimeText(file.timeText);
            Log.d(TAG, "isLoadingTimeText already have " + file.path);
        } else {
            if(!file.isLoadingTimeText) {
                Log.d(TAG, "isLoadingTimeText true " + file.path);
                LoadTimeTextTask task = new LoadTimeTextTask(realm, file, holder.iv);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                file.isLoadingTimeText = true;
            }
        }

        String name = file.name;
        name = name.substring(0, name.lastIndexOf("."));

        holder.tvName.setText(name);
        holder.tvPath.setText(file.parent);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 무조건 열지 말고 기존에 읽었던 파일인지 테스트
                if (!FileManager.checkRecentFile(context, file.absolutePath)) {
                    FileManager.openFile(context, file.absolutePath, 0L, 0);
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
