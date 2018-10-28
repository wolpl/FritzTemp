package de.wpaul.fritztemp

import android.util.Log
import de.wpaul.fritztempcommons.Measurement
import de.wpaul.fritztempcommons.MeasurementsDB
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class TemperatureLogger(val config: Config, val db: MeasurementsDB) {

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
    }

    fun getTemperature() = fritzSession.getTemperature(config.ain)

    private fun resetInterval() {
        if (this::logTask.isInitialized)
            logTask.cancel(false)

        logTask = executor.scheduleAtFixedRate({
            try {
                db.measurementsDao().insert(Measurement(fritzSession.getTemperature(config.ain), sensor = config.ain))
            } catch (ex: Exception) {
                Log.e(TAG, "Could not log new temperature!")
            }
        }, 0L, config.interval, TimeUnit.MILLISECONDS)
    }

    fun deleteLog() {
        db.measurementsDao().deleteAll()
    }

    fun getLogCsvString(): String = db.measurementsDao().getAll().joinToString("\n") { it.toString() }
}