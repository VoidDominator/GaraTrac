package com.iofamily.garatrac.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepository {
    suspend fun getTrack(serverUrl: String, deviceId: String): List<TrackPoint> {
        val validUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(validUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(TrackerApi::class.java)
            api.getTrack(deviceId)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun postLocation(serverUrl: String, update: LocationUpdate): Boolean {
        val validUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(validUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(TrackerApi::class.java)
            api.postLocation(update)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
