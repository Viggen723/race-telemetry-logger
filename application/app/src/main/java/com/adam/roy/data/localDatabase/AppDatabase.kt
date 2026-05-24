package com.adam.roy.data.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.adam.roy.data.localDatabase.dao.AccelerationRunDao
import com.adam.roy.data.localDatabase.dao.TelemetryDao
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry
import com.adam.roy.data.localDatabase.entities.DateParentEntry
import com.adam.roy.data.localDatabase.entities.TelemetryEntry
import com.adam.roy.data.localDatabase.utils.Converters

@Database(entities = [AccelerationRunEntry::class, DateParentEntry::class, TelemetryEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()
{

    abstract fun accelerationRunDao(): AccelerationRunDao
    abstract fun telemetryDao(): TelemetryDao

    // Makes the database a singleton
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase
        {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "race_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
