package de.wpaul.fritztempviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.beust.klaxon.Klaxon
import de.wpaul.fritztempcommons.*
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class LoggerClient(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dbDao = MeasurementsDB.create(context, "Measurements.db").measurementsDao()
    private var fetchedLog = false

    var uri: String?
        get() = sharedPreferences.getString(PreferencesKeys.LastUri, null)
        @SuppressLint("ApplySharedPref")
        set(v) {
            sharedPreferences.edit().putString(PreferencesKeys.LastUri, v).commit()
        }

    private fun fetchString(uri: String? = this.uri, suffix: String): String {
        requireNotNull(uri) { throw IllegalArgumentException("uri must not be null") }
        return URL(ServerUri(uri!!).full + suffix).readText()
    }

    fun getStatusRaw(uri: String? = this.uri) = fetchString(uri, "/status")

    fun getStatus(uri: String? = this.uri) = Klaxon().parse<Status>(getStatusRaw(uri))!!

    fun getLog(uri: String? = this.uri): List<Measurement> {
        if (!fetchedLog) {
            fetchAndParseLog(uri)
        }
        return dbDao.getAll()
    }

    fun fetchAndParseLog(uri: String? = this.uri) {
        val measurements = fetchString(uri, "/log").lines().asSequence().filter { !it.isEmpty() }
                .map { Measurement(it.split(";")[1].toFloat(), SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT).parse(it.split(";")[0])) }
        dbDao.insert(measurements.toList())
        fetchedLog = true
    }

    fun getMinMaxAverageAnalysis(uri: String? = this.uri): List<MinMaxAvgTemperatureElement> {
        getLog(uri)
        return dbDao.getMinMaxAverageTemperatureByDaySince(Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.time)
    }
}