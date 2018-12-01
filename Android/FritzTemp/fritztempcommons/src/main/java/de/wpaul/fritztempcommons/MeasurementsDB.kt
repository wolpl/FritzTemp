package de.wpaul.fritztempcommons

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Measurement::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class, DateConverter::class)
abstract class MeasurementsDB : RoomDatabase() {

    lateinit var name: String
        private set

    abstract fun measurementsDao(): MeasurementsDao

    companion object {
        private val instances = mutableMapOf<String, MeasurementsDB>()
        fun get(context: Context, name: String): MeasurementsDB = instances[name]
                ?: Room.databaseBuilder(context, MeasurementsDB::class.java, name).build().apply { this.name = name }

        @Suppress("unused")
        fun createInMemory(context: Context): MeasurementsDB =
                Room.inMemoryDatabaseBuilder(context, MeasurementsDB::class.java).build().apply { this.name = name }

        fun getDefault(context: Context) = get(context, "Measurements.db")
    }
}