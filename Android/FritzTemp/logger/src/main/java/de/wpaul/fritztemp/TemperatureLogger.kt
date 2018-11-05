package de.wpaul.fritztemp

import android.util.Log
import de.wpaul.fritztempcommons.Measurement
import de.wpaul.fritztempcommons.MeasurementsDB
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TemperatureLogger(val config: SharedPreferencesConfig, val db: MeasurementsDB, val dbFile: File) {

    companion object {
        const val TAG = "TemperatureLogger"
        val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    }

    private lateinit var logTask: ScheduledFuture<*>
    private val fritzSession: FritzSession = FritzSession()

    init {
        try {
            Log.v(TAG, "${getTemperature()} °C")
        } catch (e: Throwable) {
            Log.w(TAG, "Could not fetch and log temperature!")
        }
        config.onConfigChanged += { resetInterval() }
        resetInterval()
        GlobalScope.launch {
            db.measurementsDao().deleteDuplicates()
            Log.i(TAG, "Finished deleting duplicates")
        }
    }

    fun getTemperature() = if (config.sensor != null) fritzSession.getTemperature(config.sensor!!) else null

    private fun resetInterval() {
        if (this::logTask.isInitialized)
            logTask.cancel(false)

        logTask = executor.scheduleAtFixedRate({
            try {
                requireNotNull(config.sensor) { Log.w(TAG, "config.sensor is null!") }
                db.measurementsDao().insert(Measurement(fritzSession.getTemperature(config.sensor!!), sensor = config.sensor))
            } catch (ex: Exception) {
                Log.e(TAG, "Could not log new temperature!", ex)
            }
        }, 0L, config.interval, TimeUnit.MILLISECONDS)
    }

    fun deleteLog() {
        db.measurementsDao().deleteAll()
    }

    fun getLogCsvString(): String = db.measurementsDao().getAll().joinToString("\n") { it.toString() }
}