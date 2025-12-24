package com.app.brfaceresponder.data

import android.content.Context
import android.content.SharedPreferences
import com.app.brfaceresponder.model.Rule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SharedPrefs {
    private const val PREFS_NAME = "br_face_responder_prefs"
    private const val KEY_SERVICE_ENABLED = "service_enabled"
    private const val KEY_ENABLED_APPS = "enabled_apps"
    private const val KEY_RULES = "rules_json"

    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setServiceEnabled(context: Context, isEnabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SERVICE_ENABLED, isEnabled).apply()
    }

    fun isServiceEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SERVICE_ENABLED, false)
    }

    fun setAppEnabled(context: Context, packageName: String, isEnabled: Boolean) {
        val currentEnabledApps = getEnabledAppPackages(context).toMutableSet()
        if (isEnabled) {
            currentEnabledApps.add(packageName)
        } else {
            currentEnabledApps.remove(packageName)
        }
        getPrefs(context).edit().putStringSet(KEY_ENABLED_APPS, currentEnabledApps).apply()
    }

    fun getEnabledAppPackages(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_ENABLED_APPS, emptySet()) ?: emptySet()
    }

    fun saveRules(context: Context, rules: List<Rule>) {
        val json = gson.toJson(rules)
        getPrefs(context).edit().putString(KEY_RULES, json).apply()
    }

    fun getRules(context: Context): List<Rule> {
        val json = getPrefs(context).getString(KEY_RULES, null)
        return if (json != null) {
            val type = object : TypeToken<List<Rule>>() {}.type
            gson.fromJson(json, type)
        } else {
            // Return default rules if nothing is saved yet
            listOf(
                Rule(keyword = "olá", replyMessage = "Olá! Esta é uma resposta automática.", priority = 0),
                Rule(keyword = "teste", replyMessage = "Esta é uma regra de teste.", priority = 1)
            )
        }
    }
}