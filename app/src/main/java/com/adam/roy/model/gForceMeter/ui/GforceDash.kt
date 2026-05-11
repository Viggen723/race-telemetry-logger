package com.adam.roy.model.gForceMeter.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.adam.roy.MainActivity
import com.adam.roy.R
import com.adam.roy.databinding.ActivityGforceDashBinding
import com.adam.roy.model.gForceMeter.viewModel.GForceDashViewModel
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

        viewModel.moveX.observe(this)
        {
            binding.ggCircle.translationX = it
        }

        viewModel.moveY.observe(this)
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