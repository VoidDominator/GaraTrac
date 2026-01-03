package com.iofamily.garatrac.data

import com.google.gson.annotations.SerializedName

data class LocationUpdate(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
)

