package de.wpaul.fritztemp

import android.content.SharedPreferences
import de.wpaul.fritztempcommons.Config

class SharedPreferencesConfig(
        preferences: SharedPreferences,
        sensor: String? = preferences.getString(AIN, null),
        interval: Long = preferences.getLong(INTERVAL, defaultInterval)
) : Config(sensor, interval) {

    companion object {
        private const val AIN = "ain"
        private const val INTERVAL = "interval"
    }

    init {
        onConfigChanged += {
            preferences.edit().putString(AIN, this.sensor).putLong(INTERVAL, this.interval).apply()
        }
    }


}