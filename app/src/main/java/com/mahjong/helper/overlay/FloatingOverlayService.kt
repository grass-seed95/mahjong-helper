package com.mahjong.helper.overlay

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

class FloatingOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var bubbleView: TextView? = null
    private var expandedView: LinearLayout? = null
    private var isExpanded = false

    private var currentTile = "🀄"
    private var currentSafety = 0
    private var currentScore = 0
    private var currentReason = ""

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                currentTile = it.getStringExtra("tile") ?: currentTile
                currentSafety = it.getIntExtra("safety", currentSafety)
                currentScore = it.getIntExtra("score", currentScore)
                currentReason = it.getStringExtra("reason") ?: currentReason
                updateDisplay()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        registerReceiver(receiver, IntentFilter("com.mahjong.helper.UPDATE_OVERLAY"))
        createBubbleView()
    }

    private fun createBubbleView() {
        val bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 300
        }

        bubbleView = TextView(this).apply {
            text = "🀄"
            textSize = 22f
            setBackgroundColor(0xCC16A34A.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(20, 12, 20, 12)
            elevation = 8f

            setOnClickListener {
                isExpanded = !isExpanded
                if (isExpanded) showExpandedView() else hideExpandedView()
            }
        }

        windowManager.addView(bubbleView, bubbleParams)
    }

    private fun showExpandedView() {
        val expandedParams = WindowManager.LayoutParams(
            580, WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 380
        }

        expandedView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xEE1E293B.toInt())
            setPadding(20, 16, 20, 16)
            elevation = 8f

            val titleText = TextView(context).apply {
                text = "麻将助手建议"
                textSize = 14f
                setTextColor(0xFF94A3B8.toInt())
            }
            addView(titleText)

            val tileText = TextView(context).apply {
                text = "建议: $currentTile"
                textSize = 28f
                setTextColor(0xFFFFFFFF.toInt())
            }
            addView(tileText)

            val detailText = TextView(context).apply {
                text = "安全: $currentSafety | 综合: $currentScore 分\n$currentReason"
                textSize = 13f
                setTextColor(0xFFCBD5E1.toInt())
                setPadding(0, 8, 0, 0)
            }
            addView(detailText)
        }

        windowManager.addView(expandedView, expandedParams)
    }

    private fun hideExpandedView() {
        expandedView?.let { windowManager.removeView(it) }
        expandedView = null
    }

    fun updateSuggestion(tile: String, safety: Int, reason: String) {
        currentTile = tile
        currentSafety = safety
        currentReason = reason
        updateDisplay()
    }

    private fun updateDisplay() {
        bubbleView?.text = currentTile
        if (isExpanded && expandedView != null) {
            windowManager.removeView(expandedView)
            expandedView = null
            showExpandedView()
        }
    }

    override fun onDestroy() {
        try { unregisterReceiver(receiver) } catch (_: Exception) {}
        bubbleView?.let { windowManager.removeView(it) }
        expandedView?.let { windowManager.removeView(it) }
        super.onDestroy()
    }
}
