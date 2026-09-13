package com.rishi.aiagent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.content.ContextCompat

class PermissionManager(private val context: Context) {

    fun hasRecordAudio(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    fun hasCallPhone(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    fun hasReadContacts(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun hasWriteSettings(): Boolean =
        Settings.System.canWrite(context)

    fun hasOverlayPermission(): Boolean =
        Settings.canDrawOverlays(context)

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun isBatteryOptimizationIgnored(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
    }

    fun getMissingPermissionsReport(): String {
        val sb = StringBuilder()
        sb.append("Microphone: ").append(if (hasRecordAudio()) "GRANTED" else "MISSING").append("\n")
        sb.append("Phone Calls: ").append(if (hasCallPhone()) "GRANTED" else "MISSING").append("\n")
        sb.append("Contacts: ").append(if (hasReadContacts()) "GRANTED" else "MISSING").append("\n")
        sb.append("Overlay: ").append(if (hasOverlayPermission()) "GRANTED" else "MISSING").append("\n")
        sb.append("Write Settings: ").append(if (hasWriteSettings()) "GRANTED" else "MISSING").append("\n")
        sb.append("Notifications: ").append(if (hasNotificationPermission()) "GRANTED" else "MISSING").append("\n")
        sb.append("Ignore Battery Opt: ").append(if (isBatteryOptimizationIgnored()) "YES" else "NO")
        return sb.toString()
    }
}
