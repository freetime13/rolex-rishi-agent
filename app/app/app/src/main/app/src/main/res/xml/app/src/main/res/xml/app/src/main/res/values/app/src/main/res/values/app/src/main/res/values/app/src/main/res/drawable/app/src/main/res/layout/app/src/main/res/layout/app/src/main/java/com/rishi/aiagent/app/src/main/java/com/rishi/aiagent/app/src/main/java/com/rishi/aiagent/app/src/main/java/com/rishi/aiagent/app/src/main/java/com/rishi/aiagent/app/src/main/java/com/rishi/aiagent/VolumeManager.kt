package com.rishi.aiagent

import android.content.Context
import android.media.AudioManager

class VolumeManager(private val context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    fun adjustRelative(increase: Boolean): Boolean {
        val am = audioManager ?: return false
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
        return true
    }

    fun setPercentage(percent: Int): Boolean {
        val am = audioManager ?: return false
        val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val target = ((percent.coerceIn(0, 100) / 100.0) * maxVolume).toInt()
        am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
        return true
    }
}
