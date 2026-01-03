package com.iofamily.garatrac.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LocationRepository {
    suspend fun getTrack(serverUrl: String, deviceId: String): Result<List<TrackPoint>> {
        val validUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(validUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(TrackerApi::class.java)
            Result.success(api.getTrack(deviceId))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun postLocation(serverUrl: String, update: LocationUpdate): Result<Unit> {
        val validUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(validUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(TrackerApi::class.java)
            api.postLocation(update)
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
