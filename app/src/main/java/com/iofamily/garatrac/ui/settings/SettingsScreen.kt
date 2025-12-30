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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val settings by viewModel.mapSettings.collectAsState()

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
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
        }
    }
}

