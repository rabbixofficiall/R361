package com.rabbi.r361

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateChecker(private val context: Context) {

    fun checkForUpdate(currentVersionCode: Int) {
        Thread {
            try {
                val url = URL("https://raw.githubusercontent.com/rabbixofficiall/R361/main/update.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()

                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val json = JSONObject(response)

                val latestVersionCode = json.getInt("latestVersionCode")
                val latestVersionName = json.getString("latestVersionName")
                val apkUrl = json.getString("apkUrl")
                val message = json.getString("message")

                if (latestVersionCode > currentVersionCode) {
                    Handler(Looper.getMainLooper()).post {
                        AlertDialog.Builder(context)
                            .setTitle("Update Available")
                            .setMessage("$message\n\nLatest version: $latestVersionName")
                            .setCancelable(true)
                            .setPositiveButton("Update") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
                                context.startActivity(intent)
                            }
                            .setNegativeButton("Later", null)
                            .show()
                    }
                }
            } catch (_: Exception) {
            }
        }.start()
    }
}
