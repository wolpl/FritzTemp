package de.wpaul.fritztempcommons

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SharedPreferencesProperty(private val key: String, private val commit: Boolean = true) : ReadWriteProperty<Context, String?> {
    override fun getValue(thisRef: Context, property: KProperty<*>): String? {
        return PreferenceManager.getDefaultSharedPreferences(thisRef).getString(key, null)
    }

    @SuppressLint("ApplySharedPref")
    override fun setValue(thisRef: Context, property: KProperty<*>, value: String?) {
        if (commit)
            PreferenceManager.getDefaultSharedPreferences(thisRef).edit().putString(key, value).commit()
        else
            PreferenceManager.getDefaultSharedPreferences(thisRef).edit().putString(key, value).apply()
    }
}

class ExternalSharedPreferencesProperty(private val context: Context, protected val key: String, protected val commit: Boolean = true) : ReadWriteProperty<Any, String?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null)
    }

    @SuppressLint("ApplySharedPref")
    override fun setValue(thisRef: Any, property: KProperty<*>, value: String?) {
        if (commit)
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).commit()
        else
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value).apply()
    }
}