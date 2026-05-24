package com.adam.roy.data.localDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "telemetry_table")
data class TelemetryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateId: Long,
    val timeStamp: Int?,
    val accelX: Float?,
    val accelY: Float?,
    val accelZ: Float?,
    val latitude: Float?,
    val longitude: Float?,
    val speed: Float?
)
