package com.iofamily.garatrac.data

import com.google.gson.annotations.SerializedName

data class TrackPoint(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("ts") val timestamp: String
)

