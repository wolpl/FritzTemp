package de.wpaul.fritztempcommons

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Entity(tableName = "measurements")
data class Measurement(
        @ColumnInfo(name = "temperature") val temperature: Float,
        @ColumnInfo(name = "timestamp", index = true) val timestamp: LocalDateTime = LocalDateTime.now(),
        @ColumnInfo(name = "sensor") val sensor: String? = null,
        @PrimaryKey(autoGenerate = true) var id: Int? = null
) {
    override fun toString(): String = "${DateTimeConverter.instance.toString(timestamp)};$temperature;$sensor"
    fun getLocalString(): String = "${DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(timestamp)}: $temperature°C"

    companion object {
        private val dateConverter = DateTimeConverter()

        fun parse(s: String): Measurement {
            val temperature = s.split(";")[1].toFloat()
            val dateString = s.split(";")[0]
            val date = try {
                DateTimeConverter.instance.toDateTime(dateString)
            } catch (e: Throwable) {
                dateConverter.toDateTime(dateString)
            }
            var sensor: String? = s.split(";")[2]
            if (sensor.isNullOrEmpty()) sensor = null
            return Measurement(temperature, date, sensor)
        }
    }
}