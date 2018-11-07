package de.wpaul.fritztempcommons

import com.beust.klaxon.Json
import java.util.*

data class Status(
        val running: String,
        @Json("sensor ain") val sensorAin: String,
        @Json("interval") val logInterval: Long,
        @Json("entries") val logEntries: Int,
        @Json("latest") val latestEntryDateString: String,
        val temperature: String
) {
    init {
        requireNotNull(dateConverter.toDateOrNull(latestEntryDateString)) { throw IllegalArgumentException("String for latestEntryDate was invalid!") }
    }

    @Json
    constructor(running: String, sensorAin: String, logInterval: Long, logEntries: Int, latestEntryDate: Date, temperature: String) :
            this(running, sensorAin, logInterval, logEntries, dateConverter.toString(latestEntryDate), temperature)

    companion object {
        private val dateConverter = DateConverter()
    }

    @Json(ignored = true)
    val latestEntryDate = dateConverter.toDate(latestEntryDateString)
}