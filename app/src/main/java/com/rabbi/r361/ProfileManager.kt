package com.rabbi.r361

import android.content.Context

class ProfileManager(context: Context) {
    private val prefs = context.getSharedPreferences("r361_profiles", Context.MODE_PRIVATE)

    fun getProfiles(): MutableList<String> {
        val raw = prefs.getString("profiles_list", "Default") ?: "Default"
        return raw.split("||").filter { it.isNotBlank() }.toMutableList()
    }

    fun saveProfiles(list: List<String>) {
        prefs.edit().putString("profiles_list", list.joinToString("||")).apply()
    }

    fun getCurrentProfile(): String {
        return prefs.getString("current_profile", "Default") ?: "Default"
    }

    fun setCurrentProfile(name: String) {
        prefs.edit().putString("current_profile", name).apply()
    }

    fun addProfile(name: String): Boolean {
        val list = getProfiles()
        if (list.contains(name)) return false
        list.add(name)
        saveProfiles(list)
        return true
    }

    fun deleteProfile(name: String): Boolean {
        if (name == "Default") return false
        val list = getProfiles()
        val removed = list.remove(name)
        if (removed) {
            saveProfiles(list)
            if (getCurrentProfile() == name) {
                setCurrentProfile("Default")
            }
        }
        return removed
    }
}
