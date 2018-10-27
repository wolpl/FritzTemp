package de.wpaul.fritztempcommons

import com.beust.klaxon.Json

data class Config(val logPath: String, var interval: String, @Json(name = "ain") var sensor: String)