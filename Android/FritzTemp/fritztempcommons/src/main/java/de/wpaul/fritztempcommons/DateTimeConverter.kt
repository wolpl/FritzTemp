package de.wpaul.fritztempcommons

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateTimeConverter {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    private val formatterFallback = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    @TypeConverter
    fun toDateTime(s: String): LocalDateTime = try {
        LocalDateTime.parse(s, formatter)
    } catch (e: DateTimeParseException) {
        LocalDateTime.parse(s, formatterFallback)
    }

    fun toDateTimeOrNull(s: String?): LocalDateTime? =
            if (s == null)
                null
            else
                try {
                    toDateTime(s)
                } catch (e: Throwable) {
                    null
                }

    @TypeConverter
    fun toString(d: LocalDateTime): String = d.format(formatter)

    companion object {
        val instance = DateTimeConverter()
    }
}