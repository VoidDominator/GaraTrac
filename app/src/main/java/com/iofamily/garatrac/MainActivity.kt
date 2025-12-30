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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iofamily.garatrac.ui.map.MapViewModel
import com.iofamily.garatrac.ui.map.SyncStatus
import kotlinx.coroutines.launch
import com.iofamily.garatrac.ui.map.OsmMapView
import com.iofamily.garatrac.ui.theme.GaraTracTheme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import android.Manifest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

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

                LaunchedEffect(mapUiState.errorMessage) {
                    mapUiState.errorMessage?.let {
                        scope.launch {
                            snackbarHostState.showSnackbar(it)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                            val containerColor by animateColorAsState(
                                targetValue = when (mapUiState.syncStatus) {
                                    SyncStatus.SYNCING -> Color.Green
                                    SyncStatus.ERROR -> Color.Red
                                    SyncStatus.DISABLED -> Color.Gray
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                label = "fabColor"
                            )

                            Surface(
                                shape = FloatingActionButtonDefaults.shape,
                                color = containerColor,
                                shadowElevation = 6.dp,
                                modifier = Modifier.combinedClickable(
                                    onClick = { mapViewModel.syncData() },
                                    onLongClick = { mapViewModel.toggleSync() }
                                )
                            ) {
                                Box(
                                    modifier = Modifier.defaultMinSize(
                                        minWidth = 56.dp,
                                        minHeight = 56.dp
                                    ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AnimatedContent(
                                        targetState = mapUiState.syncStatus,
                                        transitionSpec = {
                                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                        },
                                        label = "fabContent"
                                    ) { status ->
                                        when (status) {
                                            SyncStatus.SYNCING -> Icon(Icons.Default.Sync, "Syncing")
                                            SyncStatus.ERROR -> Icon(Icons.Default.CloudOff, "Error")
                                            SyncStatus.DISABLED -> Icon(Icons.Default.SyncDisabled, "Disabled")
                                            else -> Text("${mapUiState.countdown}")
                                        }
                                    }
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
