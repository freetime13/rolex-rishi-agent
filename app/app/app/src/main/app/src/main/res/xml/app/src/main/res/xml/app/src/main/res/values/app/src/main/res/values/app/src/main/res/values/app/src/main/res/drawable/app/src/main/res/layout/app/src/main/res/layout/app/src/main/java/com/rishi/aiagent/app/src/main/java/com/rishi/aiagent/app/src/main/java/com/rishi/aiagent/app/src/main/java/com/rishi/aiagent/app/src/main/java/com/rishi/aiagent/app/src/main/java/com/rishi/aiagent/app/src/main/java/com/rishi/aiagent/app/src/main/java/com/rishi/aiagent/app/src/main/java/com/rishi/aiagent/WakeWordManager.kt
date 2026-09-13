package com.rishi.aiagent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class WakeWordManager(
    private val context: Context,
    private val onWakeWordDetected: () -> Unit,
    private val onCommandHeard: (String) -> Unit
) {
    private var recognizer: SpeechRecognizer? = null
    private var isListening = false
    private var isWakeWordActive = false

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        stopListening()

        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(SpeechListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
        }

        try {
            recognizer?.startListening(intent)
            isListening = true
        } catch (_: Exception) {}
    }

    fun stopListening() {
        isListening = false
        try {
            recognizer?.stopListening()
            recognizer?.cancel()
            recognizer?.destroy()
        } catch (_: Exception) {}
        recognizer = null
    }

    fun resetToWakeWordMode() {
        isWakeWordActive = false
        startListening()
    }

    private inner class SpeechListener : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            // Automatically reset and keep listening
            if (isListening) {
                startListening()
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                processHeardText(matches[0])
            } else {
                startListening()
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty() && !isWakeWordActive) {
                val heard = matches[0].lowercase()
                if (heard.contains("rolex")) {
                    isWakeWordActive = true
                    onWakeWordDetected()
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun processHeardText(rawText: String) {
        val text = rawText.lowercase()
        if (!isWakeWordActive) {
            if (text.contains("rolex")) {
                isWakeWordActive = true
                onWakeWordDetected()
                val remainder = text.substringAfter("rolex").trim()
                if (remainder.isNotEmpty()) {
                    onCommandHeard(remainder)
                    return
                }
            }
            startListening()
        } else {
            onCommandHeard(text)
        }
    }
}
