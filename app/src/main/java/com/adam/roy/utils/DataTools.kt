package com.adam.roy.utils

import com.adam.roy.model.Telemetry
import java.nio.ByteBuffer
import java.nio.ByteOrder

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
                buffer.float
            )

        } catch (e: Exception) {
            null
        }


    }

}