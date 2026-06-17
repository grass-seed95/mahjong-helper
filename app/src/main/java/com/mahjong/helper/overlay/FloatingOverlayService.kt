package com.mahjong.helper.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var expandedView: View? = null
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createBubbleView()
    }

    private fun createBubbleView() {
        val bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 200
        }

        overlayView = TextView(this).apply {
            text = "\uD83C\uDC00"
            textSize = 18f
            setBackgroundColor(0xCC22C55E.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(16, 8, 16, 8)

            setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) showExpandedView() else hideExpandedView()
            }
        }

        windowManager.addView(overlayView, bubbleParams)
    }

    fun updateSuggestion(tile: String, safety: Int, reason: String) {
        (overlayView as? TextView)?.text = tile
    }

    private fun showExpandedView() {
        val expandedParams = WindowManager.LayoutParams(
            600, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100; y = 280
        }

        expandedView = TextView(this).apply {
            text = ""
            textSize = 14f
            setBackgroundColor(0xDD1E293B.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(16, 12, 16, 12)
        }
        windowManager.addView(expandedView, expandedParams)
    }

    private fun hideExpandedView() {
        expandedView?.let { windowManager.removeView(it) }
        expandedView = null
    }

    override fun onDestroy() {
        overlayView?.let { windowManager.removeView(it) }
        expandedView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }
}
