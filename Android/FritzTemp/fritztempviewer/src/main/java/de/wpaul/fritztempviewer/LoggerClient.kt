package de.wpaul.fritztempviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.beust.klaxon.Klaxon
import de.wpaul.fritztempcommons.*
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime

class LoggerClient(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val dbDao: MeasurementsDao
    private val client = OkHttpClient()
    private val dateConverter = DateTimeConverter()
    val dbName = "Measurements.db"

    companion object {
        const val TAG = "LoggerClient"
    }

    init {
        dbDao = MeasurementsDB.create(context, dbName).measurementsDao()
        if (uri != null)
            GlobalScope.launch {
                dbDao.deleteDuplicates()
                val stat = async { getStatus() }
                val localEntries = dbDao.countAll()
                Log.i(TAG, "$localEntries entries in local database. ${stat.await().logEntries} entries in logger database.")
            }
    }

    var uri: String?
        get() = sharedPreferences.getString(PreferencesKeys.LastUri, null)
        @SuppressLint("ApplySharedPref")
        set(v) {
            sharedPreferences.edit().putString(PreferencesKeys.LastUri, v).commit()
        }

    private suspend fun fetchString(uri: String? = this.uri, suffix: String): String = GlobalScope.async(Dispatchers.Default, CoroutineStart.DEFAULT) {
        requireNotNull(uri) { throw IllegalArgumentException("uri must not be null") }
        return@async URL(ServerUri(uri).full + suffix).readText()
    }.await()

    suspend fun getStatusRaw(uri: String? = this.uri) = fetchString(uri, "/status")

    suspend fun getStatus(uri: String? = this.uri) = Klaxon().parse<Status>(getStatusRaw(uri))!!

    suspend fun getLog(uri: String? = this.uri): List<Measurement>? {
        fetchAndParseLog(uri)
        return dbDao.getAll()
    }

    suspend fun fetchAndParseLog(uri: String? = this.uri) {
        suspend fun fetchAfter(d: LocalDateTime) {
            Log.v(TAG, "fetching log after $d")
            val measurements = fetchString(uri, "/log?after=${URLEncoder.encode(dateConverter.toString(d), "UTF-8")}").lines().asSequence().filter { !it.isEmpty() }
                    .map { Measurement.parse(it) }
            dbDao.insert(measurements.toList())
            dbDao.deleteDuplicates()
        }

        val status = getStatus(uri)
        val minDate = dbDao.getYoungestEntry()?.timestamp ?: LocalDateTime.MIN
        if (status.latestEntryDate > minDate) {
            fetchAfter(minDate)
        }
        if (status.logEntries != dbDao.countAll())
            fetchAndParseWholeLog(uri)
        else Log.v(TAG, "already had newest log. not fetching anything")
    }

    private suspend fun fetchAndParseWholeLog(uri: String? = this.uri) {
        Log.v(TAG, "fetching whole log")
        val measurements = fetchString(uri, "/log").lines().asSequence().filter { !it.isEmpty() }
                .map { Measurement.parse(it) }
        dbDao.replaceAllData(measurements.toList(), true)
    }

    suspend fun getMinMaxAverageAnalysis(uri: String? = this.uri): List<MinMaxAvgTemperatureElement>? {
        getLog(uri)
        return dbDao.getMinMaxAverageTemperatureByDay()
    }

    suspend fun getConfig(uri: String? = this.uri): Config = Klaxon().parse<Config>(fetchString(uri, "/config"))
            ?: throw RuntimeException("Received bad json for config!")

    suspend fun setConfig(config: Config, uri: String? = this.uri) {
        withContext(Dispatchers.Default) {
            setSingleConfig("interval", config.interval.toString(), uri)
            setSingleConfig("ain", config.sensor!!, uri)
        }
    }

    private suspend fun setSingleConfig(key: String, value: String, uri: String? = this.uri) = GlobalScope.async(Dispatchers.Default, CoroutineStart.DEFAULT) {
        val body = RequestBody.create(MediaType.parse("text"), value)
        val request = Request.Builder().url(ServerUri(uri!!, "/config/$key").full).put(body).build()
        return@async client.newCall(request).execute()
    }.await()

    suspend fun refreshLog(uri: String? = this.uri) {
        fetchAndParseWholeLog(uri)
    }
}