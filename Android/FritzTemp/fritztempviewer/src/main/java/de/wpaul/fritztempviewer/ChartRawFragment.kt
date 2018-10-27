package de.wpaul.fritztempviewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.fragment_chart_raw.*
import kotlinx.coroutines.experimental.launch
import java.text.SimpleDateFormat
import java.util.*

class ChartRawFragment : androidx.fragment.app.Fragment() {

    private var temperatureToast: Toast? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_raw, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launch {
            val dataPoints = App.instance.loggerClient.getLog().map { DataPoint(it.date, it.temperature.toDouble()) }.sortedBy { it.x }.toList().toTypedArray()
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

                    gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.", Locale.GERMAN))
                    gridLabelRenderer.setHumanRounding(false)

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
