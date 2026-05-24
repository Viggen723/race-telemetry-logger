package com.adam.roy.model.rawRunView.ui
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.adam.roy.MainActivity
import com.adam.roy.databinding.ActivityRawRunsViewBinding
import com.adam.roy.model.rawRunView.viewModel.RawRunViewModel
import com.adam.roy.utils.UDPController
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.util.Calendar
import java.util.Date


class RawRunsView : AppCompatActivity() {
    private lateinit var binding: ActivityRawRunsViewBinding
    private lateinit var viewModel: RawRunViewModel

    private var csvString = ""
    private var midnightDate: Date = Date(0) // Initialize at epoch time just in case there is no selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRawRunsViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[RawRunViewModel::class.java]

        binding.recordingButton.setOnClickListener {
            val isActive = viewModel.isActive.value ?: false

            if (!isActive) {
                Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show()
                viewModel.startRecordLoop()
            } else {
                viewModel.stopAndSave()
                Toast.makeText(this, "Saving recording to database...", Toast.LENGTH_SHORT).show()
            }
        }

        binding.saveToCSVbutton.setOnClickListener {
            viewModel.generateCsvString (midnightDate) { generatedCsv ->
                if (generatedCsv.isBlank() || generatedCsv.lineSequence().count() <= 1) {
                    Toast.makeText(this, "No data available to export!", Toast.LENGTH_SHORT).show()
                    return@generateCsvString
                }

                csvString = generatedCsv
                val targetFileName = "telemetry_run_${System.currentTimeMillis()}.csv"
                createCsvFileLauncher.launch(targetFileName)
            }
        }

        binding.datePickerButton.setOnClickListener {
            showDatePicker()
        }

        viewModel.isActive.observe(this) { isRecordingActive ->
            if (isRecordingActive) {
                binding.recordingButton.text = "Stop recording"
                binding.saveToCSVbutton.isEnabled =
                    false // Block file exports while actively recording
            } else {
                binding.recordingButton.text = "Start recording"
                binding.saveToCSVbutton.isEnabled = true  // Allow file exports when system is idle
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed()
            {
                startActivity(Intent(this@RawRunsView, MainActivity::class.java))
                UDPController.closeSocket()
                finish()
            }
        }
        onBackPressedDispatcher.addCallback(this@RawRunsView, callback)
    }

    private val createCsvFileLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/comma-separated-values")
    )
    { uri ->
        uri?.let { targetUri ->
            try
            {
                contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(csvString)
                    }
                }
                Toast.makeText(this, "CSV Exported successfully!", Toast.LENGTH_SHORT).show()
                csvString = ""
            }
            catch (e: Exception)
            {
                Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                val targetCalendar = Calendar.getInstance()

                targetCalendar.set(Calendar.YEAR, selectedYear)
                targetCalendar.set(Calendar.MONTH, selectedMonth)
                targetCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                // Making the time midnight to make it easier to search
                targetCalendar.set(Calendar.HOUR_OF_DAY, 0)
                targetCalendar.set(Calendar.MINUTE, 0)
                targetCalendar.set(Calendar.SECOND, 0)
                targetCalendar.set(Calendar.MILLISECOND, 0)

                midnightDate = targetCalendar.time

                Toast.makeText(this, "${selectedMonth + 1}/$selectedDay/$selectedYear selected!", Toast.LENGTH_SHORT).show()
            },
            year, month, day
        )
        datePickerDialog.show()
    }
}