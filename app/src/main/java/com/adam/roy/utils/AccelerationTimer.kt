package com.adam.roy.utils

import java.util.Objects.toString
import com.adam.roy.data.State

class AccelerationTimer(private val targetSpeed: Double, private val startThreshold: Double)
{
    private var currentStatus = State.WAITING

    private var finalTime: Double = 0.0
    private var startTime = 0
    private var lastTime = 0
    private var lastSpeed = 0.0

    fun update(currentSpeed: Float, timeStamp: Int) {
        val currentMph = currentSpeed * 1.15077945

        when (currentStatus) {
            State.WAITING -> {
                if (currentMph < startThreshold)
                {
                    currentStatus = State.READY
                }
            }

            State.READY -> {
                if (currentMph >= startThreshold)
                {
                    currentStatus = State.RUNNING
                    startTime = timeStamp
                }
            }

            State.RUNNING -> {
                if (currentMph >= targetSpeed)
                {
                    finalTime = interpolate(lastTime, timeStamp, lastSpeed, currentMph)
                    currentStatus = State.FINISHED
                }
            }

            State.FINISHED -> {}
        }
        lastTime = timeStamp
        lastSpeed = currentMph
    }

        fun interpolate(time1: Int, time2: Int, speed1: Double, speed2: Double): Double {

            val secondsToLastRead = (time1 - startTime) / 1000.0

            val gap = (time2 - time1) / 1000.0

            val speedNeeded = targetSpeed - speed1
            val speedGained = speed2 - speed1

            // Checking if the reading from the GPS would result in division by 0 or a negative
            if (speedGained <= 0)
            {
                return secondsToLastRead
            }

            val percentageOfGap = speedNeeded / speedGained

            return secondsToLastRead + (percentageOfGap * gap)
        }

    fun reset()
    {
        currentStatus = State.WAITING
        finalTime = 0.0
        startTime = 0
        lastTime = 0
        lastSpeed = 0.0
    }

    fun getCurrentState() : State
    {
        return currentStatus
    }

    fun getFinalTime() : Double
    {
        return finalTime
    }
}