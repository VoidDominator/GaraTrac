package com.iofamily.garatrac.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onAboutClick: () -> Unit
) {
    val settings by viewModel.mapSettings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Map Type", style = MaterialTheme.typography.titleLarge)

        val availableSources = viewModel.availableTileSources
        availableSources.forEach { source ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (source.name() == settings.mapType),
                        onClick = { viewModel.setMapType(source.name()) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (source.name() == settings.mapType),
                    onClick = { viewModel.setMapType(source.name()) }
                )
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = source.name(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = source.name(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

        Spacer(modifier = Modifier.height(16.dp))
        Text("Retry Interval: ${settings.retryInterval / 1000} seconds", style = MaterialTheme.typography.titleMedium)

        Slider(
            value = settings.retryInterval.toFloat(),
            onValueChange = { viewModel.setRetryInterval(it.toLong()) },
            valueRange = 5000f..60000f, // 5s to 1m
            steps = 10,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Settings Panel Position (Horizontal)", style = MaterialTheme.typography.titleLarge)

        val positions = listOf("Left", "Right")
        positions.forEach { position ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (position == settings.tabletPanelPosition),
                        onClick = { viewModel.setTabletPanelPosition(position) }
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (position == settings.tabletPanelPosition),
                    onClick = { viewModel.setTabletPanelPosition(position) }
                )
                Text(
                    text = position,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAboutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("About App")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
