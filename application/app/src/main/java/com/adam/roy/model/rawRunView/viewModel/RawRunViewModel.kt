package com.adam.roy.model.rawRunView.viewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adam.roy.TelemetryApplication
import com.adam.roy.data.localDatabase.entities.DateParentEntry
import com.adam.roy.data.localDatabase.entities.TelemetryEntry
import com.adam.roy.utils.DataTools
import com.adam.roy.utils.UDPController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RawRunViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = (application as TelemetryApplication).telemetryRepository
    private val currentRunList = mutableListOf<TelemetryEntry>()
    val isActive = MutableLiveData(false)

    fun startRecordLoop() {
        isActive.value = true
        currentRunList.clear()

        viewModelScope.launch(Dispatchers.IO) {
            try
            {
                while (isActive.value == true)
                {
                    val raw = UDPController.receive()
                    val run = DataTools.parseBinaryData(raw)

                    run?.let {
                        addTelemetry(
                            run.timeStamp, run.accelX, run.accelY, run.accelZ,
                            run.latitude, run.longitude, run.speed
                        )
                    }
                }
            }
            catch (e: Exception)
            {

            }
        }
    }

    fun addTelemetry(time: Int?, accelX: Float?, accelY: Float?, accelZ: Float?,
                     latitude: Float?, longitude: Float?, speed: Float?)
    {
        currentRunList.add(TelemetryEntry(
            0, 0, time, accelX, accelY, accelZ, latitude, longitude, speed)
        )
    }

    fun stopAndSave()
    {
        isActive.postValue(false)

        viewModelScope.launch()
        {
            try {
                val todayMidnight = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val parentEntry = DateParentEntry(dateRan = todayMidnight)

                repository.saveRecordedRun(parentEntry, currentRunList)

                currentRunList.clear()
            }
            catch (e: Exception) {

            }
        }
    }

    fun generateCsvString(midnightDate: Date, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allGroupedRuns = repository.getAllGroupedTelemetry(midnightDate)

                val csvBuilder = StringBuilder()
                csvBuilder.append("runDate,timestamp,accelX,accelY,accelZ,latitude,longitude,speed\n")

                val dateFormatter = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())

                for (runGroup in allGroupedRuns)
                {
                    val parentTimeMs = runGroup.parent.dateRan.time
                    val formatted = dateFormatter.format(parentTimeMs)

                    for (item in runGroup.telemetryList)
                    {
                        csvBuilder.append("$formatted,")
                        csvBuilder.append("${item.timeStamp},")
                        csvBuilder.append("${item.accelX},")
                        csvBuilder.append("${item.accelY},")
                        csvBuilder.append("${item.accelZ},")
                        csvBuilder.append("${item.latitude},")
                        csvBuilder.append("${item.longitude},")
                        csvBuilder.append("${item.speed}\n")
                    }
                }

                onComplete(csvBuilder.toString())

            }
            catch (e: Exception)
            {

            }
        }
    }
}