package com.rabbi.r361

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DebugActivity : AppCompatActivity() {

    private lateinit var txtDebugInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        txtDebugInfo = findViewById(R.id.txtDebugInfo)
    }

    override fun onResume() {
        super.onResume()
        loadDebugInfo()
    }

    private fun loadDebugInfo() {
        val prefs = PrefsManager(this)
        val profileManager = ProfileManager(this)

        val overlay = if (Settings.canDrawOverlays(this)) "ON" else "OFF"
        val accessibility = if (isAccessibilityEnabled()) "ON" else "OFF"
        val pointSaved = if (prefs.hasPoint()) "YES" else "NO"
        val selectedApp = prefs.getSelectedAppName() ?: "None"
        val selectedPackage = prefs.getSelectedPackage() ?: "None"
        val speed = prefs.getSpeedMs()
        val clickCount = prefs.getClickCount()
        val clickLength = prefs.getClickLength()
        val profile = profileManager.getCurrentProfile()

        val info = """
            Overlay permission: $overlay
            
            Accessibility service: $accessibility
            
            Current profile: $profile
            
            Selected app: $selectedApp
            
            Selected package: $selectedPackage
            
            Point saved: $pointSaved
            
            Speed (ms): $speed
            
            Click count: $clickCount
            
            Click length (ms): $clickLength
        """.trimIndent()

        txtDebugInfo.text = info
    }

    private fun isAccessibilityEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val myService = ComponentName(this, AutoTapAccessibilityService::class.java)

        return enabledServices.any {
            it.resolveInfo.serviceInfo.packageName == myService.packageName &&
                it.resolveInfo.serviceInfo.name == myService.className
        }
    }
}
