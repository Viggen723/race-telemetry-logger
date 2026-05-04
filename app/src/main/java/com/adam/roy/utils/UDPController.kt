package com.adam.roy.utils

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object UDPController {

    var ip = InetAddress.getByName("192.168.4.2")
    var port = 4120

    fun receive() : ByteArray {

        val socket = DatagramSocket(port)

        val buffer = ByteArray(1024)
        val packet = DatagramPacket(buffer, buffer.size)

        socket.receive(packet)
        socket.close()

        return buffer
    }
}