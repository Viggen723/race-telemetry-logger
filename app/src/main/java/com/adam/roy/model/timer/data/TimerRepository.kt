package com.adam.roy.model.timer.data

import com.adam.roy.data.localDatabase.dao.AccelerationRunDao
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry

class TimerRepository(private val dao: AccelerationRunDao)
{
    val getTop10runs = dao.getTop10Runs()

    suspend fun insertRun(run: AccelerationRunEntry)
    {
        dao.insertRun(run)
    }

    suspend fun deleteAll()
    {
        val entryList = dao.getAll()
        dao.deleteAll(entryList)
    }
}