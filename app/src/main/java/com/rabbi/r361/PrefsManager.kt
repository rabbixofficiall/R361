package com.rabbi.r361

import android.content.Context

class PrefsManager(context: Context) {
    private val pref = context.getSharedPreferences("r361_prefs", Context.MODE_PRIVATE)

    fun setSelectedPackage(value: String) = pref.edit().putString("selected_pkg", value).apply()
    fun getSelectedPackage(): String? = pref.getString("selected_pkg", null)

    fun setSelectedAppName(value: String) = pref.edit().putString("selected_app_name", value).apply()
    fun getSelectedAppName(): String? = pref.getString("selected_app_name", "No app selected")

    fun setPoint(xRatio: Float, yRatio: Float) {
        pref.edit()
            .putFloat("point_x", xRatio)
            .putFloat("point_y", yRatio)
            .putBoolean("has_point", true)
            .apply()
    }

    fun getPointX(): Float = pref.getFloat("point_x", 0.5f)
    fun getPointY(): Float = pref.getFloat("point_y", 0.5f)
    fun hasPoint(): Boolean = pref.getBoolean("has_point", false)

    fun setSpeedMs(value: Int) = pref.edit().putInt("speed_ms", value).apply()
    fun getSpeedMs(): Int = pref.getInt("speed_ms", 50)

    fun setClickCount(value: Int) = pref.edit().putInt("click_count", value).apply()
    fun getClickCount(): Int = pref.getInt("click_count", 0)

    fun setClickLength(value: Int) = pref.edit().putInt("click_length", value).apply()
    fun getClickLength(): Int = pref.getInt("click_length", 50)
}
