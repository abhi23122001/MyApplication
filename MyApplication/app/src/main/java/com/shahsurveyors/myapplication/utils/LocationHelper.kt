package com.shahsurveyors.myapplication.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await

object LocationHelper {

    /**
     * Returns current GPS location as:
     * Pair(latitude, longitude)
     *
     * Returns null if:
     * - Location permission is not granted
     * - GPS/location is unavailable
     * - Location request fails
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(
        context: Context
    ): Pair<Double, Double>? {

        // Check fine/coarse location permission first
        val finePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (
            finePermission != PackageManager.PERMISSION_GRANTED &&
            coarsePermission != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        return try {

            /*
             * First try high accuracy fresh location.
             */
            val currentLocation =
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

            if (currentLocation != null) {

                Pair(
                    currentLocation.latitude,
                    currentLocation.longitude
                )

            } else {

                /*
                 * Fallback to last known location.
                 */
                val lastLocation =
                    fusedLocationClient.lastLocation.await()

                if (lastLocation != null) {

                    Pair(
                        lastLocation.latitude,
                        lastLocation.longitude
                    )

                } else {
                    null
                }
            }

        } catch (e: SecurityException) {
            null

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Returns latitude only.
     */
    suspend fun getLatitude(
        context: Context
    ): Double? {
        return getCurrentLocation(context)?.first
    }

    /**
     * Returns longitude only.
     */
    suspend fun getLongitude(
        context: Context
    ): Double? {
        return getCurrentLocation(context)?.second
    }
}