package com.adam.roy.model.gForceMeter.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adam.roy.utils.DataTools
import com.adam.roy.utils.UDPController
import kotlin.concurrent.thread
import kotlin.let

class GForceDashViewModel : ViewModel() {
    private var scaler = 25f
    val moveX = MutableLiveData(0f)
    val moveY = MutableLiveData(0f)

    // Had to use the previous raw thread instead of a coroutine because of strange lag
    @Volatile
    private var isRunning = true

    init {
        thread() {
            try {
                while (isRunning) {
                    val raw = UDPController.receive()
                    val run = DataTools.parseBinaryData(raw)

                    run?.let {
                        moveX.postValue(run.accelX * scaler)
                        moveY.postValue(run.accelY * scaler)
                    }

                }
            } finally {
                UDPController.closeSocket()
            }
        }
    }
}
