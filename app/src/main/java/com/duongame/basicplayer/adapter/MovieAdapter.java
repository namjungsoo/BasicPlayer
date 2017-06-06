package com.duongame.basicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.duongame.basicplayer.MovieFile;
import com.duongame.basicplayer.R;
import com.duongame.basicplayer.manager.FileManager;
import com.duongame.basicplayer.manager.ThumbnailManager;
import com.duongame.basicplayer.task.LoadThumbnailTask;
import com.duongame.basicplayer.view.ThumbnailImageView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by js296 on 2017-06-06.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final static String TAG = MovieAdapter.class.getSimpleName();

    private ArrayList<MovieFile> movieList = new ArrayList<>();
    private Context context;

    public MovieAdapter(Context context) {
        this.context = context;
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
        final LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = layoutInflater.inflate(R.layout.list_item, parent, false);
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
        } else {
            // 비트맵을 읽어들인 후에 설정하자
            LoadThumbnailTask task = new LoadThumbnailTask(movieList.get(position), holder.iv);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
                if(!FileManager.checkRecentFile(context, file.getAbsolutePath())) {
                    FileManager.openFile(context, file.getAbsolutePath(), 0L, 0);
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
