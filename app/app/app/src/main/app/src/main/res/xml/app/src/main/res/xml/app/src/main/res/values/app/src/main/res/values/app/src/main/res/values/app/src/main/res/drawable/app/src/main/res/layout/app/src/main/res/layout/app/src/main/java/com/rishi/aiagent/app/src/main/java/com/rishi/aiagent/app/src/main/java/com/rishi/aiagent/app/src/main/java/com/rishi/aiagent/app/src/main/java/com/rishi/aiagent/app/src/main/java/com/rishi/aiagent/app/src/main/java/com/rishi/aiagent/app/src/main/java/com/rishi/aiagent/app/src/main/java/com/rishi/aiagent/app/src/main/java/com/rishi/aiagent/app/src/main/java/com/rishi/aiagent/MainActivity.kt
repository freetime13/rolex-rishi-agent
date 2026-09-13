package com.rishi.aiagent

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var tvStatus: TextView
    private lateinit var tvPermStatus: TextView
    private lateinit var btnToggle: Button
    private lateinit var btnPermissions: Button
    private lateinit var btnBattery: Button

    private var isServiceActive = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        refreshStatus()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permissionManager = PermissionManager(this)

        tvStatus = findViewById(R.id.tvServiceStatus)
        tvPermStatus = findViewById(R.id.tvPermStatus)
        btnToggle = findViewById(R.id.btnToggleService)
        btnPermissions = findViewById(R.id.btnGrantPermissions)
        btnBattery = findViewById(R.id.btnBatteryOptimization)

        btnToggle.setOnClickListener {
            toggleService()
        }

        btnPermissions.setOnClickListener {
            requestSystemPermissions()
        }

        btnBattery.setOnClickListener {
            requestIgnoreBatteryOptimization()
        }

        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun refreshStatus() {
        tvPermStatus.text = permissionManager.getMissingPermissionsReport()
        if (isServiceActive) {
            tvStatus.text = getString(R.string.status_active)
            btnToggle.text = getString(R.string.deactivate_assistant)
            btnToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.error_color))
        } else {
            tvStatus.text = getString(R.string.status_inactive)
            btnToggle.text = getString(R.string.activate_assistant)
            btnToggle.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_accent))
        }
    }

    private fun toggleService() {
        val serviceIntent = Intent(this, RishiForegroundService::class.java)
        if (isServiceActive) {
            stopService(serviceIntent)
            isServiceActive = false
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            isServiceActive = true
        }
        refreshStatus()
    }

    private fun requestSystemPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CONTACTS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissions.toTypedArray())

        // System Overlay Permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        // Write Settings Permission
        if (!Settings.System.canWrite(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun requestIgnoreBatteryOptimization() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {}
    }
}
