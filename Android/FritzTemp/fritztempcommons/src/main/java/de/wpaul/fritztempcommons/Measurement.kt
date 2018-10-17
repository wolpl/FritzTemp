package de.wpaul.fritztempcommons

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "measurements")
data class Measurement(
        @ColumnInfo(name = "temperature") val temperature: Float,
        @ColumnInfo(name = "timestamp") val date: Date = Calendar.getInstance().time,
        @ColumnInfo(name = "sensor") val sensor: String? = null,
        @PrimaryKey(autoGenerate = true) var id: Int? = null
) {
    override fun toString(): String = "${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.ROOT).format(date)};$temperature"
}