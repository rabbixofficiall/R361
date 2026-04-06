package com.rabbi.r361

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class AutoTapAccessibilityService : AccessibilityService() {

    var running = false
    val handler = Handler(Looper.getMainLooper())

    val loop = object:Runnable{
        override fun run(){
            if(!running) return
            tap()
            handler.postDelayed(this,50)
        }
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {}

    override fun onInterrupt(){}

    override fun onKeyEvent(e: KeyEvent): Boolean {
        if(e.keyCode==KeyEvent.KEYCODE_VOLUME_UP && e.action==KeyEvent.ACTION_DOWN){
            running=!running
            if(running) handler.post(loop)
            else handler.removeCallbacks(loop)
            return true
        }
        return false
    }

    fun tap(){
        val p = Path()
        p.moveTo(500f,1000f)

        val g = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(p,0,50))
            .build()

        dispatchGesture(g,null,null)
    }
}