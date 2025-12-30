package com.iofamily.garatrac.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iofamily.garatrac.data.MapSettings
import com.iofamily.garatrac.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)

    val mapSettings = repository.mapSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MapSettings()
    )

    fun setMapType(mapType: String) {
        viewModelScope.launch {
            repository.setMapType(mapType)
        }
    }

    fun setServerUrl(url: String) {
        viewModelScope.launch {
            repository.setServerUrl(url)
        }
    }

    fun setDeviceId(id: String) {
        viewModelScope.launch {
            repository.setDeviceId(id)
        }
    }

    fun setUpdateInterval(interval: Long) {
        viewModelScope.launch {
            repository.setUpdateInterval(interval)
        }
    }
}
