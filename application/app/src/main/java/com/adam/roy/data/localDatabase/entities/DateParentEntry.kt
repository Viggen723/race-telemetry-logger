package com.adam.roy.data.localDatabase.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "date_table")
data class DateParentEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateRan: Date = Date()
)