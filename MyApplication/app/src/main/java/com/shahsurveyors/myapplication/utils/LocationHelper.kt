package com.shahsurveyors.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

object LocationHelper {
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
