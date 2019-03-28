package com.duongame.basicplayer.data

import android.util.Log
import io.realm.DynamicRealm
import io.realm.RealmMigration

class MovieFileMigration : RealmMigration {
    companion object {
        val TAG = MovieFileMigration::class.java.simpleName
    }
    override fun migrate(realm: DynamicRealm?, oldVersion: Long, newVersion: Long) {
        Log.d(TAG, "migrate BEGIN")
        val schema = realm?.schema
        var curVersion = oldVersion
        if(curVersion == 0L) {
            Log.d(TAG, "migrate curVersion == 0L")
            val movieFileSchema = schema?.get("MovieFile")

            movieFileSchema?.addField("thumbnail", String::class.java)
            movieFileSchema?.addField("size", Long::class.java)
            movieFileSchema?.addField("lastModified", Long::class.java)

            curVersion++
        }
        Log.d(TAG, "migrate EMD")
    }

}