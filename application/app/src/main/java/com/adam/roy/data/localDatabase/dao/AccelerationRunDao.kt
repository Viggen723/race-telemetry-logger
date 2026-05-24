package com.adam.roy.data.localDatabase.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface AccelerationRunDao
{
    // Use suspend so the app can continue to run when the function is called
    @Query("SELECT * FROM acceleration_runs ORDER BY date_ran DESC")
    fun getAll(): List<AccelerationRunEntry>

    @Query("SELECT * FROM acceleration_runs ORDER BY completion_time ASC LIMIT 10")
    fun getTop10Runs(): Flow<List<AccelerationRunEntry>> // Cannot have suspend with flow

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: AccelerationRunEntry): Long // Returns a long for the newly generated ID

    @Delete
    fun deleteRun(run: AccelerationRunEntry)

    @Delete
    fun deleteAll(list: List<AccelerationRunEntry>)

}