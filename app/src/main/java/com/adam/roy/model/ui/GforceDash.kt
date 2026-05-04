package com.adam.roy.model.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log.d
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adam.roy.MainActivity
import com.adam.roy.R
import com.adam.roy.utils.UDPController
import com.adam.roy.utils.DataTools
import com.adam.roy.model.Telemetry
import kotlin.concurrent.thread
import kotlin.jvm.java

class GforceDash : AppCompatActivity() {
    private lateinit var ggCircle: ImageView
    private var scaler = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_gforce_dash)

        ggCircle = findViewById(R.id.ggCircle)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent(this@GforceDash, MainActivity::class.java).also {
                    startActivity(it)
                    onDestroy()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        thread {

            while (true) {
                var RawCurrentRun = UDPController.receive()
                var currentRun = DataTools.parseBinaryData(RawCurrentRun)

                runOnUiThread {

                    if (currentRun != null) {
                        ggCircle.translationX = currentRun.accelX * scaler
                        ggCircle.translationY = currentRun.accelY * scaler
                    }

                }
            }
        }
    }
}