package de.wpaul.fritztemp

import android.annotation.SuppressLint
import android.app.Application
import android.preference.PreferenceManager
import android.util.Log

class App : Application() {
    companion object {
        var instance: App? = null
        private const val TAG = "App"
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    var username: String
        get() = PreferenceManager.getDefaultSharedPreferences(this).getString("USERNAME", "")
        @SuppressLint("ApplySharedPref")
        set(v) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("USERNAME", v).commit()
            Log.i(TAG, "Stored new username")
        }
    var password: String
        get() = PreferenceManager.getDefaultSharedPreferences(this).getString("PASSWORD", "")
        @SuppressLint("ApplySharedPref")
        set(v) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("PASSWORD", v).commit()
            Log.i(TAG, "Stored new password")
        }
}