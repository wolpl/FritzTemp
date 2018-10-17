package de.wpaul.fritztemp

import android.util.Log
import com.beust.klaxon.Klaxon
import de.wpaul.fritztempcommons.Event
import java.io.File

class Config(private val filesDir: String) {

    companion object {
        private const val TAG = "Config"
        private const val AIN = "ain"
        private const val INTERVAL = "interval"
        private const val LOGPATH = "logPath"
    }

    private val confFile = File("$filesDir/config.json")

    private val _map: MutableMap<String, String>

    private val validators = mapOf(
            INTERVAL to { it: String -> it.toIntOrNull() != null }
    )

    val onConfigChanged = Event<String>()

    init {
        val defaultMap = mutableMapOf(
                LOGPATH to "$filesDir/TempLog.txt",
                INTERVAL to "10000",
                AIN to "119600690696"
        )

        _map = if (confFile.exists()) {
            try {
                Log.v(TAG, confFile.readText())
                val res = Klaxon().parse<JsonHelper>(confFile)!!.map
                require(res.containsKey(INTERVAL))
                require(res.containsKey(AIN))
                require(res.containsKey(LOGPATH))
                res
            } catch (e: Exception) {
                Log.e(TAG, "Error while reading config json", e)
                defaultMap
            }
        } else {
            defaultMap
        }
        saveToFile()

        onConfigChanged += { saveToFile() }
    }

    private fun saveToFile() {
        confFile.writeText(Klaxon().toJsonString(JsonHelper(_map)))
    }

    operator fun get(key: String) = _map[key]
    operator fun set(key: String, value: String) {
        if (!validators.containsKey(key) || validators[key]?.invoke(value) == true) {
            if (_map[key] != value) {
                _map[key] = value
                onConfigChanged(key)
            }
        } else
            throw IllegalArgumentException("Data validation failed")
    }

    val map: Map<String, String>
        get() = _map

    var interval
        get() = this[INTERVAL]!!.toLong()
        set(it) {
            this[INTERVAL] = it.toString()
        }

    var ain: String
        get() = this[AIN]!!
        set(it) {
            this[AIN] = it
        }

    var logPath: String
        get() = this[LOGPATH]!!
        set(it) {
            this[LOGPATH] = File(it).absolutePath
        }

    val logFile
        get() = File(logPath)

    class JsonHelper(val map: MutableMap<String, String>)
}