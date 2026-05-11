package com.adam.roy.utils

import java.net.DatagramPacket
import java.net.DatagramSocket

object UDPController {
    private const val PORT = 4120
    private var socket: DatagramSocket? = null

    fun receive(): ByteArray {
        val buffer = ByteArray(1024)
        val packet = DatagramPacket(buffer, buffer.size)

        try {
            if (socket == null || socket!!.isClosed) {
                socket = DatagramSocket(PORT)
            }

            socket?.receive(packet)
            return buffer

        } catch (e: Exception) {
            return ByteArray(0)
        }
    }

    fun closeSocket() {
        try {
            socket?.close()
        } catch (e: Exception) {
        }
    }
}