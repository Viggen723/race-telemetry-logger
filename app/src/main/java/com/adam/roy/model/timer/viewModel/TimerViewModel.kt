package com.adam.roy.model.timer.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.adam.roy.TelemetryApplication
import com.adam.roy.data.State
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry
import com.adam.roy.model.Telemetry
import com.adam.roy.utils.AccelerationTimer
import com.adam.roy.utils.DataTools
import com.adam.roy.utils.UDPController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as TelemetryApplication).repository

    val state = MutableLiveData<State>(State.WAITING)
    val speedDisplay = MutableLiveData<String>()
    val timeDisplay = MutableLiveData<String>()
    val isTargetInputVisible = MutableLiveData(true)
    val runSavedEvent = MutableLiveData(false)

    private var speedTarget = 60.0
    private var isSaved = false
    private var startTime = 0
    private val timer = AccelerationTimer(60.0, 1.0)

    init {
        startTelemetryLoop()
    }

    private fun startTelemetryLoop() {
        try
        {
            viewModelScope.launch(Dispatchers.IO) {

                // Again, use isActive to prevent race condition
                while (isActive) {

                    val rawTelemetry = UDPController.receive()
                    val parsedData = DataTools.parseBinaryData(rawTelemetry)

                    parsedData?.let { data ->
                        timer.update(data.speed, data.time)
                        updateUI(data)
                    }
                }

            }
        }
        finally
        {
            UDPController.closeSocket()
        }

    }

    private fun updateUI(data: Telemetry) {
        val currentState = timer.getCurrentState()
        state.postValue(currentState)

        if (currentState == State.RUNNING) {
            if (startTime == 0) startTime = data.time
            isTargetInputVisible.postValue(false)

            val speedMph = data.speed * 1.15078
            speedDisplay.postValue(String.format("%.2f MPH", speedMph))

            val elapsed = (data.time - startTime) / 1000.0
            timeDisplay.postValue(String.format("%.2f Seconds", elapsed))

        } else if (currentState == State.FINISHED && !isSaved) {
            saveRun(timer.getFinalTime())
            speedDisplay.postValue("$speedTarget mph reached!")
        }
    }

    fun reset(newTarget: Double) {
        timer.reset()
        timer.setTarget(newTarget)
        speedTarget = newTarget
        startTime = 0
        isSaved = false
        state.value = State.WAITING
        isTargetInputVisible.value = true
    }

    private fun saveRun(finalTime: Double) {
        isSaved = true
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRun(
                AccelerationRunEntry(
                    completionTime = finalTime,
                    dateRan = System.currentTimeMillis(),
                    vehicleUsed = "Test",
                    targetSpeed = speedTarget
                )
            )
            runSavedEvent.postValue(true)
        }
    }
}