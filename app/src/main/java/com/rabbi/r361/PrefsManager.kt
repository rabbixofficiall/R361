package com.rabbi.r361

import android.content.Context

class PrefsManager(context: Context) {
    private val pref = context.getSharedPreferences("r361_prefs", Context.MODE_PRIVATE)
    private val profileManager = ProfileManager(context)

    private fun key(base: String): String {
        val profile = profileManager.getCurrentProfile()
        return "${profile}_$base"
    }

    fun setSelectedPackage(value: String) = pref.edit().putString(key("selected_pkg"), value).apply()
    fun getSelectedPackage(): String? = pref.getString(key("selected_pkg"), null)

    fun setSelectedAppName(value: String) = pref.edit().putString(key("selected_app_name"), value).apply()
    fun getSelectedAppName(): String? = pref.getString(key("selected_app_name"), "No app selected")

    fun setPoint(xRatio: Float, yRatio: Float) {
        pref.edit()
            .putFloat(key("point_x"), xRatio)
            .putFloat(key("point_y"), yRatio)
            .putBoolean(key("has_point"), true)
            .apply()
    }

    fun getPointX(): Float = pref.getFloat(key("point_x"), 0.5f)
    fun getPointY(): Float = pref.getFloat(key("point_y"), 0.5f)
    fun hasPoint(): Boolean = pref.getBoolean(key("has_point"), false)

    fun clearPoint() {
        pref.edit()
            .remove(key("point_x"))
            .remove(key("point_y"))
            .putBoolean(key("has_point"), false)
            .apply()
    }

    fun setSpeedMs(value: Int) = pref.edit().putInt(key("speed_ms"), value).apply()
    fun getSpeedMs(): Int = pref.getInt(key("speed_ms"), 50)

    fun setClickCount(value: Int) = pref.edit().putInt(key("click_count"), value).apply()
    fun getClickCount(): Int = pref.getInt(key("click_count"), 0)

    fun setClickLength(value: Int) = pref.edit().putInt(key("click_length"), value).apply()
    fun getClickLength(): Int = pref.getInt(key("click_length"), 50)
}
