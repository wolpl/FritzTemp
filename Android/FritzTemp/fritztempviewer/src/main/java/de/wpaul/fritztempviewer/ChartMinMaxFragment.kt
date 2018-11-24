package de.wpaul.fritztempviewer


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.wpaul.fritztempcommons.plus
import kotlinx.android.synthetic.main.fragment_chart_min_max.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class ChartMinMaxFragment : androidx.fragment.app.Fragment() {

    companion object {
        private val TAG = ChartMinMaxFragment::class.simpleName
    }

    private lateinit var viewModel: ReportingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_min_max, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProviders.of(this).get(ReportingViewModel::class.java) } ?: throw Exception("Invalid activity!")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        GlobalScope.launch {
            val data = viewModel.measurementsRepo.measurementsDao.getMinMaxAverageTemperatureByDay()

            if (data == null || context == null) return@launch
            val minSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.min.toDouble()) }.toTypedArray())
            val maxSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.max.toDouble()) }.toTypedArray())
            val avgSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.avg.toDouble()) }.toTypedArray())

            minSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMin)
            maxSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMax)
            avgSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempAvg)

            withContext(Dispatchers.Main) {
                with(graph_view) {
                    viewport.setMaxX(Date().time.toDouble())
                    viewport.setMinX(Date().plus(Calendar.MONTH, -1).time.toDouble())

                    addSeries(minSeries)
                    addSeries(maxSeries)
                    addSeries(avgSeries)
                }
            }
        }
    }
}
