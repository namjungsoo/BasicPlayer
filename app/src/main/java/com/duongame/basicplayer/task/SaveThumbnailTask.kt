package com.duongame.basicplayer.task

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import com.duongame.basicplayer.data.MovieFile
import io.realm.Realm
import java.io.FileOutputStream
import java.lang.ref.WeakReference
import java.util.*

class SaveThumbnailTask(context: Context, realm: Realm, movieFile: MovieFile, bitmap: Bitmap) : AsyncTask<Void, Void, Boolean>() {
    private val TAG = SaveThumbnailTask::class.java.simpleName
    private val contextWeakReference = WeakReference<Context>(context)
    private val realm = realm
    private val movieFile = movieFile
    private val bitmap = bitmap

    override fun doInBackground(vararg params: Void?): Boolean {
        // 넘어온 비트맵을 파일로 저징한다.
        val uuid = UUID.randomUUID().toString()
        val path = contextWeakReference.get()?.cacheDir?.absolutePath + "/" + uuid + ".png"

        // 파일명을 저장하고
        // Realm 트랜잭션으로 write 한다.
        val output = FileOutputStream(path)
        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output))
            return false
        Log.d(TAG, "SaveThumbnailTask compress $path")
        movieFile.thumbnail = path
        return true
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)

        if (result) {
            realm.executeTransaction(Realm.Transaction {
                Log.d(TAG, "SaveThumbnailTask thumbnail=${movieFile.thumbnail}")
                it.copyToRealmOrUpdate(movieFile)
            })
        }
    }
}