package com.rabbi.r361

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class AutoTapAccessibilityService : AccessibilityService() {

    private lateinit var prefs: PrefsManager
    private val handler = Handler(Looper.getMainLooper())

    private var isRunning = false
    private var isTargetActive = false
    private var currentPackage: String? = null
    private var remainingClicks = 0

    private val loop = object : Runnable {
        override fun run() {
            if (!isRunning || !isTargetActive) return

            tap()

            val configuredCount = prefs.getClickCount()
            if (configuredCount > 0) {
                remainingClicks--
                if (remainingClicks <= 0) {
                    stopLoop()
                    return
                }
            }

            handler.postDelayed(this, prefs.getSpeedMs().toLong())
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = PrefsManager(this)
        isRunning = false
        isTargetActive = false
        currentPackage = null
        handler.removeCallbacks(loop)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        currentPackage = pkg

        val selected = prefs.getSelectedPackage()
        isTargetActive = !selected.isNullOrBlank() && pkg == selected

        if (!isTargetActive && isRunning) {
            stopLoop()
        }
    }

    override fun onInterrupt() {
        stopLoop()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) {
            return false
        }

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (isTargetActive && prefs.hasPoint()) {
                if (isRunning) stopLoop() else startLoop()
                return true
            }
        }

        return false
    }

    private fun startLoop() {
        if (!prefs.hasPoint()) return

        remainingClicks = prefs.getClickCount()
        isRunning = true
        handler.removeCallbacks(loop)
        handler.post(loop)
    }

    private fun stopLoop() {
        isRunning = false
        handler.removeCallbacks(loop)
    }

    private fun tap() {
        val dm = resources.displayMetrics
        val x = prefs.getPointX() * dm.widthPixels
        val y = prefs.getPointY() * dm.heightPixels

        val path = Path().apply { moveTo(x, y) }
        val duration = prefs.getClickLength().toLong().coerceAtLeast(1L)

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()

        dispatchGesture(gesture, null, null)
    }
}
