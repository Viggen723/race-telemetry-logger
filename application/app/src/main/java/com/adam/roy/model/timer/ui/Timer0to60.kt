package com.adam.roy.model.timer.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.adam.roy.MainActivity
import com.adam.roy.R
import com.adam.roy.data.State
import com.adam.roy.databinding.ActivityTimer0to60Binding
import com.adam.roy.model.timer.viewModel.TimerViewModel


class Timer0to60 : AppCompatActivity() {
    private lateinit var viewModel: TimerViewModel
    private lateinit var binding: ActivityTimer0to60Binding // Using binding removes the annoying list of findViewById

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimer0to60Binding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TimerViewModel::class.java]

        binding.resetButton.setOnClickListener {
            val target = binding.setSpeedTargetText.text.toString().toDoubleOrNull() ?: 60.0
            viewModel.reset(target)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.drawerFragmentContainer, Top10RunsFragment())
            .commit()

        viewModel.state.observe(this) { state ->
            binding.currentStatusText.text = state.toString()
            updateTextColor(state)
        }

        viewModel.speedDisplay.observe(this) { binding.currentSpeedText.text = it }
        viewModel.timeDisplay.observe(this) { binding.elapsedTimeText.text = it }

        viewModel.isTargetInputVisible.observe(this) { visible ->
            binding.setSpeedTargetText.visibility = if (visible) View.VISIBLE else View.GONE
        }

        viewModel.runSavedEvent.observe(this) { saved ->
            if (saved) Toast.makeText(this, "Run Saved! Swipe left to view", Toast.LENGTH_SHORT).show()
        }

        val callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@Timer0to60, MainActivity::class.java))
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }

    private fun updateTextColor(state: State) {
        val color = when (state) {
            State.WAITING -> R.color.WAITING_yellow
            State.READY -> R.color.READY_green
            State.RUNNING -> R.color.RUNNING_blue
            State.FINISHED -> R.color.FINISHED_red
        }
        binding.currentStatusText.setTextColor(ContextCompat.getColor(this, color))
    }
}