package de.wpaul.fritztempcommons

import com.beust.klaxon.Json

data class Status(
        val running: String,
        @Json("sensor ain") val sensorAin: String,
        @Json("log interval") val logInterval: Long,
        @Json("log entries") val logEntries: Int,
        val temperature: String
)