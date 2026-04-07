package com.rabbi.r361

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UpdateActivity : AppCompatActivity() {

    private lateinit var txtCurrentVersion: TextView
    private lateinit var txtLatestVersion: TextView
    private lateinit var txtUpdateMessage: TextView
    private lateinit var btnCheckNow: Button
    private lateinit var btnOpenUpdate: Button

    private var latestUrl: String = ""
    private var latestVersionCode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        txtCurrentVersion = findViewById(R.id.txtCurrentVersion)
        txtLatestVersion = findViewById(R.id.txtLatestVersion)
        txtUpdateMessage = findViewById(R.id.txtUpdateMessage)
        btnCheckNow = findViewById(R.id.btnCheckNow)
        btnOpenUpdate = findViewById(R.id.btnOpenUpdate)

        val currentVersionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode.toInt()
        val currentVersionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"

        txtCurrentVersion.text = "Current version: $currentVersionName ($currentVersionCode)"
        txtLatestVersion.text = "Latest version: Checking..."
        txtUpdateMessage.text = "Tap the button below to check for updates."

        btnCheckNow.setOnClickListener {
            checkUpdate(currentVersionCode)
        }

        btnOpenUpdate.setOnClickListener {
            if (latestUrl.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(latestUrl)))
            } else {
                toast("No update link found")
            }
        }
    }

    private fun checkUpdate(currentVersionCode: Int) {
        txtLatestVersion.text = "Latest version: Checking..."
        txtUpdateMessage.text = "Please wait..."

        UpdateChecker(this).fetchUpdateInfo(
            onSuccess = { info ->
                latestUrl = info.apkUrl
                latestVersionCode = info.latestVersionCode
                txtLatestVersion.text = "Latest version: ${info.latestVersionName} (${info.latestVersionCode})"

                txtUpdateMessage.text =
                    if (info.latestVersionCode > currentVersionCode) {
                        "Update available.\n\n${info.message}"
                    } else {
                        "You are already using the latest version."
                    }
            },
            onError = {
                txtLatestVersion.text = "Latest version: Failed"
                txtUpdateMessage.text = "Update check failed."
            }
        )
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
