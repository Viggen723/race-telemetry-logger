package com.adam.roy.data.localDatabase.entities

import androidx.room.Embedded
import androidx.room.Relation

data class GroupRunTelemetryByDate(
    @Embedded val parent: DateParentEntry,
    @Relation(
        parentColumn = "id", // PrimaryKey of the parent entity
        entityColumn = "dateId" // Custom-made key to match up with the parent
    )
    val telemetryList: List<TelemetryEntry>
)
