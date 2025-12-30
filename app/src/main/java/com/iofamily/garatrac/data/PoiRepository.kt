package com.iofamily.garatrac.data

object PoiRepository {
    fun getPois(): List<Poi> {
        return listOf(
            Poi("1", "Eiffel Tower", "Iron lady", 48.8583, 2.2944),
            Poi("2", "Louvre Museum", "Art museum", 48.8606, 2.3376),
            Poi("3", "Notre-Dame", "Cathedral", 48.8530, 2.3499)
        )
    }
}

