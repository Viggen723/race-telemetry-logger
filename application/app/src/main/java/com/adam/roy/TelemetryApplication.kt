package com.adam.roy

import android.app.Application
import com.adam.roy.data.localDatabase.AppDatabase
import com.adam.roy.data.repository.RawRunRepository
import com.adam.roy.features.timer.data.TimerRepository

class TelemetryApplication: Application()
{
    private val db by lazy { AppDatabase.Companion.getDatabase(this) }

    private val timerDao by lazy { db.accelerationRunDao() }
    val timerRepository by lazy { TimerRepository(timerDao) }

    private val telemetryDao by lazy { db.telemetryDao() }
    val telemetryRepository by lazy { RawRunRepository(telemetryDao)}



}