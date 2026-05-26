package com.adam.roy.features.mappedSession.ui

import android.app.DatePickerDialog
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adam.roy.R
import com.adam.roy.data.localDatabase.AppDatabase
import com.adam.roy.data.localDatabase.entities.DateParentEntry
import com.adam.roy.data.localDatabase.entities.GroupRunTelemetryByDate
import com.adam.roy.data.localDatabase.entities.TelemetryEntry
import com.adam.roy.data.repository.RawRunRepository
import com.adam.roy.features.Telemetry
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class MappedSessionView : AppCompatActivity(), OnMapReadyCallback {

    private val db by lazy { AppDatabase.Companion.getDatabase(this) }
    private val telemetryDao by lazy { db.telemetryDao() }
    private val telemetryRepository by lazy { RawRunRepository(telemetryDao) }

    private lateinit var sessionUploadButton: Button
    private lateinit var sessionLoadButton: Button

    private var mGoogleMap: GoogleMap? = null

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val points = getPointsFromCsv(selectedUri)

                if (points.isNotEmpty()) {
                    try {
                        // FIX: Separated Calendar initialization and .time call to
                        // explicitly assign a valid java.util.Date object
                        val calendarInstance = Calendar.getInstance()
                        calendarInstance.set(Calendar.HOUR_OF_DAY, 0)
                        calendarInstance.set(Calendar.MINUTE, 0)
                        calendarInstance.set(Calendar.SECOND, 0)
                        calendarInstance.set(Calendar.MILLISECOND, 0)
                        val todayMidnight: Date = calendarInstance.time
                        val midnightTimestamp: Long = todayMidnight.time

                        val parentEntry = DateParentEntry(midnightTimestamp)

                        // 2. Map parsed objects into formal Room database schema entities
                        val databaseEntities = points.map { uiPoint ->
                            TelemetryEntry(
                                id = 0, // Auto-generated index primary key by Room
                                dateId = midnightTimestamp,
                                timeStamp = uiPoint.timeStamp,
                                accelX = uiPoint.accelX,
                                accelY = uiPoint.accelY,
                                accelZ = uiPoint.accelZ,
                                latitude = uiPoint.latitude,
                                longitude = uiPoint.longitude,
                                speed = uiPoint.speed
                            )
                        }

                        // 3. Persist structural data securely into local storage tables
                        telemetryRepository.saveRecordedRun(parentEntry, databaseEntities)

                        withContext(Dispatchers.Main) {
                            mGoogleMap?.let { map ->
                                drawPathOnMap(map, points)
                                Toast.makeText(this@MappedSessionView, "Run imported and saved to database!", Toast.LENGTH_SHORT).show()
                            } ?: Toast.makeText(this@MappedSessionView, "Map not ready, but data is saved!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MappedSessionView, "Database save failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MappedSessionView, "No valid data found in CSV", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mapped_session_view)

        sessionUploadButton = findViewById(R.id.sessionUploadButton)
        sessionLoadButton =  findViewById(R.id.sessionLoadButton)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        sessionUploadButton.setOnClickListener {
            selectFileLauncher.launch("*/*")
        }

        // Calendar Date Picker integration block
        sessionLoadButton.setOnClickListener {
            val currentCalendar = Calendar.getInstance()
            val startYear = currentCalendar.get(Calendar.YEAR)
            val startMonth = currentCalendar.get(Calendar.MONTH)
            val startDay = currentCalendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->

                    // FIX: Changed block structure to ensure that targetCalendar.time
                    // resolves and returns cleanly as a clean java.util.Date object type
                    val targetCalendar = Calendar.getInstance()
                    targetCalendar.set(Calendar.YEAR, selectedYear)
                    targetCalendar.set(Calendar.MONTH, selectedMonth)
                    targetCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                    targetCalendar.set(Calendar.HOUR_OF_DAY, 0)
                    targetCalendar.set(Calendar.MINUTE, 0)
                    targetCalendar.set(Calendar.SECOND, 0)
                    targetCalendar.set(Calendar.MILLISECOND, 0)
                    val chosenDate: Date = targetCalendar.time

                    // Query and map background database stream
                    lifecycleScope.launch(Dispatchers.IO) {
                        val databasePoints = getPointsFromDatabase(chosenDate)

                        withContext(Dispatchers.Main) {
                            if (databasePoints.isNotEmpty())
                            {
                                mGoogleMap?.let { map ->
                                    drawPathOnMap(map, databasePoints)
                                    Toast.makeText(this@MappedSessionView, "Loaded ${databasePoints.size} points!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            else {
                                Toast.makeText(this@MappedSessionView, "No race runs recorded on this date.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                startYear,
                startMonth,
                startDay
            )
            datePickerDialog.show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        // Default location as Lime Rock Park
        val defaultLocation = LatLng(41.927335, -73.384477)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 14.5f))
    }

    private fun getPointsFromCsv(uri: Uri): List<Telemetry> {
        val points = mutableListOf<Telemetry>()
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = inputStream.bufferedReader()
                reader.lineSequence().forEachIndexed { index, line ->
                    if (index == 0 || line.isBlank()) return@forEachIndexed

                    var cols = line.split(",")
                    if (cols.size < 7) cols = line.split(";")

                    val lat = cols.getOrNull(5)?.trim()?.toFloatOrNull()
                    val lng = cols.getOrNull(6)?.trim()?.toFloatOrNull()

                    if (lat != null && lng != null && lat != 0.0f && lng != 0.0f) {
                        val timeStamp = cols.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                        val accelX = cols.getOrNull(2)?.trim()?.toFloatOrNull() ?: 0f
                        val accelY = cols.getOrNull(3)?.trim()?.toFloatOrNull() ?: 0f
                        val accelZ = cols.getOrNull(4)?.trim()?.toFloatOrNull() ?: 0f
                        val speed = cols.getOrNull(7)?.trim()?.toFloatOrNull() ?: 0f

                        points.add(Telemetry(timeStamp, accelX, accelY, accelZ, lat, lng, speed))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return points
    }

    private suspend fun getPointsFromDatabase(dateId: Date): List<Telemetry> {
        return try {

            val groupedRuns: List<GroupRunTelemetryByDate> = telemetryRepository.getAllGroupedTelemetry(dateId)

            // Flatten your grouped database objects into a single cohesive list
            val allTelemetryEntries: List<TelemetryEntry> = groupedRuns.flatMap { group ->
                group.telemetryList
            }

            // TODO Make the check for 0 and null its own function
            val validTelemetryEntries = allTelemetryEntries.filter { dbEntry ->
                val lat = dbEntry.latitude
                val lng = dbEntry.longitude

                lat != null && lng != null && lat != 0.0f && lng != 0.0f
            }
            
            validTelemetryEntries.map { dbEntry ->
                Telemetry(
                    timeStamp = dbEntry.timeStamp ?: 0,
                    accelX = dbEntry.accelX ?: 0f,
                    accelY = dbEntry.accelY ?: 0f,
                    accelZ = dbEntry.accelZ ?: 0f,
                    latitude = dbEntry.latitude ?: 0f,
                    longitude = dbEntry.longitude ?: 0f,
                    speed = dbEntry.speed ?: 0f
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun drawPathOnMap(googleMap: GoogleMap, telemetryList: List<Telemetry>) {
        googleMap.clear()

        val polylineOptions = PolylineOptions()
            .color(Color.MAGENTA)
            .width(10f)

        val boundsBuilder = LatLngBounds.Builder()

        telemetryList.forEach { data ->
            val point = LatLng(data.latitude.toDouble(), data.longitude.toDouble())
            polylineOptions.add(point)
            boundsBuilder.include(point)

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(point)
                    .alpha(0f)
            )
            marker?.tag = data
        }

        googleMap.addPolyline(polylineOptions)

        googleMap.setOnMarkerClickListener { marker ->
            val data = marker.tag as? Telemetry
            data?.let {
                Toast.makeText(this, "Time: ${it.timeStamp}\nSpeed: ${it.speed}", Toast.LENGTH_SHORT).show()
            }
            false
        }

        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        }
        catch (e: Exception)
        {
            val firstPoint = LatLng(telemetryList[0].latitude.toDouble(), telemetryList[0].longitude.toDouble())
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstPoint, 15f))
        }
    }
}