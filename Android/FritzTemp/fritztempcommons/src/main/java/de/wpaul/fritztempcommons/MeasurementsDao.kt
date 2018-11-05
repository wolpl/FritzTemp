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

    @Query("select min(temperature) from measurements where date(timestamp)=date(:day)")
    fun getMinTempAtDay(day: Date): Float

    @Query("select max(temperature) from measurements where date(timestamp)=date(:day)")
    fun getMaxTempAtDay(day: Date): Float

    @Query("select * from measurements where timestamp=(select min(timestamp) from measurements)")
    fun getOldestEntry(): Measurement

    @Query("select * from measurements where timestamp=(select max(timestamp) from measurements)")
    fun getYoungestEntry(): Measurement

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

    @Query("delete from measurements where id not in (select min(id)  from measurements group by timestamp,temperature,sensor)")
    fun deleteDuplicates()
}