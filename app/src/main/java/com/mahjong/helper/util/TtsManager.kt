package com.mahjong.helper.util

import android.content.Context
import android.os.Build
import android.speech.tts.TextToSpeech
import java.util.*

class TtsManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.CHINESE
            isReady = true
        }
    }

    fun speak(text: String) {
        if (!isReady) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mahjong_advice")
        } else {
            @Suppress("DEPRECATION")
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    fun speakDiscardAdvice(tile: String, safetyScore: Int, warning: String?) {
        val sb = StringBuilder()
        sb.append("建议打出${tile}，安全度${safetyScore}")
        warning?.let { sb.append("，${it}") }
        speak(sb.toString())
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
