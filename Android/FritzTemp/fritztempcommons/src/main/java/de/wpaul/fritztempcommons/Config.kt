package de.wpaul.fritztempcommons

import com.beust.klaxon.Json

open class Config(sensor: String? = null, interval: Long = defaultInterval) {
    companion object {
        const val defaultInterval = 60000L
    }

    @Json(name = "ain")
    var sensor = sensor
        set(value) {
            field = value
            onConfigChanged(this::sensor.name)
        }
    var interval = interval
        set(value) {
            require(value >= 0) { throw IllegalArgumentException("Could not parse an interval string to an integer!") }
            field = value
            onConfigChanged(this::interval.name)
        }
    @Json(ignored = true)
    val onConfigChanged = Event<String>()

    operator fun get(s: String): String? {
        return when (s) {
            this::sensor.name -> sensor
            this::interval.name -> interval.toString()
            else -> null
        }
    }

    operator fun set(s: String, value: String) {
        when (s) {
            this::sensor.name -> sensor = value
            this::interval.name -> interval = value.toLong()
            else -> throw java.lang.IllegalArgumentException("Field name not found: $s")
        }
    }

    fun hasProperty(p: String) = p == this::interval.name || p == this::sensor.name
}