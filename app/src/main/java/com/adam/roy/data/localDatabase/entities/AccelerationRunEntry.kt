package com.adam.roy.data.localDatabase.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "acceleration_runs")
data class AccelerationRunEntry(
    @PrimaryKey(autoGenerate = true)
    val runid: Int = 0, // Initalization to 0 is needed for Room to know to generate the next ID

    @ColumnInfo("date_ran") val dateRan: Long, // Use Unix timestamp and make it searchable
    @ColumnInfo("target_speed") val targetSpeed: Double?,
    @ColumnInfo("completion_time") val completionTime: Double?,
    @ColumnInfo ("vehicle_used") val vehicleUsed: String?
    )