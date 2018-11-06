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
import java.util.*

class LoggerClient(context: Context) {

    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val dbDao: MeasurementsDao
    private var fetchedLog = false
    private val client = OkHttpClient()
    val dbName = "Measurements.db"

    companion object {
        const val TAG = "LoggerClient"
    }

    init {
        dbDao = MeasurementsDB.create(context, dbName).measurementsDao()
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

    suspend fun getLog(uri: String? = this.uri): List<Measurement> {
        if (!fetchedLog) {
            fetchAndParseLog(uri)
        }
        return dbDao.getAll()
    }

    suspend fun fetchAndParseLog(uri: String? = this.uri) {
        val measurements = fetchString(uri, "/log").lines().asSequence().filter { !it.isEmpty() }
                .map { Measurement.parse(it) }
        dbDao.insert(measurements.toList())
        fetchedLog = true
    }

    suspend fun getMinMaxAverageAnalysis(uri: String? = this.uri): List<MinMaxAvgTemperatureElement> {
        getLog(uri)
        return dbDao.getMinMaxAverageTemperatureByDaySince(Calendar.getInstance().apply { add(Calendar.MONTH, -2) }.time)
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
        dbDao.deleteAll()
        fetchAndParseLog(uri)
    }
}