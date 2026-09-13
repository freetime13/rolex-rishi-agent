package com.rishi.aiagent

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class RishiForegroundService : Service() {

    private lateinit var wakeWordManager: WakeWordManager
    private lateinit var commandParser: CommandParser
    private lateinit var torchManager: TorchManager
    private lateinit var callManager: CallManager
    private lateinit var brightnessManager: BrightnessManager
    private lateinit var volumeManager: VolumeManager
    private lateinit var overlayManager: OverlayManager
    private lateinit var ttsManager: TTSManager

    override fun onCreate() {
        super.onCreate()

        commandParser = CommandParser()
        torchManager = TorchManager(this)
        callManager = CallManager(this)
        brightnessManager = BrightnessManager(this)
        volumeManager = VolumeManager(this)
        overlayManager = OverlayManager(this)
        ttsManager = TTSManager(this)

        wakeWordManager = WakeWordManager(
            context = this,
            onWakeWordDetected = {
                overlayManager.show()
            },
            onCommandHeard = { spokenText ->
                handleCommand(spokenText)
            }
        )

        createNotificationChannel()
        val notification = buildForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        wakeWordManager.startListening()
    }

    private fun handleCommand(text: String) {
        when (val command = commandParser.parse(text)) {
            is ParsedCommand.Call -> {
                ttsManager.speak("${command.contactQuery} ko call kar raha hoon")
                val result = callManager.makeCall(command.contactQuery)
                if (result is CallManager.CallResult.MultipleMatches) {
                    ttsManager.speak("Multiple contacts found. Opening dialer.")
                } else if (result is CallManager.CallResult.NotFound) {
                    ttsManager.speak("Contact nahi mila.")
                }
            }
            is ParsedCommand.Torch -> {
                val ok = torchManager.setTorch(command.enable)
                if (ok) {
                    ttsManager.speak(if (command.enable) "Torch on" else "Torch off")
                } else {
                    ttsManager.speak("Flashlight access fail ho gaya")
                }
            }
            is ParsedCommand.Brightness -> {
                val ok = when (val action = command.action) {
                    is BrightnessAction.Increase -> brightnessManager.adjustRelative(true)
                    is BrightnessAction.Decrease -> brightnessManager.adjustRelative(false)
                    is BrightnessAction.Max -> brightnessManager.setMax()
                    is BrightnessAction.Min -> brightnessManager.setMin()
                    is BrightnessAction.Percent -> brightnessManager.setPercentage(action.value)
                }
                if (ok) {
                    ttsManager.speak("Brightness change kar di")
                } else {
                    ttsManager.speak("Settings permission required")
                }
            }
            is ParsedCommand.Volume -> {
                val ok = when (val action = command.action) {
                    is VolumeAction.Increase -> volumeManager.adjustRelative(true)
                    is VolumeAction.Decrease -> volumeManager.adjustRelative(false)
                    is VolumeAction.Percent -> volumeManager.setPercentage(action.value)
                }
                if (ok) {
                    ttsManager.speak("Volume updated")
                }
            }
            is ParsedCommand.None -> {
                // Command unhandled, return silently to standby
            }
        }

        overlayManager.hide()
        wakeWordManager.resetToWakeWordMode()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rishi Assistant Active Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification to keep Rishi AI background listening active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rishi is active")
            .setContentText("Say 'Rolex' to give a command")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordManager.stopListening()
        overlayManager.hide()
        ttsManager.shutdown()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1010
        private const val CHANNEL_ID = "rishi_agent_fg_channel"
    }
}
