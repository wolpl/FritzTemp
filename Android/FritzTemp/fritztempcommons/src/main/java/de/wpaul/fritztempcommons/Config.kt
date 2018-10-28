package de.wpaul.fritztempcommons

import com.beust.klaxon.Json

data class Config(var interval: String, @Json(name = "ain") var sensor: String)