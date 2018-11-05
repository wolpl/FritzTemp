package de.wpaul.fritztempcommons

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "measurements")
data class Measurement(
        @ColumnInfo(name = "temperature") val temperature: Float,
        @ColumnInfo(name = "timestamp", index = true) val date: Date = Calendar.getInstance().time,
        @ColumnInfo(name = "sensor") val sensor: String? = null,
        @PrimaryKey(autoGenerate = true) var id: Int? = null
) {
    override fun toString(): String = "${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT).format(date)};$temperature;$sensor"
    fun getLocalString(): String = "${SimpleDateFormat().format(date)}: $temperature°C"

    companion object {
        private val dateConverter = DateConverter()

        fun parse(s: String): Measurement {
            val temperature = s.split(";")[1].toFloat()
            val dateString = s.split(";")[0]
            val date = try {
                SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT).parse(dateString)
            } catch (e: Throwable) {
                dateConverter.toDate(dateString)
            }
            var sensor: String? = s.split(";")[2]
            if (sensor.isNullOrEmpty()) sensor = null
            return Measurement(temperature, date, sensor)
        }
    }
}