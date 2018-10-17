package de.wpaul.fritztempcommons

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.*

class DateConverter {

    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT)
    private val formatShort = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

    @TypeConverter
    fun toDate(s: String): Date = try {
        format.parse(s)
    } catch (e: Throwable) {
        formatShort.parse(s)
    }

    @TypeConverter
    fun toString(d: Date): String = format.format(d)

}