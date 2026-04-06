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

        val txtName = findViewById<TextView>(R.id.txtOwnerName)
        val txtEmail = findViewById<TextView>(R.id.txtEmail)
        val txtFacebook = findViewById<TextView>(R.id.txtFacebook)

        txtName.text = "MD Rabbi Hossain"
        txtEmail.text = "rabbihossainltd@gmail.com"
        txtFacebook.text = "facebook.com/share/1BDs5h2U3Q/"

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