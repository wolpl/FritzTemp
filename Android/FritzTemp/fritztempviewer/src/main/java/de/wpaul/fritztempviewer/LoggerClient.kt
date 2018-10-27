package de.wpaul.fritztempviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.beust.klaxon.Klaxon
import de.wpaul.fritztempcommons.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class LoggerClient(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val dbDao: MeasurementsDao
    private var fetchedLog = false
    private val client = OkHttpClient()

    companion object {
        const val TAG = "LoggerClient"
    }

    init {
        Log.i(TAG, File("Measurements.db").exists().toString())
        dbDao = MeasurementsDB.create(context, "Measurements.db").measurementsDao()
    }

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

    fun getConfig(uri: String? = this.uri): Config = Klaxon().parse<Config>(fetchString(uri, "/config"))
            ?: throw RuntimeException("Received bad json for config!")

    suspend fun setConfig(config: Config, uri: String? = this.uri) {
        withContext(CommonPool) {
            setSingleConfig("interval", config.interval, uri)
            setSingleConfig("ain", config.sensor, uri)
        }
    }

    private suspend fun setSingleConfig(key: String, value: String, uri: String? = this.uri) = async {
        val body = RequestBody.create(MediaType.parse("text"), value)
        val request = Request.Builder().url(ServerUri(uri!!, "/config/$key").full).put(body).build()
        return@async client.newCall(request).execute()
    }.await()
}