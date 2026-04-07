package com.rabbi.r361

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var prefs: PrefsManager
    private var rootView: FrameLayout? = null
    private var markerView: View? = null

    private var lastX = -1f
    private var lastY = -1f

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        showOverlay()
    }

    private fun showOverlay() {
        val root = FrameLayout(this)
        root.setBackgroundColor(Color.parseColor("#22000000"))

        val marker = View(this).apply {
            setBackgroundColor(Color.GREEN)
            visibility = View.GONE
        }

        val markerParams = FrameLayout.LayoutParams(28, 28)
        root.addView(marker, markerParams)
        markerView = marker

        val confirm = TextView(this).apply {
            text = "✓"
            textSize = 28f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC00AA00"))
            setPadding(35, 20, 35, 20)
            setOnClickListener {
                if (lastX < 0 || lastY < 0) {
                    Toast.makeText(this@OverlayService, "Tap screen first to set point", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val dm = resources.displayMetrics
                val xRatio = (lastX / dm.widthPixels).coerceIn(0f, 1f)
                val yRatio = (lastY / dm.heightPixels).coerceIn(0f, 1f)

                prefs.setPoint(xRatio, yRatio)
                Toast.makeText(this@OverlayService, "Point saved", Toast.LENGTH_SHORT).show()
                stopSelf()
            }
        }

        val cancel = TextView(this).apply {
            text = "✕"
            textSize = 24f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC990000"))
            setPadding(35, 20, 35, 20)
            setOnClickListener { stopSelf() }
        }

        val confirmLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 80
            marginEnd = 40
        }

        val cancelLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            topMargin = 80
            marginStart = 40
        }

        root.addView(confirm, confirmLp)
        root.addView(cancel, cancelLp)

        root.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    lastX = event.rawX
                    lastY = event.rawY

                    val lp = markerView?.layoutParams as FrameLayout.LayoutParams
                    lp.leftMargin = (lastX - 14).toInt()
                    lp.topMargin = (lastY - 14).toInt()
                    markerView?.layoutParams = lp
                    markerView?.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(root, params)
        rootView = root
    }

    override fun onDestroy() {
        rootView?.let { windowManager.removeView(it) }
        rootView = null
        markerView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
