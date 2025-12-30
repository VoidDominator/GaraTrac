package com.iofamily.garatrac.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class MapSettings(
    val mapType: String = "MAPNIK"
)

class SettingsRepository(private val context: Context) {
    private val MAP_TYPE_KEY = stringPreferencesKey("map_type")

    val mapSettings: Flow<MapSettings> = context.dataStore.data
        .map { preferences ->
            MapSettings(
                mapType = preferences[MAP_TYPE_KEY] ?: "MAPNIK"
            )
        }

    suspend fun setMapType(mapType: String) {
        context.dataStore.edit { preferences ->
            preferences[MAP_TYPE_KEY] = mapType
        }
    }
}

