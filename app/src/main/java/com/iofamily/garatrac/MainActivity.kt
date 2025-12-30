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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.iofamily.garatrac.ui.settings.SettingsScreen
import com.iofamily.garatrac.ui.settings.SettingsViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.iofamily.garatrac.ui.map.MapViewModel
import com.iofamily.garatrac.ui.map.SyncStatus
import com.iofamily.garatrac.ui.map.OsmMapView
import com.iofamily.garatrac.ui.theme.GaraTracTheme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaraTracTheme {
                val navController = rememberNavController()
                val settingsViewModel: SettingsViewModel = viewModel()
                val mapViewModel: MapViewModel = viewModel()

                val settings by settingsViewModel.mapSettings.collectAsState()
                val mapUiState by mapViewModel.uiState.collectAsState()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
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
                    },
                    floatingActionButton = {
                        if (currentRoute == "map") {
                            FloatingActionButton(
                                onClick = { mapViewModel.syncData() },
                                containerColor = when (mapUiState.syncStatus) {
                                    SyncStatus.SYNCING -> Color.Green
                                    SyncStatus.ERROR -> Color.Red
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                }
                            ) {
                                when (mapUiState.syncStatus) {
                                    SyncStatus.SYNCING -> Icon(Icons.Default.Sync, "Syncing")
                                    SyncStatus.ERROR -> Icon(Icons.Default.CloudOff, "Error")
                                    else -> Text("${mapUiState.countdown}")
                                }
                            }
                        }
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
                                trackPoints = mapUiState.trackPoints,
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
