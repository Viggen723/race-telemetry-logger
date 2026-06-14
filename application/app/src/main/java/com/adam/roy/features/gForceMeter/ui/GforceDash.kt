package com.adam.roy.features.gForceMeter.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.adam.roy.MainActivity
import com.adam.roy.databinding.ActivityGforceDashBinding
import com.adam.roy.features.gForceMeter.viewModel.GForceDashViewModel
import com.adam.roy.utils.UDPController

class GforceDash : AppCompatActivity() {
    private lateinit var viewModel: GForceDashViewModel
    private lateinit var binding: ActivityGforceDashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityGforceDashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[GForceDashViewModel::class.java]

        // ** X and Y are switched as the sensors reading for the X axis are forward in backwards in current config
        viewModel.moveY.observe(this)
        {
            binding.ggCircle.translationX = it
        }

        viewModel.moveX.observe(this)
        {
            binding.ggCircle.translationY = it
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Intent(this@GforceDash, MainActivity::class.java).also {
                    startActivity(it)
                    UDPController.closeSocket()
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

    }
}