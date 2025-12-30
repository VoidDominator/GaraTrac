package com.iofamily.garatrac.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarLocation
import androidx.car.app.model.ItemList
import androidx.car.app.model.Metadata
import androidx.car.app.model.Place
import androidx.car.app.model.PlaceListMapTemplate
import androidx.car.app.model.PlaceMarker
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay
import com.iofamily.garatrac.data.LocationRepository
import com.iofamily.garatrac.data.SettingsRepository
import com.iofamily.garatrac.data.TrackPoint

class CarMapScreen(carContext: CarContext) : Screen(carContext) {
    private var trackPoints: List<TrackPoint> = emptyList()
    private val locationRepository = LocationRepository()
    private val settingsRepository = SettingsRepository(carContext)

    init {
        lifecycleScope.launch {
            val settings = settingsRepository.mapSettings.first()
            while(true) {
                val result = locationRepository.getTrack(settings.serverUrl, settings.deviceId)
                trackPoints = result.getOrDefault(emptyList())
                invalidate()
                delay(settings.updateInterval)
            }
        }
    }

    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        if (trackPoints.isEmpty()) {
             itemListBuilder.addItem(
                Row.Builder()
                    .setTitle("Loading or No Data")
                    .build()
            )
        } else {
            trackPoints.forEach { point ->
                val place = Place.Builder(
                    CarLocation.create(point.latitude, point.longitude)
                )
                .setMarker(PlaceMarker.Builder().build())
                .build()

                itemListBuilder.addItem(
                    Row.Builder()
                        .setTitle("Location")
                        .addText(point.timestamp)
                        .setMetadata(
                            Metadata.Builder()
                                .setPlace(place)
                                .build()
                        )
                        .build()
                )
            }
        }

        return PlaceListMapTemplate.Builder()
            .setTitle("GaraTrac Track")
            .setHeaderAction(Action.APP_ICON)
            .setItemList(itemListBuilder.build())
            .build()
    }
}

