package com.adam.roy

import android.app.Application
import com.adam.roy.data.localDatabase.AppDatabase
import com.adam.roy.model.timer.data.TimerRepository

class TelemetryApplication: Application()
{
    private val db by lazy { AppDatabase.Companion.getDatabase(this) }
    private val dao by lazy { db.accelerationRunDao() }
    val repository by lazy { TimerRepository(dao) }
}