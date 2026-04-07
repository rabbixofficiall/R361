package com.rabbi.r361

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private lateinit var txtOverlay: TextView
    private lateinit var txtAccessibility: TextView

    companion object {
        private const val REQ_EXPORT = 1001
        private const val REQ_IMPORT = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        txtOverlay = findViewById(R.id.txtOverlay)
        txtAccessibility = findViewById(R.id.txtAccessibility)

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }

        findViewById<Button>(R.id.btnAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnExport).setOnClickListener {
            exportSettings()
        }

        findViewById<Button>(R.id.btnImport).setOnClickListener {
            importSettings()
        }

        findViewById<Button>(R.id.btnOpenUpdatePage).setOnClickListener {
            startActivity(Intent(this, UpdateActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenDebugPage).setOnClickListener {
            startActivity(Intent(this, DebugActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        txtOverlay.text = if (Settings.canDrawOverlays(this)) {
            "Overlay permission: ON"
        } else {
            "Overlay permission: OFF"
        }

        txtAccessibility.text = if (isAccessibilityEnabled()) {
            "Accessibility service: ON"
        } else {
            "Accessibility service: OFF"
        }
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

    private fun exportSettings() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "r361_backup.json")
        }
        startActivityForResult(intent, REQ_EXPORT)
    }

    private fun importSettings() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        startActivityForResult(intent, REQ_IMPORT)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return

        when (requestCode) {
            REQ_EXPORT -> writeBackupJson(data.data!!)
            REQ_IMPORT -> readBackupJson(data.data!!)
        }
    }

    private fun writeBackupJson(uri: Uri) {
        try {
            val prefsMain = getSharedPreferences("r361_prefs", MODE_PRIVATE)
            val prefsProfiles = getSharedPreferences("r361_profiles", MODE_PRIVATE)

            val root = JSONObject()
            root.put("r361_prefs", sharedPrefsToJson(prefsMain.all))
            root.put("r361_profiles", sharedPrefsToJson(prefsProfiles.all))

            contentResolver.openOutputStream(uri)?.use { out ->
                out.write(root.toString(2).toByteArray())
            }

            toast("Settings exported")
        } catch (e: Exception) {
            toast("Export failed")
        }
    }

    private fun readBackupJson(uri: Uri) {
        try {
            val jsonText = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            val root = JSONObject(jsonText)

            val prefsMain = getSharedPreferences("r361_prefs", MODE_PRIVATE)
            val prefsProfiles = getSharedPreferences("r361_profiles", MODE_PRIVATE)

            restoreSharedPrefs(prefsMain, root.getJSONObject("r361_prefs"))
            restoreSharedPrefs(prefsProfiles, root.getJSONObject("r361_profiles"))

            toast("Settings imported")
        } catch (e: Exception) {
            toast("Import failed")
        }
    }

    private fun sharedPrefsToJson(map: Map<String, *>): JSONObject {
        val obj = JSONObject()
        for ((key, value) in map) {
            when (value) {
                is String -> obj.put(key, value)
                is Int -> obj.put(key, value)
                is Boolean -> obj.put(key, value)
                is Float -> obj.put(key, value.toDouble())
                is Long -> obj.put(key, value)
                is Set<*> -> obj.put(key, JSONArray(value.toList()))
                else -> if (value != null) obj.put(key, value.toString())
            }
        }
        return obj
    }

    private fun restoreSharedPrefs(prefs: android.content.SharedPreferences, json: JSONObject) {
        val editor = prefs.edit()
        editor.clear()

        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)

            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Double -> {
                    val floatValue = value.toFloat()
                    if (floatValue.toDouble() == value) {
                        editor.putFloat(key, floatValue)
                    } else {
                        editor.putString(key, value.toString())
                    }
                }
                is JSONArray -> {
                    val set = mutableSetOf<String>()
                    for (i in 0 until value.length()) {
                        set.add(value.getString(i))
                    }
                    editor.putStringSet(key, set)
                }
                else -> editor.putString(key, value.toString())
            }
        }

        editor.apply()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
