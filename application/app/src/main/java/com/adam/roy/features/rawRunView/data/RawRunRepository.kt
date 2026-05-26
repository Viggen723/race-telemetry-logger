package com.adam.roy.data.repository

import com.adam.roy.data.localDatabase.dao.TelemetryDao
import com.adam.roy.data.localDatabase.entities.DateParentEntry
import com.adam.roy.data.localDatabase.entities.TelemetryEntry
import com.adam.roy.data.localDatabase.entities.GroupRunTelemetryByDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

class RawRunRepository(private val dao: TelemetryDao)
{
    suspend fun getAllGroupedTelemetry(dateId: Date): List<GroupRunTelemetryByDate>
    {
        return withContext(Dispatchers.IO)
        {
            dao.getAllGroupedTelemetry(dateId)
        }
    }

    suspend fun saveRecordedRun(parentDate: DateParentEntry, telemetryList: List<TelemetryEntry>)
    {
        withContext(Dispatchers.IO)
        {
            val parentId = dao.insertDate(parentDate)

            val runsList = telemetryList.map { list ->
                list.copy(dateId = parentId)
            }
            dao.insertTelemetryList(runsList)
        }
    }
}