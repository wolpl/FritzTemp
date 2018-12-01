package de.wpaul.fritztempcommons

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateConverter {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @TypeConverter
    fun toDateTime(s: String): LocalDate = LocalDate.parse(s, formatter)

    @Suppress("unused")
    fun toDateTimeOrNull(s: String?): LocalDate? =
            if (s == null)
                null
            else
                try {
                    toDateTime(s)
                } catch (e: Throwable) {
                    null
                }

    @TypeConverter
    fun toString(d: LocalDate): String = d.format(formatter)

    companion object {
        val instance = DateConverter()
    }
}