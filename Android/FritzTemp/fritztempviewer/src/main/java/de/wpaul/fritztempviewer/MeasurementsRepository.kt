package de.wpaul.fritztempviewer

import android.app.Application
import android.util.Log
import de.wpaul.fritztempcommons.MeasurementsDB
import de.wpaul.fritztempcommons.MeasurementsDao
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime

class MeasurementsRepository(val app: Application) {

    companion object {
        private val TAG = MeasurementsRepository::class.simpleName
    }

    private val measurementsDB = MeasurementsDB.getDefault(app)
    val measurementsDao = measurementsDB.measurementsDao()
    private val loggerClient = LoggerClient(app)
    private val updateJob: Job

    init {
        updateJob = GlobalScope.launch {
            while (isActive) {
                try {
                    fetchAndParseLog()
                    delay(20000)
                } catch (e: Throwable) {
                    Log.w(TAG, "Error while trying to update log!", e)
                }
            }
        }
    }

    private suspend fun <T> accessDao(operation: (MeasurementsDao) -> T) = withContext(Dispatchers.Default) {
        operation(measurementsDao)
    }

    fun saveDbToFile(file: File, overwrite: Boolean = true) {
        Log.i(TAG, "Saving database backup to ${file.absolutePath}")
        app.getDatabasePath(measurementsDB.name).copyTo(file, overwrite)
    }

    private suspend fun fetchAndParseLog() {
        val status = loggerClient.getStatus()
        val minDate = measurementsDao.getYoungestEntry()?.timestamp ?: LocalDateTime.MIN
        if (status.latestEntryDate > minDate) {
            measurementsDao.insert(loggerClient.getLog(after = minDate))
        }
        if (status.logEntries != measurementsDao.countAll())
            measurementsDao.replaceAllData(loggerClient.getLog())
        else Log.v(TAG, "already had newest log. not fetching anything")
    }

    fun reloadAllData() = GlobalScope.launch {
        measurementsDao.replaceAllData(loggerClient.getLog())
    }
}