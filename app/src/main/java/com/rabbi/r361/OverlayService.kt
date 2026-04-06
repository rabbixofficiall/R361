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

    private var lastX = 0f
    private var lastY = 0f

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

        val markerParams = FrameLayout.LayoutParams(24, 24)
        root.addView(marker, markerParams)
        markerView = marker

        val confirm = TextView(this).apply {
            text = "✓"
            textSize = 28f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#AA00AA00"))
            setPadding(35, 20, 35, 20)
            setOnClickListener {
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
            setBackgroundColor(Color.parseColor("#AA990000"))
            setPadding(35, 20, 35, 20)
            setOnClickListener {
                stopSelf()
            }
        }

        val confirmLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        confirmLp.gravity = Gravity.TOP or Gravity.END
        confirmLp.topMargin = 80
        confirmLp.marginEnd = 40

        val cancelLp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        cancelLp.gravity = Gravity.TOP or Gravity.START
        cancelLp.topMargin = 80
        cancelLp.marginStart = 40

        root.addView(confirm, confirmLp)
        root.addView(cancel, cancelLp)

        root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                lastX = event.rawX
                lastY = event.rawY

                val lp = markerView?.layoutParams as FrameLayout.LayoutParams
                lp.leftMargin = (lastX - 12).toInt()
                lp.topMargin = (lastY - 12).toInt()
                markerView?.layoutParams = lp
                markerView?.visibility = View.VISIBLE
                true
            } else {
                false
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
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
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