package com.rabbi.r361

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val prefs = getSharedPreferences("r361_prefs", MODE_PRIVATE)
        val firstRun = prefs.getBoolean("first_run", true)

        Handler(Looper.getMainLooper()).postDelayed({
            if (firstRun) {
                startActivity(Intent(this, SetupActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }, 1500)
    }
}
