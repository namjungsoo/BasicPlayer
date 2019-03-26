package com.duongame.basicplayer.task

import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.text.TextUtils
import android.util.Log
import com.duongame.basicplayer.Player
import com.duongame.basicplayer.data.MovieFile
import com.duongame.basicplayer.util.TimeConverter
import com.duongame.basicplayer.view.ThumbnailImageView
import io.realm.Realm
import java.lang.ref.WeakReference

class LoadTimeTextTask(realm: Realm, movieFile: MovieFile, imageView: ThumbnailImageView) : AsyncTask<Void, Void, Boolean>() {
    private val TAG = LoadThumbnailTask::class.java.simpleName
    private val realm: Realm = realm
    private val movieFile: MovieFile = movieFile
    private val imageViewRef: WeakReference<ThumbnailImageView> = WeakReference(imageView)

    private fun getPlayTimeText(path: String): String? {
        try {
            val begin = System.nanoTime()
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val timeMs = java.lang.Long.parseLong(time)
            val timeText = TimeConverter.convertMsToString(timeMs)
            val end = System.nanoTime()
            Log.d(TAG, "getPlayTimeText $path $timeText retriever=" + (end - begin))
            return timeText
        } catch (e: RuntimeException) {
            Log.e(TAG, "getPlayTimeText $path error")

            val begin = System.nanoTime()
            val player = Player()
            player.init()

            // FFmpeg NDK 라이브러리에서 로딩한다.
            // audio 사용여부는 false이다.
            val ret = player.openMovieWithAudio(path, 0)
            if(ret != 0)
                return null
            val timeText = TimeConverter.convertUsToString(player.movieDurationUs)
            player.closeMovie()

            val end = System.nanoTime()
            Log.d(TAG, "getPlayTimeText $path $timeText player=" + (end - begin))
            return timeText
        }
    }

    override fun doInBackground(vararg voids: Void): Boolean {
        val timeText = getPlayTimeText(movieFile.path)
        if (TextUtils.isEmpty(timeText))
            return false
        movieFile.timeText = timeText
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

        // timeText가 변하였으면...
        if (result) {
            if (imageViewRef.get()!!.tag == movieFile.path) {
                imageViewRef.get()!!.timeText = movieFile.timeText
                imageViewRef.get()!!.invalidate()
                Log.d(TAG, "setTimeText ${movieFile.path} ${movieFile.timeText}")
            } else {
                Log.e(TAG, "setTimeText ${movieFile.path} ${movieFile.timeText} FAIL")
            }

            realm.executeTransaction(Realm.Transaction {
                it.copyToRealmOrUpdate(movieFile)
            })
        }
    }
}
