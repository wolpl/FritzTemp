package de.wpaul.fritztempviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_status.*
import kotlinx.coroutines.*
import java.time.LocalDate

class StatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
            App.instance.loggerClient.fetchAndParseLog()
            val status = App.instance.loggerClient.getStatus()
            val low = App.instance.loggerClient.dbDao.getMinTempAtDay(LocalDate.now())
            val high = App.instance.loggerClient.dbDao.getMaxTempAtDay(LocalDate.now())
            val oldest = App.instance.loggerClient.dbDao.getOldestEntry().getLocalString()
            val youngest = App.instance.loggerClient.dbDao.getYoungestEntry().getLocalString()
            withContext(Dispatchers.Main) {
                tvStatus?.text = getString(R.string.status_text, status.temperature.toFloat(), low, high, status.logEntries, oldest, youngest)
            }
        }
    }
}
