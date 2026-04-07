package com.rabbi.r361

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var listView: ListView
    private lateinit var searchBox: EditText

    private val allAppNames = mutableListOf<String>()
    private val allAppPackages = mutableListOf<String>()

    private val filteredNames = mutableListOf<String>()
    private val filteredPackages = mutableListOf<String>()

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_picker)

        prefs = PrefsManager(this)
        listView = findViewById(R.id.listApps)
        searchBox = findViewById(R.id.searchApps)

        loadApps()
        setupSearch()

        listView.setOnItemClickListener { _, _, position, _ ->
            prefs.setSelectedAppName(filteredNames[position])
            prefs.setSelectedPackage(filteredPackages[position])

            AlertDialog.Builder(this)
                .setTitle("App selected")
                .setMessage("Warning: For better trigger accuracy, use the selected app in landscape mode.")
                .setPositiveButton("OK") { _, _ ->
                    Toast.makeText(this, "Selected: ${filteredNames[position]}", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .show()
        }
    }

    private fun loadApps() {
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

        allAppNames.clear()
        allAppPackages.clear()

        for (app in sortedApps) {
            val label = pm.getApplicationLabel(app).toString()
            allAppNames.add(label)
            allAppPackages.add(app.packageName)
        }

        filteredNames.clear()
        filteredPackages.clear()
        filteredNames.addAll(allAppNames)
        filteredPackages.addAll(allAppPackages)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filteredNames)
        listView.adapter = adapter
    }

    private fun setupSearch() {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString().orEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun filterApps(query: String) {
        val q = query.trim().lowercase()

        filteredNames.clear()
        filteredPackages.clear()

        if (q.isEmpty()) {
            filteredNames.addAll(allAppNames)
            filteredPackages.addAll(allAppPackages)
        } else {
            for (i in allAppNames.indices) {
                val name = allAppNames[i]
                val pkg = allAppPackages[i]
                if (name.lowercase().contains(q) || pkg.lowercase().contains(q)) {
                    filteredNames.add(name)
                    filteredPackages.add(pkg)
                }
            }
        }

        adapter.notifyDataSetChanged()
    }
}
