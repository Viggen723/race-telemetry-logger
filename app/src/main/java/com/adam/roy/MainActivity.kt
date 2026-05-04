package com.adam.roy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.adam.roy.model.Settings
import kotlin.concurrent.thread
import com.adam.roy.utils.DataTools
import com.adam.roy.model.ui.GforceDash
import com.adam.roy.model.ui.Timer0to60
import com.adam.roy.utils.UDPController
import com.google.android.material.navigation.NavigationView
// TODO Change the activities such as settings to fragments since it is far too slow

class MainActivity : AppCompatActivity() {
    private lateinit var longitudeText: TextView
    private lateinit var latitudeText: TextView
    private lateinit var speedText: TextView
    private lateinit var elapsedTimeText: TextView
    private lateinit var drawerLayout: DrawerLayout

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
                    onDestroy()
                }
                R.id.navDrawerGforce -> {
                    startActivity(Intent(this, GforceDash::class.java))
                    onDestroy()
                }

                R.id.navDrawerSettings -> {
                    startActivity(Intent(this, Settings::class.java))
                    onDestroy()
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        thread {

            while (true) {

                var rawData = UDPController.receive()
                var parsedData = DataTools.parseBinaryData(rawData)

                runOnUiThread {

                    if (parsedData != null) {
                        longitudeText.text = "Long: %.6f degrees".format(parsedData.longitude)
                        latitudeText.text = "Lat: %.6f degrees".format(parsedData.latitude)
                        speedText.text = "%.2f mph".format(parsedData.speed * 1.15078)
                        elapsedTimeText.text = "${parsedData.time} ms"
                    }
                }
            }
        }
    }
}