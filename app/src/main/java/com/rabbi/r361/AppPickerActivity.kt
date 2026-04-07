package com.rabbi.r361

import android.content.pm.PackageManager
import android.os.Build
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

        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        val sortedApps = installedApps
            .filter { it.packageName != packageName }
            .sortedBy { pm.getApplicationLabel(it).toString().lowercase() }

        for (app in sortedApps) {
            val label = pm.getApplicationLabel(app).toString()
            appNames.add(label)
            appPackages.add(app.packageName)
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
