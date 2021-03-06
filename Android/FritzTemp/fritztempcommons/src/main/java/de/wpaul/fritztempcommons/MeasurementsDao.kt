package de.wpaul.fritztempcommons

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate
import java.time.LocalDateTime


@Dao
abstract class MeasurementsDao {
    @Transaction
    @Query("select * from measurements order by timestamp")
    abstract fun getAll(): List<Measurement>?

    @Query("SELECT distinct sensor from measurements order by sensor")
    abstract fun getSensors(): LiveData<List<String>?>

    @Query("select * from measurements where sensor in (:sensors) order by sensor,timestamp")
    abstract fun getFromSensors(sensors: List<String>): LiveData<List<Measurement>?>

    @Transaction
    @Query("select * from measurements where timestamp >= :d and sensor in (:sensors) order by timestamp")
    abstract fun getAllAfterDateTimeFromSensors(d: LocalDateTime, sensors: List<String>): List<Measurement>?

    @Transaction
    @Query("select * from measurements where timestamp >= :d order by timestamp")
    abstract fun getAllAfterDateTime(d: LocalDateTime): List<Measurement>?

    @Query("select min(temperature) from measurements where date(timestamp)=date(:day)")
    abstract fun getMinTempAtDay(day: LocalDate): LiveData<Float?>

    @Query("select max(temperature) from measurements where date(timestamp)=date(:day)")
    abstract fun getMaxTempAtDay(day: LocalDate): LiveData<Float?>

    @Query("select * from measurements where timestamp=(select min(timestamp) from measurements)")
    abstract fun getOldestEntry(): LiveData<Measurement?>

    @Query("select * from measurements where timestamp=(select max(timestamp) from measurements)")
    abstract fun getYoungestEntryLive(): LiveData<Measurement?>

    @Query("select * from measurements where timestamp=(select max(timestamp) from measurements)")
    abstract fun getYoungestEntry(): Measurement?

    @Query("select date(timestamp) as day,min(temperature)as min,max(temperature) as max,avg(temperature) as avg from measurements where timestamp>=:earliest group by date(timestamp) order by date(timestamp)")
    abstract fun getMinMaxAverageTemperatureByDaySince(earliest: LocalDate): List<MinMaxAvgTemperatureElement>?

    @Query("select date(timestamp) as day,min(temperature)as min,max(temperature) as max,avg(temperature) as avg from measurements group by date(timestamp) order by date(timestamp)")
    abstract fun getMinMaxAverageTemperatureByDay(): List<MinMaxAvgTemperatureElement>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg m: Measurement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(ms: List<Measurement>)

    @Query("select count(*) from measurements")
    abstract fun countAllLive(): LiveData<Int>

    @Query("select count(*) from measurements")
    abstract fun countAll(): Int

    @Query("select distinct count(*) from measurements")
    abstract fun countAllDistinctLive(): LiveData<Int>

    @Query("select distinct count(*) from measurements")
    abstract fun countAllDistinct(): Int

    @Query("select avg(temperature) from measurements")
    abstract fun getAverageTemperature(): LiveData<Float?>

    @Query("delete from measurements")
    abstract fun deleteAll()

    @Query("delete from measurements where id not in (select min(id)  from measurements group by timestamp,temperature,sensor)")
    abstract fun deleteDuplicates()

    @Transaction
    open fun replaceAllData(newData: List<Measurement>, deleteDuplicates: Boolean = false) {
        deleteAll()
        insert(newData)
        if (deleteDuplicates) deleteDuplicates()
    }
}