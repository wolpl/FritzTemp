package de.wpaul.fritztempcommons

import com.beust.klaxon.Json
import java.time.LocalDateTime

data class Status(
        val running: String,
        @Json("sensor ain") val sensorAin: String,
        @Json("interval") val logInterval: Long,
        @Json("entries") val logEntries: Int,
        @Json("latest") val latestEntryDateString: String,
        val temperature: String
) {
    constructor(running: String, sensorAin: String, logInterval: Long, logEntries: Int, latestEntryDate: LocalDateTime, temperature: String)
            : this(running, sensorAin, logInterval, logEntries, DateTimeConverter.instance.toString(latestEntryDate), temperature)

    @Json(ignored = true)
    val latestEntryDate = DateTimeConverter.instance.toDateTimeOrNull(latestEntryDateString)
            ?: throw IllegalArgumentException("Could not parse datetime from given string!")
}