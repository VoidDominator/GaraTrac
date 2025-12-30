package com.iofamily.garatrac.data

import retrofit2.http.GET
import retrofit2.http.Query

interface TrackerApi {
    @GET("api/track")
    suspend fun getTrack(@Query("deviceId") deviceId: String): List<TrackPoint>
}

