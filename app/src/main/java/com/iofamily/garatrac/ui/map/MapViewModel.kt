package com.iofamily.garatrac.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iofamily.garatrac.data.LocationManager
import com.iofamily.garatrac.data.LocationRepository
import com.iofamily.garatrac.data.LocationUpdate
import com.iofamily.garatrac.data.SettingsRepository
import com.iofamily.garatrac.data.TrackPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR
}

data class MapUiState(
    val trackPoints: List<TrackPoint> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val countdown: Int = 0
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val locationRepository = LocationRepository()
    private val locationManager = LocationManager(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        startAutoSync()
    }

    private fun startAutoSync() {
        viewModelScope.launch {
            while (true) {
                val settings = settingsRepository.mapSettings.first()
                val intervalSeconds = (settings.updateInterval / 1000).toInt()

                // Countdown
                for (i in -1 downTo -intervalSeconds) {
                    _uiState.value = _uiState.value.copy(countdown = i)
                    delay(1000)
                }

                syncData()
            }
        }
    }

    fun syncData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.SYNCING)
            val settings = settingsRepository.mapSettings.first()

            // 1. Get Location
            val location = locationManager.getCurrentLocation()

            // 2. Post Location
            var uploadSuccess = false
            if (location != null) {
                val update = LocationUpdate(
                    deviceId = settings.deviceId,
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                uploadSuccess = locationRepository.postLocation(settings.serverUrl, update)
            }

            // 3. Get Track
            val track = locationRepository.getTrack(settings.serverUrl, settings.deviceId)

            if (uploadSuccess || track.isNotEmpty()) {
                 _uiState.value = _uiState.value.copy(
                    trackPoints = track,
                    syncStatus = SyncStatus.SUCCESS
                )
            } else {
                _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.ERROR)
            }

            // Reset status after a short delay if success/error to show the result
            delay(2000)
            if (_uiState.value.syncStatus != SyncStatus.SYNCING) {
                 _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.IDLE)
            }
        }
    }
}

