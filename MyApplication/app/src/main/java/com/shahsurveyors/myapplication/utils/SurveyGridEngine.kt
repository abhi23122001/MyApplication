package com.shahsurveyors.myapplication.utils

import kotlin.math.*

object SurveyGridEngine {

    // WGS84 to UTM Zone 44N (Simplified Transverse Mercator)
    fun wgs84ToUtm44N(lat: Double, lon: Double): Pair<Double, Double> {
        val a = 6378137.0 // semi-major axis
        val f = 1 / 298.257223563 // flattening
        val k0 = 0.9996 // scale factor
        val longitudeOrigin = 81.0 // Central Meridian for Zone 44
        val fe = 500000.0 // false easting
        val fn = 0.0 // false northing (Northern Hemisphere)

        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)
        val lambda0 = Math.toRadians(longitudeOrigin)

        val e2 = 2 * f - f * f
        val ep2 = e2 / (1 - e2)
        val n = a / sqrt(1 - e2 * sin(phi) * sin(phi))
        val t = tan(phi) * tan(phi)
        val c = ep2 * cos(phi) * cos(phi)
        val a_val = cos(phi) * (lambda - lambda0)

        val m = a * ((1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * phi -
                (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2 * e2 / 1024) * sin(2 * phi) +
                (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * sin(4 * phi) -
                (35 * e2 * e2 * e2 / 3072) * sin(6 * phi))

        val easting = k0 * n * (a_val + (1 - t + c) * a_val * a_val * a_val / 6 +
                (5 - 18 * t + t * t + 72 * c - 58 * ep2) * a_val * a_val * a_val * a_val * a_val / 120) + fe

        val northing = k0 * (m + n * tan(phi) * (a_val * a_val / 2 + (5 - t + 9 * c + 4 * c * c) * a_val * a_val * a_val * a_val / 24 +
                (61 - 58 * t + t * t + 600 * c - 330 * ep2) * a_val * a_val * a_val * a_val * a_val * a_val / 720)) + fn

        return Pair(easting, northing)
    }

    // 2D Base-Shift (Translation only for simplicity)
    fun applyBaseShift(e: Double, n: Double, shiftE: Double, shiftN: Double): Pair<Double, Double> {
        return Pair(e + shiftE, n + shiftN)
    }

    // Polygon Area Calculator using Shoelace Formula
    fun calculateArea(coords: List<Pair<Double, Double>>): Double {
        if (coords.size < 3) return 0.0
        var area = 0.0
        for (i in coords.indices) {
            val j = (i + 1) % coords.size
            area += coords[i].first * coords[j].second
            area -= coords[j].first * coords[i].second
        }
        return abs(area) / 2.0
    }

    fun convertAreaToUnits(areaSqM: Double): Map<String, Double> {
        return mapOf(
            "SqM" to areaSqM,
            "Acres" to areaSqM * 0.000247105,
            "Hectares" to areaSqM * 0.0001
        )
    }
}
