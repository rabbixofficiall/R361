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

data class UpdateInfo(
    val latestVersionCode: Int,
    val latestVersionName: String,
    val apkUrl: String,
    val message: String
)

class UpdateChecker(private val context: Context) {

    private val updateJsonUrl =
        "https://raw.githubusercontent.com/rabbixofficiall/R361/main/update.json"

    fun checkForUpdate(currentVersionCode: Int) {
        fetchUpdateInfo(
            onSuccess = { info ->
                if (info.latestVersionCode > currentVersionCode) {
                    showUpdateDialog(info)
                }
            },
            onError = {}
        )
    }

    fun fetchUpdateInfo(
        onSuccess: (UpdateInfo) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val url = URL(updateJsonUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()

                val response = BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }

                val json = JSONObject(response)

                val info = UpdateInfo(
                    latestVersionCode = json.getInt("latestVersionCode"),
                    latestVersionName = json.getString("latestVersionName"),
                    apkUrl = json.getString("apkUrl"),
                    message = json.getString("message")
                )

                Handler(Looper.getMainLooper()).post {
                    onSuccess(info)
                }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post {
                    onError(e.message ?: "Update check failed")
                }
            }
        }.start()
    }

    private fun showUpdateDialog(info: UpdateInfo) {
        AlertDialog.Builder(context)
            .setTitle("Update Available")
            .setMessage("${info.message}\n\nLatest version: ${info.latestVersionName}")
            .setCancelable(true)
            .setPositiveButton("Update") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(info.apkUrl))
                context.startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }
}
