package de.wpaul.fritztempviewer


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.wpaul.fritztempcommons.plus
import de.wpaul.fritztempcommons.toLocalDateTime
import kotlinx.android.synthetic.main.fragment_chart_min_max.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class ChartMinMaxFragment : androidx.fragment.app.Fragment() {

    companion object {
        private val TAG = ChartMinMaxFragment::class.simpleName
    }

    private val dayMonthLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.", Locale.GERMAN))
    private val dayMonthYearLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.yy", Locale.GERMAN))
    private val dayMonthHourMinuteLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.\nHH:mm", Locale.GERMAN))
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
            activity?.runOnUiThread {
                if (data == null) return@runOnUiThread
                val minSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.min.toDouble()) }.toTypedArray())
                val maxSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.max.toDouble()) }.toTypedArray())
                val avgSeries = LineGraphSeries(data.map { DataPoint(it.day.toOldDate(), it.avg.toDouble()) }.toTypedArray())

                minSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMin)
                maxSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMax)
                avgSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempAvg)

                if (graph_view == null) {
                    Log.w(TAG, "graph_view was null!")
                    return@runOnUiThread
                }

                with(graph_view) {
                    addSeries(minSeries)
                    addSeries(maxSeries)
                    addSeries(avgSeries)

                    viewport.isScrollable = true
                    gridLabelRenderer.labelFormatter = dayMonthHourMinuteLabelFormatter
                    gridLabelRenderer.setHumanRounding(false)
                    viewport.setScalableY(true)
                    viewport.isScalable = true

                    viewport.onXAxisBoundsChangedListener = Viewport.OnXAxisBoundsChangedListener { minX, maxX, _ ->
                        val span = Duration.between(minX.toLong().toLocalDateTime(), maxX.toLong().toLocalDateTime())
                        gridLabelRenderer.labelFormatter = when {
                            span <= Duration.ofDays(2) -> {
                                gridLabelRenderer.labelHorizontalHeight = 100
                                dayMonthHourMinuteLabelFormatter
                            }
                            span <= Duration.ofDays(365) -> {
                                gridLabelRenderer.labelHorizontalHeight = 50
                                dayMonthLabelFormatter
                            }
                            else -> {
                                gridLabelRenderer.labelHorizontalHeight = 100
                                dayMonthYearLabelFormatter
                            }
                        }
                    }
                    viewport.setMinX(Date().plus(Calendar.MONTH, -1).time.toDouble())
                }
            }

        }


    }
}
