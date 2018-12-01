package de.wpaul.fritztempviewer

import android.content.Context
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

    private val client = OkHttpClient()

    companion object {
        private const val TAG = "LoggerClient"
    }

    var uri: String? by ExternalSharedPreferencesProperty(context, PreferencesKeys.LastUri)

    private suspend fun fetchString(uri: String? = this.uri, suffix: String): String = GlobalScope.async(Dispatchers.IO, CoroutineStart.DEFAULT) {
        requireNotNull(uri) { throw IllegalArgumentException("uri must not be null") }
        return@async URL(ServerUri(uri).full + suffix).readText()
    }.await()

    suspend fun getStatusRaw(uri: String? = this.uri) = fetchString(uri, "/status")

    suspend fun getStatus(uri: String? = this.uri) = Klaxon().parse<Status>(getStatusRaw(uri))!!

    suspend fun getLog(uri: String? = this.uri, after: LocalDateTime? = null): List<Measurement> {
        if (after == null) return getWholeLog(uri)
        return getLogAfter(uri, after)
    }

    private suspend fun getLogAfter(uri: String? = this.uri, d: LocalDateTime): List<Measurement> {
        Log.v(TAG, "fetching log after $d")
        return fetchString(uri, "/log?after=${URLEncoder.encode(DateTimeConverter.instance.toString(d), "UTF-8")}").lines().asSequence().filter { !it.isEmpty() }
                .map { Measurement.parse(it) }.toList()
    }

    private suspend fun getWholeLog(uri: String? = this.uri): List<Measurement> {
        Log.v(TAG, "fetching whole log")
        return fetchString(uri, "/log").lines().asSequence().filter { !it.isEmpty() }
                .map { Measurement.parse(it) }.toList()
    }

    suspend fun getConfig(uri: String? = this.uri): Config = Klaxon().parse<Config>(fetchString(uri, "/config"))
            ?: throw RuntimeException("Received bad json for config!")

    suspend fun setConfig(config: Config, uri: String? = this.uri) {
        withContext(Dispatchers.IO) {
            setSingleConfig("interval", config.interval.toString(), uri)
            setSingleConfig("ain", config.sensor!!, uri)
        }
    }

    private suspend fun setSingleConfig(key: String, value: String, uri: String? = this.uri) = withContext(Dispatchers.IO) {
        val body = RequestBody.create(MediaType.parse("text"), value)
        val request = Request.Builder().url(ServerUri(uri!!, "/config/$key").full).put(body).build()
        return@withContext client.newCall(request).execute()
    }
}