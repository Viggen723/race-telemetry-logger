package com.adam.roy.data.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adam.roy.data.localDatabase.dao.AccelerationRunDao
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry

@Database(entities = [AccelerationRunEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accelerationRunDao(): AccelerationRunDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the instance is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "race_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}