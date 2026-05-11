package com.adam.roy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.adam.roy.model.Settings
import kotlin.concurrent.thread
import com.adam.roy.utils.DataTools
import com.adam.roy.model.gForceMeter.ui.GforceDash
import com.adam.roy.model.timer.ui.Timer0to60
import com.adam.roy.utils.UDPController
import com.google.android.material.navigation.NavigationView
// TODO Change the activities such as settings to fragments since it is far too slow

class MainActivity : AppCompatActivity() {
    private lateinit var longitudeText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var speedText: TextView
    private lateinit var elapsedTimeText: TextView
    private lateinit var drawerLayout: DrawerLayout

    @Volatile
    private var isRunning = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        longitudeText = findViewById(R.id.longitudeValueText)
        latitudeText = findViewById(R.id.latitudeValueText)
        elapsedTimeText = findViewById(R.id.elapsedTimeText)
        speedText = findViewById(R.id.speedValueText)
        drawerLayout = findViewById(R.id.drawer_layout)

        val navView: NavigationView = findViewById(R.id.nav_view)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle =
            ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
            drawerLayout.addDrawerListener(toggle)
            toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navDrawerTimer -> {
                    startActivity(Intent(this, Timer0to60::class.java))
                    UDPController.closeSocket()
                    onDestroy()
                }
                R.id.navDrawerGforce -> {
                    startActivity(Intent(this, GforceDash::class.java))
                    UDPController.closeSocket()
                    onDestroy()
                }
                R.id.navDrawerSettings -> {
                    startActivity(Intent(this, Settings::class.java))
                    UDPController.closeSocket()
                    onDestroy()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

            thread {
                while (isRunning) {
                    val rawData = UDPController.receive()

                    // UDPController returns an empty array if fails so check if empty or not
                    if (rawData.isNotEmpty()) {
                        val parsedData = DataTools.parseBinaryData(rawData)

                        if (parsedData != null) {
                            runOnUiThread {
                                // Check if activity is still there before
                                if (!isFinishing) {
                                    longitudeText.text = "Long: %.6f°".format(parsedData.longitude)
                                    latitudeText.text = "Lat: %.6f°".format(parsedData.latitude)
                                    speedText.text = "%.2f mph".format(parsedData.speed * 1.15078)
                                    elapsedTimeText.text = "${parsedData.time} ms"
                                }
                            }
                        }
                    }
                    Thread.sleep(10)
                }
            }
    }
}