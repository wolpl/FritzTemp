package de.wpaul.fritztempviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.wpaul.fritztempcommons.toLocalDateTime
import kotlinx.android.synthetic.main.fragment_chart_raw.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class ChartRawFragment : androidx.fragment.app.Fragment() {

    private val dayMonthLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.", Locale.GERMAN))
    private val dayMonthYearLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.yy", Locale.GERMAN))
    private val dayMonthHourMinuteLabelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.\nHH:mm", Locale.GERMAN))

    private var temperatureToast: Toast? = null
    private lateinit var viewModel: ReportingViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_raw, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = activity?.run { ViewModelProviders.of(this).get(ReportingViewModel::class.java) } ?: throw Exception("Invalid activity!")
        GlobalScope.launch(Dispatchers.Default, CoroutineStart.DEFAULT) {
            val dataPoints = viewModel.measurementsRepo.measurementsDao.getAll()?.map { DataPoint(it.timestamp.toOldDate(), it.temperature.toDouble()) }?.sortedBy { it.x }?.toList()?.toTypedArray()
            if (dataPoints == null) {
                withContext(Dispatchers.Main) { Toast.makeText(context, "No data available!", Toast.LENGTH_LONG).show() }
                return@launch
            }
            activity?.runOnUiThread {
                graphView.apply {
                    val series = LineGraphSeries<DataPoint>(dataPoints)
                    series.isDrawBackground = true
                    series.setOnDataPointTapListener { _, dataPoint ->
                        temperatureToast?.cancel()
                        temperatureToast = Toast.makeText(activity, "${dataPoint.y} °C", Toast.LENGTH_LONG)
                        temperatureToast!!.show()
                    }
                    addSeries(series)

                    gridLabelRenderer.labelFormatter = dayMonthHourMinuteLabelFormatter
                    gridLabelRenderer.setHumanRounding(false)

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

                    viewport.setScalableY(true)
                    viewport.isScalable = true
                    val instance = Calendar.getInstance()
                    instance.add(Calendar.DAY_OF_MONTH, -30)
                    viewport.setMinX(instance.timeInMillis.toDouble())
                }
            }
        }
    }
}
