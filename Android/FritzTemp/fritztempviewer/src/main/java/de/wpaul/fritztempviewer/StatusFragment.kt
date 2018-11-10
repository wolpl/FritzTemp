package de.wpaul.fritztempviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.wpaul.fritztempcommons.Measurement
import de.wpaul.fritztempcommons.Status
import kotlinx.android.synthetic.main.fragment_status.*
import java.time.LocalDate

class StatusFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    private lateinit var viewModel: ReportingViewModel
    private lateinit var status: LiveData<Status>
    private lateinit var low: LiveData<Float?>
    private lateinit var high: LiveData<Float?>
    private lateinit var oldest: LiveData<Measurement?>
    private lateinit var youngest: LiveData<Measurement?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = activity?.run { ViewModelProviders.of(this).get(ReportingViewModel::class.java) } ?: throw Exception("Invalid activity!")

        status = viewModel.status
        low = viewModel.measurementsRepo.measurementsDao.getMinTempAtDay(LocalDate.now())
        high = viewModel.measurementsRepo.measurementsDao.getMaxTempAtDay(LocalDate.now())
        oldest = viewModel.measurementsRepo.measurementsDao.getOldestEntry()
        youngest = viewModel.measurementsRepo.measurementsDao.getYoungestEntryLive()
        listOf(status, low, high, oldest, youngest).forEach { it.observe(this@StatusFragment, Observer { refreshStatusText() }) }
        refreshStatusText()
    }

    private fun refreshStatusText() {
        activity?.runOnUiThread {
            tvStatus?.text = getString(R.string.status_text, status.value?.temperature?.toFloat(), low.value, high.value, status.value?.logEntries, oldest.value?.getLocalString(), youngest.value?.getLocalString())
        }
    }
}
