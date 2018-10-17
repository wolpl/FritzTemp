package de.wpaul.fritztempcommons

import com.beust.klaxon.Json

data class Status(
        val running: String,
        @Json("log file") val logFile: String,
        @Json("log file size") val logFileSize: String,
        @Json("sensor ain") val sensorAin: String,
        @Json("log interval") val logInterval: String,
        @Json("log entries") val logEntries: String,
        val temperature: String
)