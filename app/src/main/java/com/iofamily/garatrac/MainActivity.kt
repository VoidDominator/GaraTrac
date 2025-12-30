package com.iofamily.garatrac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iofamily.garatrac.ui.settings.SettingsScreen
import com.iofamily.garatrac.ui.settings.SettingsViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import com.iofamily.garatrac.data.LocationRepository
import com.iofamily.garatrac.data.TrackPoint
import com.iofamily.garatrac.ui.map.OsmMapView
import com.iofamily.garatrac.ui.theme.GaraTracTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaraTracTheme {
                val navController = rememberNavController()
                val settingsViewModel: SettingsViewModel = viewModel()
                val settings by settingsViewModel.mapSettings.collectAsState()

                val locationRepository = remember { LocationRepository() }
                var trackPoints by remember { mutableStateOf(emptyList<TrackPoint>()) }

                LaunchedEffect(settings.serverUrl, settings.deviceId, settings.updateInterval) {
                    while(true) {
                        trackPoints = locationRepository.getTrack(settings.serverUrl, settings.deviceId)
                        delay(settings.updateInterval)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("GaraTrac") },
                            actions = {
                                IconButton(onClick = { navController.navigate("settings") }) {
                                    Text("Settings")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "map",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("map") {
                            OsmMapView(
                                modifier = Modifier.fillMaxSize(),
                                trackPoints = trackPoints,
                                mapType = settings.mapType
                            )
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = settingsViewModel)
                        }
                    }
                }
            }
        }
    }
}

