package de.wpaul.fritztempviewer

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.wpaul.fritztempcommons.Config
import de.wpaul.fritztempcommons.ExternalSharedPreferencesProperty
import de.wpaul.fritztempcommons.Status
import kotlinx.coroutines.*

class ReportingViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        private val TAG = ReportingViewModel::class.simpleName
    }

    val measurementsRepo = MeasurementsRepository(app)

    private val loggerClient = LoggerClient(app)
    var serverUri: String? by ExternalSharedPreferencesProperty(app, PreferencesKeys.LastUri)
    val status: MutableLiveData<Status> = MutableLiveData()
    private val updateJob: Job

    override fun onCleared() {
        super.onCleared()
        updateJob.cancel()
    }

    init {
        updateJob = GlobalScope.launch {
            while (this.isActive) {
                if (loggerClient.uri != null) {
                    try {
                        val newVal = loggerClient.getStatus()
                        status.postValue(newVal)
                    } catch (e: Throwable) {
                        Log.w(TAG, "Error while trying to update status!", e)
                    }
                }
                delay(20000)
            }
        }
    }

    suspend fun getConfig() = loggerClient.getConfig()
    suspend fun setConfig(config: Config) = loggerClient.setConfig(config)
}