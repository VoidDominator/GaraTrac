package com.iofamily.garatrac.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import androidx.datastore.preferences.core.longPreferencesKey

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class MapSettings(
    val mapType: String = "MAPNIK",
    val serverUrl: String = "https://homelab.perifural.com",
    val deviceId: String = "dev1",
    val updateInterval: Long = 60000L, // 1 minute in milliseconds
    val tabletPanelPosition: String = "Right" // "Left" or "Right"
)

class SettingsRepository(private val context: Context) {
    private val MAP_TYPE_KEY = stringPreferencesKey("map_type")
    private val SERVER_URL_KEY = stringPreferencesKey("server_url")
    private val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    private val UPDATE_INTERVAL_KEY = longPreferencesKey("update_interval")
    private val TABLET_PANEL_POSITION_KEY = stringPreferencesKey("tablet_panel_position")

    val mapSettings: Flow<MapSettings> = context.dataStore.data
        .map { preferences ->
            MapSettings(
                mapType = preferences[MAP_TYPE_KEY] ?: "MAPNIK",
                serverUrl = preferences[SERVER_URL_KEY] ?: "https://homelab.perifural.com",
                deviceId = preferences[DEVICE_ID_KEY] ?: "dev1",
                updateInterval = preferences[UPDATE_INTERVAL_KEY] ?: 60000L,
                tabletPanelPosition = preferences[TABLET_PANEL_POSITION_KEY] ?: "Right"
            )
        }

    suspend fun setMapType(mapType: String) {
        context.dataStore.edit { preferences ->
            preferences[MAP_TYPE_KEY] = mapType
        }
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL_KEY] = url
        }
    }

    suspend fun setDeviceId(id: String) {
        context.dataStore.edit { preferences ->
            preferences[DEVICE_ID_KEY] = id
        }
    }

    suspend fun setUpdateInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[UPDATE_INTERVAL_KEY] = interval
        }
    }

    suspend fun setTabletPanelPosition(position: String) {
        context.dataStore.edit { preferences ->
            preferences[TABLET_PANEL_POSITION_KEY] = position
        }
    }
}
