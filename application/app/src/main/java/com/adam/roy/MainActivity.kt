package com.adam.roy

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.adam.roy.features.EulerAngles
import com.adam.roy.features.Settings
import com.adam.roy.features.Telemetry
import com.adam.roy.features.gForceMeter.ui.GforceDash
import com.adam.roy.features.mappedSession.ui.MappedSessionView
import com.adam.roy.features.rawRunView.ui.RawRunsView
import com.adam.roy.features.timer.ui.Timer0to60
import com.adam.roy.ui.theme.RaceTelemetryLoggerTheme
import com.adam.roy.utils.DataTools
import com.adam.roy.utils.UDPController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            RaceTelemetryLoggerTheme {
                MainScreen(onNavigate = { intent ->
                    startActivity(intent)
                    onDestroy()
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        UDPController.closeSocket()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onNavigate: (Intent) -> Unit) {

    // State to hold UDP Data
    var telemetry by remember { mutableStateOf(Telemetry(0,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f,
        0f)) }
    var angles by remember { mutableStateOf(EulerAngles(0f, 0f, 0f)) }

    // Coroutine for the Compose lifecycle replaces the raw Thread()
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            while (isActive) { // Automatically cancels if the Composable leaves the screen
                val rawData = UDPController.receive()
                if (rawData.isNotEmpty()) {
                    val parsedData = DataTools.parseBinaryData(rawData)
                    if (parsedData != null) {
                        val calculatedAngles = DataTools.convertToEuler(
                            parsedData.qw, parsedData.qi, parsedData.qj, parsedData.qk
                        )

                        telemetry = Telemetry(
                            timeStamp = parsedData.timeStamp,
                            accelX = parsedData.accelX,
                            accelY = parsedData.accelY,
                            accelZ = parsedData.accelZ,
                            longitude = parsedData.longitude,
                            latitude = parsedData.latitude,
                            speed = parsedData.speed * 1.15078f,
                            qw = parsedData.qw,
                            qi = parsedData.qi,
                            qj = parsedData.qj,
                            qk = parsedData.qk
                        )

                        angles = calculatedAngles
                    }
                }
                delay(16) // Updating at around 60 fps as it was skipping frames at higher
            }
        }
    }

    Scaffold(
        topBar = {
            TitleBar()
        },
        bottomBar = {
            BottomBar(onNavigate = onNavigate)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            InfoScreen(data = telemetry, angles = angles)
        }
    }
}

@Composable
fun TitleBar(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(top = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp),
                contentDescription = "App Logo",
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BottomBar(modifier: Modifier = Modifier, onNavigate: (Intent) -> Unit) {

    val context = LocalContext.current

    NavigationBar(
        modifier = modifier.shadow(30.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant

        ) {
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.ic_timer_logo), contentDescription = "Timer") },
            label = { Text("Timer") },
            selected = false, // Have to change if it is going to be one activity running these things
            onClick = { onNavigate(Intent(context, Timer0to60::class.java)) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.ic_gforce_mark), contentDescription = "G-Force") },
            label = { Text("G-Force") },
            selected = false,
            onClick = { onNavigate(Intent(context, GforceDash::class.java)) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.ic_stopwatch_arrow_down), contentDescription = "Raw Runs") },
            label = { Text("Runs") },
            selected = false,
            onClick = { onNavigate(Intent(context, RawRunsView::class.java)) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.ic_globe_mark), contentDescription = "Map") },
            label = { Text("Map") },
            selected = false,
            onClick = { onNavigate(Intent(context, MappedSessionView::class.java)) }
        )
        NavigationBarItem(
            icon = { Image(painterResource(R.drawable.outline_settings_24), contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = false,
            onClick = { onNavigate(Intent(context, Settings::class.java)) }
        )
    }
}

// TODO change to string resources (Same with above composable)
@Composable
fun InfoScreen(modifier: Modifier = Modifier, data: Telemetry, angles: EulerAngles) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
            .fillMaxWidth(0.8f) // 80 percent of screen
            .padding(16.dp)
            .shadow(30.dp)
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Elapsed Time: ${data.timeStamp} ms", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(text = "Speed: %.2f mph".format(data.speed), style = MaterialTheme.typography.bodyLarge)
            Text(text = "Longitude: %.6f°".format(data.longitude), style = MaterialTheme.typography.bodyMedium)
            Text(text = "Latitude: %.6f°".format(data.latitude), style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Lean (Roll): %.1f°".format(angles.roll), style = MaterialTheme.typography.bodyMedium)
            Text(text = "Pitch: %.1f°".format(angles.pitch), style = MaterialTheme.typography.bodyMedium)
            Text(text = "Yaw: %.1f°".format(angles.yaw), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview
@Composable
fun InfoScreenPreview()
{
    InfoScreen(Modifier, Telemetry( 0, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), EulerAngles(0f, 0f, 0f))
}