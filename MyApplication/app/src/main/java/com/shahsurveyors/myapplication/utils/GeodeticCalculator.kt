package com.shahsurveyors.myapplication.utils

import kotlin.math.*

object GeodeticCalculator {

    // UTM Zone 44N Constants (WGS 84)
    private const val a = 6378137.0
    private const val f = 1 / 298.257223563
    private const val k0 = 0.9996
    private const val FE = 500000.0
    private const val FN = 0.0

    /**
     * Converts Lat/Long to UTM Zone 44N Easting/Northing.
     * Simplified implementation for demonstration.
     */
    fun latLongToUTM44N(lat: Double, lon: Double): Pair<Double, Double> {
        val lon0 = 81.0 // Central Meridian for Zone 44
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)
        val lon0Rad = Math.toRadians(lon0)

        val e2 = 2 * f - f * f
        val ep2 = e2 / (1 - e2)
        val N = a / sqrt(1 - e2 * sin(latRad).pow(2))
        val T = tan(latRad).pow(2)
        val C = ep2 * cos(latRad).pow(2)
        val A = (lonRad - lon0Rad) * cos(latRad)

        val M = a * ((1 - e2 / 4 - 3 * e2.pow(2) / 64 - 5 * e2.pow(3) / 256) * latRad -
                (3 * e2 / 8 + 3 * e2.pow(2) / 32 + 45 * e2.pow(3) / 1024) * sin(2 * latRad) +
                (15 * e2.pow(2) / 256 + 45 * e2.pow(3) / 1024) * sin(4 * latRad) -
                (35 * e2.pow(3) / 3072) * sin(6 * latRad))

        val easting = k0 * N * (A + (1 - T + C) * A.pow(3) / 6 + (5 - 18 * T + T.pow(2) + 72 * C - 58 * ep2) * A.pow(5) / 120) + FE
        val northing = k0 * (M + N * tan(latRad) * (A.pow(2) / 2 + (5 - T + 9 * C + 4 * C.pow(2)) * A.pow(4) / 24 + (61 - 58 * T + T.pow(2) + 600 * C - 330 * ep2) * A.pow(6) / 720)) + FN

        return Pair(easting, northing)
    }

    /**
     * Applies a 2D Base-Shift to coordinates.
     */
    fun applyBaseShift(east: Double, north: Double, dE: Double, dN: Double): Pair<Double, Double> {
        return Pair(east + dE, north + dN)
    }
}
