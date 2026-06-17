package com.mahjong.helper.capture

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import com.mahjong.helper.engine.DiscardAdvisor
import com.mahjong.helper.engine.model.GameState
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.overlay.FloatingOverlayService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class CaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private val binder = LocalBinder()

    private val _latestBitmap = MutableStateFlow<Bitmap?>(null)
    val latestBitmap: StateFlow<Bitmap?> = _latestBitmap

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult

    private var captureJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val advisor = DiscardAdvisor()
    private var tts: TextToSpeech? = null
    private var recognizer: VisionRecognizer? = null

    data class AnalysisResult(
        val hand: Hand,
        val suggestedTile: String,
        val safetyScore: Int,
        val acceptanceCount: Int,
        val combinedScore: Int,
        val reason: String
    )

    inner class LocalBinder : Binder() {
        fun getService(): CaptureService = this@CaptureService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startForeground(
            1,
            Notification.Builder(this, createChannel())
                .setContentTitle("麻将助手运行中")
                .setContentText("正在监控屏幕")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .build()
        )
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.CHINESE
            }
        }
    }

    fun startCapture(resultCode: Int, data: Intent, config: ScreenConfig? = null) {
        val projectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let {
                val planes = it.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width
                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                _latestBitmap.value = bitmap
                it.close()
            }
        }, null)

        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "MahjongCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )

        // Start overlay service for displaying results
        startOverlay()

        // Start periodic analysis
        if (config != null) {
            recognizer = VisionRecognizer(config, emptyMap())
            startAnalysisLoop()
        }
    }

    private fun startAnalysisLoop() {
        captureJob?.cancel()
        captureJob = scope.launch {
            while (isActive) {
                delay(2000) // Analyze every 2 seconds
                val bitmap = _latestBitmap.value ?: continue
                val rec = recognizer ?: continue

                try {
                    val gameState = rec.recognize(bitmap)
                    if (gameState != null) {
                        val recommendations = advisor.recommend(gameState.myHand, gameState)
                        val best = advisor.bestDiscard(recommendations)
                        if (best != null) {
                            val result = AnalysisResult(
                                hand = gameState.myHand,
                                suggestedTile = best.tile.toString(),
                                safetyScore = best.safetyScore,
                                acceptanceCount = best.acceptanceCount,
                                combinedScore = best.combinedScore.toInt(),
                                reason = best.reason
                            )
                            _analysisResult.value = result

                            // Update overlay
                            updateOverlay(result)

                            // Speak advice
                            speakAdvice(result)
                        }
                    }
                } catch (e: Exception) {
                    // Recognition failure, skip this frame
                }
            }
        }
    }

    private fun startOverlay() {
        val intent = Intent(this, FloatingOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun updateOverlay(result: AnalysisResult) {
        val intent = Intent(UPDATE_OVERLAY_ACTION).apply {
            setPackage(packageName)
            putExtra("tile", result.suggestedTile)
            putExtra("safety", result.safetyScore)
            putExtra("reason", result.reason)
            putExtra("score", result.combinedScore)
        }
        sendBroadcast(intent)
    }

    private fun speakAdvice(result: AnalysisResult) {
        val text = "建议打${result.suggestedTile}，安全度${result.safetyScore}，" +
                "进张${result.acceptanceCount}张"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "mahjong_advice")
    }

    fun stopCapture() {
        captureJob?.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        stopService(Intent(this, FloatingOverlayService::class.java))
    }

    override fun onDestroy() {
        stopCapture()
        tts?.shutdown()
        scope.cancel()
        super.onDestroy()
    }

    private fun createChannel(): String {
        val channelId = "capture_service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "屏幕监控", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        return channelId
    }

    companion object {
        const val UPDATE_OVERLAY_ACTION = "com.mahjong.helper.UPDATE_OVERLAY"
    }
}
