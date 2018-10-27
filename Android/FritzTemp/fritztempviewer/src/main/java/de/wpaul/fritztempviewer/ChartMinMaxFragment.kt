package de.wpaul.fritztempviewer


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import de.wpaul.fritztempcommons.plus
import kotlinx.android.synthetic.main.fragment_chart_min_max.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import java.text.SimpleDateFormat
import java.util.*

class ChartMinMaxFragment : androidx.fragment.app.Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chart_min_max, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            val data = App.instance.loggerClient.getMinMaxAverageAnalysis()
            val minSeries: LineGraphSeries<DataPoint> = LineGraphSeries(data.map { DataPoint(it.day, it.min.toDouble()) }.toTypedArray())
            val maxSeries: LineGraphSeries<DataPoint> = LineGraphSeries(data.map { DataPoint(it.day, it.max.toDouble()) }.toTypedArray())
            val avgSeries: LineGraphSeries<DataPoint> = LineGraphSeries(data.map { DataPoint(it.day, it.avg.toDouble()) }.toTypedArray())

            minSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMin)
            maxSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempMax)
            avgSeries.color = ContextCompat.getColor(this@ChartMinMaxFragment.context!!, R.color.colorTempAvg)

            withContext(UI) {
                with(graph_view) {
                    addSeries(minSeries)
                    addSeries(maxSeries)
                    addSeries(avgSeries)

                    viewport.isScrollable = true
                    gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(activity, SimpleDateFormat("dd.MM.", Locale.GERMAN))
                    gridLabelRenderer.setHumanRounding(false)
                    viewport.setScalableY(true)
                    viewport.isScalable = true

                    viewport.setMinX(Date().plus(Calendar.MONTH, -1).time.toDouble())
                }
            }
        }
    }
}
