package com.rabbi.r361

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val txtOwnerName = findViewById<TextView>(R.id.txtOwnerName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val txtFacebook = findViewById<TextView>(R.id.txtFacebook)
        val txtVersion = findViewById<TextView>(R.id.txtVersion)
        val txtPackage = findViewById<TextView>(R.id.txtPackage)
        val txtDeveloperNote = findViewById<TextView>(R.id.txtDeveloperNote)

        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "Unknown"
        val versionCode = packageManager.getPackageInfo(packageName, 0).longVersionCode

        txtOwnerName.text = "MD Rabbi Hossain"
        txtEmail.text = "rabbihossainltd@gmail.com"
        txtFacebook.text = "facebook.com/share/1BDs5h2U3Q/"
        txtVersion.text = "Version: $versionName ($versionCode)"
        txtPackage.text = "Package: $packageName"
        txtDeveloperNote.text = "R361 is a premium AMOLED tool app with profile support, update check, export/import, and debug utilities."

        txtEmail.setOnClickListener {
            val i = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:rabbihossainltd@gmail.com")
            }
            startActivity(i)
        }

        txtFacebook.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/share/1BDs5h2U3Q/"))
            startActivity(i)
        }
    }
}
