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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var profileManager: ProfileManager

    private lateinit var txtSelectedApp: TextView
    private lateinit var txtPointStatus: TextView
    private lateinit var txtSpeedValue: TextView
    private lateinit var txtOverlayStatus: TextView
    private lateinit var txtAccessibilityStatus: TextView
    private lateinit var txtCurrentProfile: TextView
    private lateinit var seekSpeed: SeekBar
    private lateinit var inputClickCount: EditText
    private lateinit var inputClickLength: EditText
    private lateinit var spinnerProfiles: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PrefsManager(this)
        profileManager = ProfileManager(this)

        txtSelectedApp = findViewById(R.id.txtSelectedApp)
        txtPointStatus = findViewById(R.id.txtPointStatus)
        txtSpeedValue = findViewById(R.id.txtSpeedValue)
        txtOverlayStatus = findViewById(R.id.txtOverlayStatus)
        txtAccessibilityStatus = findViewById(R.id.txtAccessibilityStatus)
        txtCurrentProfile = findViewById(R.id.txtCurrentProfile)
        seekSpeed = findViewById(R.id.seekSpeed)
        inputClickCount = findViewById(R.id.inputClickCount)
        inputClickLength = findViewById(R.id.inputClickLength)
        spinnerProfiles = findViewById(R.id.spinnerProfiles)

        setupProfiles()

        findViewById<Button>(R.id.btnAddProfile).setOnClickListener {
            showAddProfileDialog()
        }

        findViewById<Button>(R.id.btnDeleteProfile).setOnClickListener {
            val current = profileManager.getCurrentProfile()
            if (profileManager.deleteProfile(current)) {
                toast("Profile deleted")
                setupProfiles()
                refreshUi()
            } else {
                toast("Cannot delete Default profile")
            }
        }

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
            val selectedPkg = prefs.getSelectedPackage()

            if (selectedPkg.isNullOrBlank()) {
                toast("Select an app first")
                return@setOnClickListener
            }

            if (!Settings.canDrawOverlays(this)) {
                openOverlaySettings()
                return@setOnClickListener
            }

            if (!isAccessibilityEnabled()) {
                openAccessibilitySettings()
                return@setOnClickListener
            }

            val launchIntent = packageManager.getLaunchIntentForPackage(selectedPkg)
            if (launchIntent == null) {
                toast("Could not open selected app")
                return@setOnClickListener
            }

            startService(Intent(this, OverlayService::class.java))
            startActivity(launchIntent)
            toast("Selected app opened. Tap screen to choose point, then press ✓")
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

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun setupProfiles() {
        val profiles = profileManager.getProfiles()
        val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_spinner_item, profiles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProfiles.adapter = adapter

        val current = profileManager.getCurrentProfile()
        val index = profiles.indexOf(current).coerceAtLeast(0)
        spinnerProfiles.setSelection(index)

        spinnerProfiles.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selected = profiles[position]
                if (profileManager.getCurrentProfile() != selected) {
                    profileManager.setCurrentProfile(selected)
                    prefs = PrefsManager(this@MainActivity)
                    refreshUi()
                    inputClickCount.setText(prefs.getClickCount().toString())
                    inputClickLength.setText(prefs.getClickLength().toString())
                    seekSpeed.progress = prefs.getSpeedMs().coerceAtLeast(1)
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun showAddProfileDialog() {
        val input = EditText(this)
        input.hint = "Profile name"

        AlertDialog.Builder(this)
            .setTitle("Add Profile")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    if (profileManager.addProfile(name)) {
                        profileManager.setCurrentProfile(name)
                        prefs = PrefsManager(this)
                        toast("Profile created")
                        setupProfiles()
                        refreshUi()
                    } else {
                        toast("Profile already exists")
                    }
                } else {
                    toast("Enter profile name")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun refreshUi() {
        txtCurrentProfile.text = "Current profile: ${profileManager.getCurrentProfile()}"
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
        startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
