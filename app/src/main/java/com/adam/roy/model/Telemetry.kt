package com.adam.roy.model

data class Telemetry(
    val time: Int,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val latitude: Float,
    val longitude: Float,
    val speed: Float
)
