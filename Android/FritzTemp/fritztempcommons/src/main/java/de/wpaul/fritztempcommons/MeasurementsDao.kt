package de.wpaul.fritztempcommons

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.*


@Dao
interface MeasurementsDao {
    @Query("select * from measurements")
    fun getAll(): List<Measurement>

    @Query("SELECT distinct sensor from measurements")
    fun getSensors(): List<String>

    @Query("select * from measurements where sensor in (:sensors)")
    fun getFromSensors(sensors: List<String>): List<Measurement>

    @Query("select * from measurements where timestamp >= :d and sensor in (:sensors)")
    fun getAllAfterDateFromSensors(d: Date, sensors: List<String>): List<Measurement>

    @Query("select * from measurements where timestamp >= :d")
    fun getAllAfterDate(d: Date): List<Measurement>


    @Query("select date(timestamp) as day,min(temperature)as min,max(temperature) as max,avg(temperature) as avg from measurements where timestamp>=:earliest group by date(timestamp)")
    fun getMinMaxAverageTemperatureByDaySince(earliest: Date): List<MinMaxAvgTemperatureElement>

    @Insert
    fun insert(vararg m: Measurement)

    @Insert
    fun insert(ms: List<Measurement>)

    @Query("select avg(temperature) from measurements")
    fun getAverageTemperature(): Float

    @Query("delete  from measurements")
    fun deleteAll()
}