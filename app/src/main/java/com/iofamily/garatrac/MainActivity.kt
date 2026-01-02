package com.iofamily.garatrac

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iofamily.garatrac.ui.settings.SettingsScreen
import com.iofamily.garatrac.ui.settings.SettingsViewModel
import com.iofamily.garatrac.ui.settings.AboutScreen
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.BottomSheetDefaults
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.res.Configuration
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iofamily.garatrac.ui.theme.GaraTracTheme
import com.iofamily.garatrac.ui.theme.ExtendedTheme
import com.iofamily.garatrac.ui.map.MapViewModel
import com.iofamily.garatrac.ui.map.SyncStatus
import com.iofamily.garatrac.ui.map.OsmMapView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GaraTracTheme {
                val settingsViewModel: SettingsViewModel = viewModel()
                val mapViewModel: MapViewModel = viewModel()

                val settings by settingsViewModel.mapSettings.collectAsState()
                val mapUiState by mapViewModel.uiState.collectAsState()

                var showSettings by remember { mutableStateOf(false) }
                var showAbout by remember { mutableStateOf(false) }
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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

                LaunchedEffect(showSettings) {
                    if (!showSettings) {
                        showAbout = false
                    }
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
                    floatingActionButton = {
                        if (!isLandscape || !showSettings) {
                            val containerColor by animateColorAsState(
                                targetValue = when (mapUiState.syncStatus) {
                                    SyncStatus.SYNCING -> ExtendedTheme.colorScheme.success.colorContainer
                                    SyncStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                                    SyncStatus.DISABLED -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.primaryContainer
                                },
                                label = "fabColor"
                            )

                            val contentColor by animateColorAsState(
                                targetValue = when (mapUiState.syncStatus) {
                                    SyncStatus.SYNCING -> ExtendedTheme.colorScheme.success.onColorContainer
                                    SyncStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                    SyncStatus.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.onPrimaryContainer
                                },
                                label = "fabContentColor"
                            )

                            Surface(
                                shape = FloatingActionButtonDefaults.shape,
                                color = containerColor,
                                contentColor = contentColor,
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
//                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        OsmMapView(
                            modifier = Modifier.fillMaxSize(),
                            trackPoints = mapUiState.trackPoints,
                            mapType = settings.mapType
                        )

                        FloatingSearchBar(
                            deviceName = "GaraTrac",
                            onSettingsClick = { showSettings = true },
                            modifier = Modifier
                                .align(Alignment.TopCenter)
//                                .statusBarsPadding()
                                .padding(innerPadding)
                        )

                        if (isLandscape) {
                            if (showSettings) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .clickable { showSettings = false }
                                )
                            }

                            val isRightPanel = settings.tabletPanelPosition == "Right"
                            val alignment = if (isRightPanel) Alignment.CenterEnd else Alignment.CenterStart

                            if (showSettings) {
                                BackHandler {
                                    if (showAbout) {
                                        showAbout = false
                                    } else {
                                        showSettings = false
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .align(alignment)
                                    .width(400.dp)
                                    .fillMaxHeight()
                                    .padding(top = 16.dp)
                            ) {
                                AnimatedVisibility(
                                    visible = showSettings,
                                    enter = slideInVertically(initialOffsetY = { it }),
                                    exit = slideOutVertically(targetOffsetY = { it }),
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        shape = MaterialTheme.shapes.large.copy(
                                            bottomStart = CornerSize(0.dp),
                                            bottomEnd = CornerSize(0.dp)
                                        ),
                                        color = MaterialTheme.colorScheme.surface,
                                        tonalElevation = 6.dp
                                    ) {
                                        Column(modifier = Modifier.fillMaxSize()) {
                                            BottomSheetDefaults.DragHandle(
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            )
                                            AnimatedContent(targetState = showAbout, label = "SettingsContent") { isAbout ->
                                                if (isAbout) {
                                                    AboutScreen(onBackClick = { showAbout = false })
                                                } else {
                                                    SettingsScreen(
                                                        viewModel = settingsViewModel,
                                                        onAboutClick = { showAbout = true }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (showSettings) {
                                ModalBottomSheet(
                                    onDismissRequest = { showSettings = false },
                                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
                                ) {
                                    AnimatedContent(targetState = showAbout, label = "SettingsContent") { isAbout ->
                                        if (isAbout) {
                                            AboutScreen(onBackClick = { showAbout = false })
                                        } else {
                                            SettingsScreen(
                                                viewModel = settingsViewModel,
                                                onAboutClick = { showAbout = true }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingSearchBar(
    deviceName: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape,
        shadowElevation = 6.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
