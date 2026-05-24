package com.adam.roy.data.localDatabase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.adam.roy.data.localDatabase.entities.DateParentEntry
import com.adam.roy.data.localDatabase.entities.GroupRunTelemetryByDate
import com.adam.roy.data.localDatabase.entities.TelemetryEntry
import java.util.Date

@Dao
interface TelemetryDao
{
    @Query("SELECT * FROM telemetry_table ORDER BY timeStamp ASC")
    fun getAllRuns(): List<TelemetryEntry>

    @Transaction // So Room know it is from a parent table
    @Query("SELECT * FROM date_table WHERE dateRan = :dateId")
    suspend fun getAllGroupedTelemetry(dateId: Date): List<GroupRunTelemetryByDate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(entry: TelemetryEntry)

    @Insert()
    suspend fun insertTelemetryList(list: List<TelemetryEntry>)

    @Insert()
    suspend fun insertDate(date: DateParentEntry): Long

    @Delete
    suspend fun deleteRun(entry: TelemetryEntry)

    @Delete
    suspend fun deleteAll(list: List<TelemetryEntry>)
}