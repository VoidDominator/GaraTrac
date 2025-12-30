package com.iofamily.garatrac.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.mapSettings.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Map Type", style = MaterialTheme.typography.titleLarge)

            val mapTypes = listOf("MAPNIK", "USGS_TOPO", "USGS_SAT")
            mapTypes.forEach { type ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (type == settings.mapType),
                            onClick = { viewModel.setMapType(type) }
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (type == settings.mapType),
                        onClick = { viewModel.setMapType(type) }
                    )
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Server Configuration", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = settings.serverUrl,
                onValueChange = { viewModel.setServerUrl(it) },
                label = { Text("Server URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = settings.deviceId,
                onValueChange = { viewModel.setDeviceId(it) },
                label = { Text("Device ID") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Update Interval: ${settings.updateInterval / 1000} seconds", style = MaterialTheme.typography.titleMedium)

            Slider(
                value = settings.updateInterval.toFloat(),
                onValueChange = { viewModel.setUpdateInterval(it.toLong()) },
                valueRange = 10000f..300000f, // 10s to 5m
                steps = 28, // (300-10)/10 - 1 approx
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

