package com.adam.roy.data.localDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TelemetryEntry(
    @PrimaryKey val id: Int?,
    val timeStamp: Int?,
    val accelX: Float?,
    val accelY: Float?,
    val accelZ: Float?,
    val latitude: Float?,
    val longitude: Float?
)
