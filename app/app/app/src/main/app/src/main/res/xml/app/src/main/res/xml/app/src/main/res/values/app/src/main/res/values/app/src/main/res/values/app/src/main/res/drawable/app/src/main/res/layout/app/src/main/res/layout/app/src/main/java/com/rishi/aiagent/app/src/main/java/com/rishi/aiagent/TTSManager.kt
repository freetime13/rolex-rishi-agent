package com.rishi.aiagent

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TTSManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("hi", "IN").takeIf {
                    tts?.isLanguageAvailable(it) == TextToSpeech.LANG_AVAILABLE
                } ?: Locale.US
                isInitialized = true
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RISHI_TTS_ID")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
