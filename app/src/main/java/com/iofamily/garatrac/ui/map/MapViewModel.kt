package com.iofamily.garatrac.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iofamily.garatrac.data.LocationManager
import com.iofamily.garatrac.data.LocationRepository
import com.iofamily.garatrac.data.LocationUpdate
import com.iofamily.garatrac.data.SettingsRepository
import com.iofamily.garatrac.data.TrackPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR, DISABLED
}

data class MapUiState(
    val trackPoints: List<TrackPoint> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.IDLE,
    val countdown: Int = 0,
    val isSyncEnabled: Boolean = true,
    val errorMessage: String? = null
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)
    private val locationRepository = LocationRepository()
    private val locationManager = LocationManager(application)

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var autoSyncJob: Job? = null

    init {
        startAutoSync()
    }

    private fun startAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = viewModelScope.launch {
            if (!_uiState.value.isSyncEnabled) {
                _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.DISABLED)
                return@launch
            }

            while (isActive) {
                if (!_uiState.value.isSyncEnabled) {
                    _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.DISABLED)
                    break
                }

                val success = syncDataInternal()

                // Wait to show result
                delay(2000)

                if (!_uiState.value.isSyncEnabled) break

                val settings = settingsRepository.mapSettings.first()

                if (success) {
                    _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.IDLE)
                    val intervalSeconds = (settings.updateInterval / 1000).toInt()
                    for (i in -1 downTo -intervalSeconds) {
                        if (!_uiState.value.isSyncEnabled) break
                        _uiState.value = _uiState.value.copy(countdown = i)
                        delay(1000)
                    }
                } else {
                    // Keep ERROR status (Red)
                    val retrySeconds = (settings.retryInterval / 1000).toInt()
                    for (i in retrySeconds downTo 1) {
                        if (!_uiState.value.isSyncEnabled) break
                        _uiState.value = _uiState.value.copy(countdown = i)
                        delay(1000)
                    }
                }
            }
        }
    }

    fun toggleSync() {
        val newEnabledState = !_uiState.value.isSyncEnabled
        _uiState.value = _uiState.value.copy(
            isSyncEnabled = newEnabledState,
            syncStatus = if (newEnabledState) SyncStatus.IDLE else SyncStatus.DISABLED
        )
        if (newEnabledState) {
            startAutoSync()
        } else {
            autoSyncJob?.cancel()
        }
    }

    fun syncData() {
        if (!_uiState.value.isSyncEnabled) {
            _uiState.value = _uiState.value.copy(isSyncEnabled = true)
        }
        startAutoSync()
    }

    private suspend fun syncDataInternal(): Boolean {
        _uiState.value = _uiState.value.copy(syncStatus = SyncStatus.SYNCING, errorMessage = null)
        val settings = settingsRepository.mapSettings.first()

        // 1. Get Location
        val location = locationManager.getCurrentLocation()

        // 2. Post Location
        var uploadResult: Result<Unit> = Result.success(Unit)
        if (location != null) {
            val update = LocationUpdate(
                deviceId = settings.deviceId,
                latitude = location.latitude,
                longitude = location.longitude
            )
            uploadResult = locationRepository.postLocation(settings.serverUrl, update)
        }

        // 3. Get Track
        val trackResult = locationRepository.getTrack(settings.serverUrl, settings.deviceId)

        val success = uploadResult.isSuccess && trackResult.isSuccess

        if (success) {
            _uiState.value = _uiState.value.copy(
                trackPoints = trackResult.getOrDefault(emptyList()),
                syncStatus = SyncStatus.SUCCESS
            )
        } else {
            val errorMsg = uploadResult.exceptionOrNull()?.message ?: trackResult.exceptionOrNull()?.message ?: "Unknown error"
            _uiState.value = _uiState.value.copy(
                syncStatus = SyncStatus.ERROR,
                errorMessage = errorMsg
            )
        }

        return success
    }
}

