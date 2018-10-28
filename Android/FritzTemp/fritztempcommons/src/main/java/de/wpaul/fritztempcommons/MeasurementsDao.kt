package de.wpaul.fritztempcommons

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.*


@Dao
interface MeasurementsDao {
    @Query("select * from measurements order by timestamp")
    fun getAll(): List<Measurement>

    @Query("SELECT distinct sensor from measurements order by sensor")
    fun getSensors(): List<String>

    @Query("select * from measurements where sensor in (:sensors) order by sensor,timestamp")
    fun getFromSensors(sensors: List<String>): List<Measurement>

    @Query("select * from measurements where timestamp >= :d and sensor in (:sensors) order by timestamp")
    fun getAllAfterDateFromSensors(d: Date, sensors: List<String>): List<Measurement>

    @Query("select * from measurements where timestamp >= :d order by timestamp")
    fun getAllAfterDate(d: Date): List<Measurement>


    @Query("select date(timestamp) as day,min(temperature)as min,max(temperature) as max,avg(temperature) as avg from measurements where timestamp>=:earliest group by date(timestamp) order by date(timestamp)")
    fun getMinMaxAverageTemperatureByDaySince(earliest: Date): List<MinMaxAvgTemperatureElement>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg m: Measurement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(ms: List<Measurement>)

    @Query("select count(*) from measurements")
    fun countAll(): Int

    @Query("select distinct count(*) from measurements")
    fun countAllDistinct(): Int

    @Query("select avg(temperature) from measurements")
    fun getAverageTemperature(): Float

    @Query("delete from measurements")
    fun deleteAll()
}