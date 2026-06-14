package com.adam.roy.utils

import com.adam.roy.features.EulerAngles
import com.adam.roy.features.Telemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2

object DataTools {

    fun parseBinaryData(data: ByteArray) : Telemetry? {

        // Adding up the data sent from esp32 and checking if more or less


        // Byte buffer allows us to move through the bytes, keeping a position on each read
        // .long, .double, .float are the things to call to go through each set of bytes
        val buffer = ByteBuffer.wrap(data).apply {

            // The reordering is needed as the esp32 sends the byte array in little endian
            // ByteBuffer defaults to big endian
            order(ByteOrder.LITTLE_ENDIAN)
        }

        // NOTE: When parsing the data, the size of a Kotlin long is different from a C++ long on esp32
        // Using integer for time instead of long (Too large) Consider changing esp32 side to long long
        return try {
            Telemetry(
                buffer.int,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float,
                buffer.float
                )

        } catch (e: Exception) {
            null
        }
    }

    fun convertToEuler(qw: Float, qi: Float, qj: Float, qk: Float): EulerAngles
    {
        // Using the formulas to convert a quaternion to Euler angles
        val pitchTemp: Double = 2.0 * (qw * qj - qk * qi)

        val roll: Double
        val pitch: Double
        val yaw: Double

        // Make sure there is no arcsin ops that are not acceptable
        if (pitchTemp <= -0.999999) // Straight up
        {
            pitch = -Math.PI / 2.0
            roll = 0.0
            yaw = -2.0 * atan2(qi, qw)
        }
        else if (pitchTemp >= 0.999999) // Straight down
        {
            pitch = Math.PI / 2.0
            roll = 0.0
            yaw = 2.0 * atan2(qi, qw)
        }
        else // Normal
        {
            roll = atan2((2.0 * (qw * qi + qj * qk)), 1.0 - 2.0 * (qi * qi + qj * qj))
            pitch = asin(pitchTemp)
            yaw = atan2((2.0 * (qw * qk + qi * qj)), 1.0 - 2.0 * (qj * qj + qk * qk))
        }

        // The values are in radians so now convert to degrees

        return EulerAngles(Math.toDegrees(roll).toFloat(), Math.toDegrees(pitch).toFloat(), Math.toDegrees(yaw).toFloat())
    }

}