package com.duongame.basicplayer.view

import android.view.MotionEvent
import android.view.View

interface IPlayerView {
    fun onTouch(v: View?, ev: MotionEvent?): Boolean
    fun openFile(filename: String?): Boolean
    fun getPlaying(): Boolean
    fun setSeeking(b: Boolean)
    fun setBitmapRotation(rotation: Int)
    fun getBitmapRotation(): Int
    fun setPortrait(portrait: Boolean)
    fun pause(end: Boolean)
    fun resume()
    fun seekMovie(positionUs: Long): Int
    fun getMovieDurationUs(): Long
    fun close()
    fun invalidate()
    fun setOnClickListener(listener: View.OnClickListener)
}