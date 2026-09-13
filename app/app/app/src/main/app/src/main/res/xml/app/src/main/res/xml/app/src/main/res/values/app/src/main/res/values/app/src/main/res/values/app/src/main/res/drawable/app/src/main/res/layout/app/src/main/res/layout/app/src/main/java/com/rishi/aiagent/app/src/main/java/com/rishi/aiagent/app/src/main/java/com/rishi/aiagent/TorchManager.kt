package com.rishi.aiagent

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager

class TorchManager(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var cameraId: String? = null

    init {
        try {
            cameraManager?.cameraIdList?.forEach { id ->
                val hasFlash = cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash) {
                    cameraId = id
                    return@forEach
                }
            }
        } catch (_: Exception) {}
    }

    fun setTorch(enabled: Boolean): Boolean {
        val id = cameraId ?: return false
        return try {
            cameraManager?.setTorchMode(id, enabled)
            true
        } catch (_: CameraAccessException) {
            false
        } catch (_: Exception) {
            false
        }
    }
}
