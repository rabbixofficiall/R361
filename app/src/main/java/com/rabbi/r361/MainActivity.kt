package com.rabbi.r361

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager

    private lateinit var txtSelectedApp: TextView
    private lateinit var txtPointStatus: TextView
    private lateinit var txtSpeedValue: TextView
    private lateinit var txtOverlayStatus: TextView
    private lateinit var txtAccessibilityStatus: TextView
    private lateinit var seekSpeed: SeekBar
    private lateinit var inputClickCount: EditText
    private lateinit var inputClickLength: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PrefsManager(this)

        txtSelectedApp = findViewById(R.id.txtSelectedApp)
        txtPointStatus = findViewById(R.id.txtPointStatus)
        txtSpeedValue = findViewById(R.id.txtSpeedValue)
        txtOverlayStatus = findViewById(R.id.txtOverlayStatus)
        txtAccessibilityStatus = findViewById(R.id.txtAccessibilityStatus)
        seekSpeed = findViewById(R.id.seekSpeed)
        inputClickCount = findViewById(R.id.inputClickCount)
        inputClickLength = findViewById(R.id.inputClickLength)

        findViewById<Button>(R.id.btnApps).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<Button>(R.id.btnSaveSensitivity).setOnClickListener {
            val clickCount = inputClickCount.text.toString().trim().toIntOrNull() ?: 0
            val clickLength = inputClickLength.text.toString().trim().toIntOrNull() ?: 50

            prefs.setClickCount(clickCount.coerceAtLeast(0))
            prefs.setClickLength(clickLength.coerceAtLeast(1))
            toast("Sensitivity saved")
        }

        findViewById<Button>(R.id.btnSetPoint).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                openOverlaySettings()
                return@setOnClickListener
            }

            if (!isAccessibilityEnabled()) {
                openAccessibilitySettings()
                return@setOnClickListener
            }

            startService(Intent(this, OverlayService::class.java))
            toast("Open any target app, tap screen to set point, then press ✓")
        }

        seekSpeed.max = 1000
        seekSpeed.progress = prefs.getSpeedMs().coerceAtLeast(1)
        txtSpeedValue.text = prefs.getSpeedMs().toString()

        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safe = progress.coerceAtLeast(1)
                prefs.setSpeedMs(safe)
                txtSpeedValue.text = safe.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        inputClickCount.setText(prefs.getClickCount().toString())
        inputClickLength.setText(prefs.getClickLength().toString())

        if (!Settings.canDrawOverlays(this) || !isAccessibilityEnabled()) {
            toast("Enable Overlay and Accessibility permissions")
        }

        val currentVersionCode = packageManager
            .getPackageInfo(packageName, 0)
            .longVersionCode
            .toInt()

        UpdateChecker(this).checkForUpdate(currentVersionCode)
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun refreshUi() {
        txtSelectedApp.text = prefs.getSelectedAppName()
        txtPointStatus.text = if (prefs.hasPoint()) "Point saved" else "No point selected"
        txtOverlayStatus.text = if (Settings.canDrawOverlays(this)) "Overlay: ON" else "Overlay: OFF"
        txtAccessibilityStatus.text = if (isAccessibilityEnabled()) "Accessibility: ON" else "Accessibility: OFF"
        txtSpeedValue.text = prefs.getSpeedMs().toString()
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

    private fun openOverlaySettings() {
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
        )
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
