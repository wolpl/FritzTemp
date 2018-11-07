package de.wpaul.fritztempcommons

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Measurement::class], version = 1, exportSchema = false)
@TypeConverters(DateTimeConverter::class, DateConverter::class)
abstract class MeasurementsDB : RoomDatabase() {

    abstract fun measurementsDao(): MeasurementsDao

    companion object {
        fun create(context: Context, name: String): MeasurementsDB =
                Room.databaseBuilder(context, MeasurementsDB::class.java, name).build()

        fun createInMemory(context: Context): MeasurementsDB =
                Room.inMemoryDatabaseBuilder(context, MeasurementsDB::class.java).build()
    }
}