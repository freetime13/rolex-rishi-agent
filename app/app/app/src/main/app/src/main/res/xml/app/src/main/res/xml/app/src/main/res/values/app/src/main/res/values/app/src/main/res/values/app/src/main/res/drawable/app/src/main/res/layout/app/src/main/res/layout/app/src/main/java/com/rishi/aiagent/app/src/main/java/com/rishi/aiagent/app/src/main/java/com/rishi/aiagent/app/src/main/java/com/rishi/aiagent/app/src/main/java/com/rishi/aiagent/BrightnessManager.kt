package com.rishi.aiagent

import android.content.Context
import android.provider.Settings

class BrightnessManager(private val context: Context) {

    fun adjustRelative(increase: Boolean): Boolean {
        if (!Settings.System.canWrite(context)) return false
        val current = getCurrentBrightness()
        val delta = if (increase) 51 else -51
        val target = (current + delta).coerceIn(10, 255)
        return setExact(target)
    }

    fun setPercentage(percent: Int): Boolean {
        if (!Settings.System.canWrite(context)) return false
        val bounded = percent.coerceIn(5, 100)
        val value = ((bounded / 100.0) * 255).toInt()
        return setExact(value)
    }

    fun setMin(): Boolean = setPercentage(5)
    fun setMax(): Boolean = setPercentage(100)

    private fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (_: Exception) {
            128
        }
    }

    private fun setExact(value: Int): Boolean {
        return try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                value
            )
            true
        } catch (_: Exception) {
            false
        }
    }
}
