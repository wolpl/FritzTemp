package de.wpaul.fritztempviewer

import android.content.Context
import android.util.AttributeSet
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.Viewport
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import de.wpaul.fritztempcommons.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class DateTimeGraphView(context: Context, attrs: AttributeSet) : GraphView(context, attrs) {

    private val dayMonthLabelFormatter = DateAsXAxisLabelFormatter(context, SimpleDateFormat("dd.MM.", Locale.GERMAN))
    private val dayMonthYearLabelFormatter = DateAsXAxisLabelFormatter(context, SimpleDateFormat("dd.MM.yy", Locale.GERMAN))
    private val dayMonthHourMinuteLabelFormatter = DateAsXAxisLabelFormatter(context, SimpleDateFormat("dd.MM.\nHH:mm", Locale.GERMAN))

    init {
        with(gridLabelRenderer) {
            setHumanRounding(false)
            labelFormatter = dayMonthLabelFormatter
            labelHorizontalHeight = 50
            numVerticalLabels = 5
        }

        with(viewport) {
            isXAxisBoundsManual = true
            isScrollable = true
            isScalable = true
            onXAxisBoundsChangedListener = Viewport.OnXAxisBoundsChangedListener { minX, maxX, _ ->
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
        }
    }
}