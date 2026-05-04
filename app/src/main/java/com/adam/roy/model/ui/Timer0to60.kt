package com.adam.roy.model.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.adam.roy.MainActivity
import com.adam.roy.R
import com.adam.roy.utils.*
import com.adam.roy.data.State
import java.util.Objects.toString
import kotlin.concurrent.thread


class Timer0to60 : AppCompatActivity() {

    //*TODO - have to make it so the user can enter the desired parameters/targets
    // The defaults for the input for the timer object.
    // Also make everything in the thread its own function
    @Volatile
    private var threadRunning = true

    private var speedTarget = 60.0
    private var speedThreshold = 1.0

    private lateinit var stateText: TextView
    private lateinit var speedText: TextView
    private lateinit var resultText: TextView
    private lateinit var elapsedTimeText: TextView
    private lateinit var resetButton: Button
    private lateinit var setSpeedTargetText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_timer0to60)

        stateText = findViewById(R.id.currentStatusText)
        speedText = findViewById(R.id.currentSpeedText)
        resultText = findViewById(R.id.resultText)
        resetButton = findViewById(R.id.resetButton)
        elapsedTimeText = findViewById(R.id.elapsedTimeText)
        setSpeedTargetText = findViewById(R.id.setSpeedTargetText)

        var timer = AccelerationTimer(speedTarget, speedThreshold)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent(this@Timer0to60, MainActivity::class.java).also {
                    startActivity(it)
                    onDestroy()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        resetButton.setOnClickListener {
            timer.reset()
            stateText.text = timer.getCurrentState().toString()
            resultText.text = ""
            speedText.text = ""
            elapsedTimeText.text = ""

            //TODO Also encapsulate this
            setSpeedTargetText.visibility = View.VISIBLE

            var speedTargetString = ""
            try
            {
                speedTargetString = setSpeedTargetText.text.toString()
                speedTarget = speedTargetString.toDouble()
            }
            catch (e: Exception)
            {
                Toast.makeText(this@Timer0to60, "Target was not set. Setting to 60mph default", Toast.LENGTH_SHORT).show()
            }

            timer = AccelerationTimer(speedTarget, speedThreshold)
        }

        stateText.text = timer.getCurrentState().toString()
        changeStateTextColor(this, timer, stateText)

        thread {

            while (threadRunning) {
                val rawTelemetry = UDPController.receive()
                val parsedData = DataTools.parseBinaryData(rawTelemetry)

                if (parsedData != null) {
                    timer.update(parsedData.speed, parsedData.time)
                }

                runOnUiThread {
                    stateText.text = timer.getCurrentState().toString()
                    changeStateTextColor(this@Timer0to60, timer, stateText)

                    val startTime = parsedData!!.time
                    if (timer.getCurrentState() == State.RUNNING)
                    {
                        setSpeedTargetText.visibility = View.GONE
                        val speedMph = parsedData!!.speed * 1.15078
                        speedText.text = String.format("%.2f", speedMph) + " MPH"

                        val elapsedTime = (parsedData!!.time - startTime) / 1000.0
                        elapsedTimeText.text = String.format("%.2f", elapsedTime) + " Seconds"
                    }
                }
            }
            runOnUiThread {
                resultText.text = String.format("%.2f", timer.getFinalTime()) + " Seconds"
                stateText.setText(timer.getCurrentState().toString())
                changeStateTextColor(this@Timer0to60, timer, stateText)
            }
        }
    }

    fun changeStateTextColor(context: Context, timer: AccelerationTimer, stateText: TextView)
    {
        val color = when (timer.getCurrentState()) {
                State.WAITING -> {
                    ContextCompat.getColor(context, R.color.WAITING_yellow)
                }

                State.READY -> {
                    ContextCompat.getColor(context, R.color.READY_green)
                }

                State.RUNNING -> {
                    ContextCompat.getColor(context, R.color.RUNNING_blue)

                }
                // Need to change it as the while loop will break before this is got to
                State.FINISHED -> {
                    ContextCompat.getColor(context, R.color.FINISHED_red)
                }
            }

        stateText.setTextColor(color)
    }
}