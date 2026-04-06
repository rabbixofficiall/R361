package com.rabbi.r361

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var listView: ListView
    private val appNames = mutableListOf<String>()
    private val appPackages = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        prefs = PrefsManager(this)
        listView = findViewById(R.id.listApps)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .sortedBy { it.loadLabel(pm).toString().lowercase() }

        for (app in apps) {
            val appName = app.loadLabel(pm).toString()
            val pkg = app.activityInfo.packageName
            if (pkg != packageName) {
                appNames.add(appName)
                appPackages.add(pkg)
            }
        }

        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appNames)

        listView.setOnItemClickListener { _, _, position, _ ->
            prefs.setSelectedAppName(appNames[position])
            prefs.setSelectedPackage(appPackages[position])

            AlertDialog.Builder(this)
                .setTitle("App selected")
                .setMessage("Warning: For better trigger accuracy, use the selected app in landscape mode.")
                .setPositiveButton("OK") { _, _ ->
                    Toast.makeText(this, "Selected: ${appNames[position]}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .show()
        }
    }
}