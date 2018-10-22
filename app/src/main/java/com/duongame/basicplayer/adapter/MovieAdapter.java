package com.duongame.basicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
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
import com.duongame.basicplayer.util.TimeConverter;
import com.duongame.basicplayer.view.ThumbnailImageView;

import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MICRO_KIND;
import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

/**
 * Created by js296 on 2017-06-06.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private final static String TAG = MovieAdapter.class.getSimpleName();

    private List<MovieFile> movieList;
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

    public void setMovieList(List<MovieFile> movieList) {
        this.movieList = movieList;
    }

    @Override
    public MovieAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = layoutInflater.inflate(R.layout.list_item, parent, false);
        final ViewHolder holder = new ViewHolder(v);

        holder.iv = (ThumbnailImageView) v.findViewById(R.id.thumbnail);
        holder.tvName = (TextView) v.findViewById(R.id.textName);
        holder.tvPath = (TextView) v.findViewById(R.id.textPath);

        return holder;
    }

    private String getPlayTimeText(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeMs = Long.parseLong(time);
            return TimeConverter.convertMsToString(timeMs);
        } catch (RuntimeException e) {
            Log.e(TAG, "getPlayTimeText " + path + " error");
            return "";
        }
    }

    @Override
    public void onBindViewHolder(MovieAdapter.ViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder " + position);
        final MovieFile file = movieList.get(position);

        //mini only
        int kind;
        Bitmap bitmap = ThumbnailManager.getBitmap(MINI_KIND, file.path);
        if (bitmap != null) {
            holder.iv.setImageBitmap(bitmap);
        } else {
            kind = MINI_KIND;
            // 비트맵을 읽어들인 후에 설정하자
            holder.iv.setTag(movieList.get(position).path);

            LoadThumbnailTask task = new LoadThumbnailTask(kind, movieList.get(position), holder.iv);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

        //micro only
//        int kind;
//        Bitmap bitmap = ThumbnailManager.getBitmap(MICRO_KIND, file.path);
//        if (bitmap != null) {
//            holder.iv.setImageBitmap(bitmap);
//        } else {
//            kind = MICRO_KIND;
//            // 비트맵을 읽어들인 후에 설정하자
//            holder.iv.setTag(movieList.get(position).path);
//
//            LoadThumbnailTask task = new LoadThumbnailTask(kind, movieList.get(position), holder.iv);
//            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//        }


        // mini가 있으면 종료
//        Bitmap bitmap = ThumbnailManager.getBitmap(MINI_KIND, file.path);
//        if (bitmap != null) {
//            holder.iv.setImageBitmap(bitmap);
//        } else {
//            // micro가 있으면 micro를 셋팅하고 mini를 로딩
//            int kind;
//            bitmap = ThumbnailManager.getBitmap(MICRO_KIND, file.path);
//            if (bitmap != null) {
//                holder.iv.setImageBitmap(bitmap);
//                kind = MINI_KIND;
//            } else {
//                kind = MICRO_KIND;
//            }
//
//            // 비트맵을 읽어들인 후에 설정하자
//            holder.iv.setTag(movieList.get(position).path);
//
//            LoadThumbnailTask task = new LoadThumbnailTask(kind, movieList.get(position), holder.iv);
//            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
//        }


        holder.iv.setTimeText(getPlayTimeText(file.path));

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
