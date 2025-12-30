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
import com.iofamily.garatrac.data.PoiRepository

class CarMapScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val itemListBuilder = ItemList.Builder()

        com.iofamily.garatrac.data.PoiRepository.getPois().forEach { poi ->
            val place = Place.Builder(
                CarLocation.create(poi.latitude, poi.longitude)
            )
            .setMarker(PlaceMarker.Builder().build())
            .build()

            itemListBuilder.addItem(
                Row.Builder()
                    .setTitle(poi.name)
                    .addText(poi.description)
                    .setMetadata(
                        Metadata.Builder()
                            .setPlace(place)
                            .build()
                    )
                    .build()
            )
        }

        return PlaceListMapTemplate.Builder()
            .setTitle("GaraTrac POIs")
            .setHeaderAction(Action.APP_ICON)
            .setItemList(itemListBuilder.build())
            .build()
    }
}

