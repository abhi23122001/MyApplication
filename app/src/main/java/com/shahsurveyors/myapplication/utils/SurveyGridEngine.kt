package com.shahsurveyors.myapplication.utils

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object SurveyGridEngine {

    // ============================================================
    // WGS84 / UTM ZONE 44N
    // ============================================================

    private const val WGS84_A = 6378137.0
    private const val WGS84_F = 1.0 / 298.257223563
    private const val UTM_K0 = 0.9996
    private const val UTM_ZONE_44_CENTRAL_MERIDIAN = 81.0
    private const val FALSE_EASTING = 500000.0
    private const val FALSE_NORTHING_NORTHERN = 0.0

    /**
     * Converts WGS84 Latitude / Longitude to UTM Zone 44N.
     *
     * Returns:
     * Pair(
     *     Easting,
     *     Northing
     * )
     *
     * Zone 44N central meridian = 81°E
     */
    fun wgs84ToUtm44N(
        lat: Double,
        lon: Double
    ): Pair<Double, Double> {

        require(lat in -80.0..84.0) {
            "Latitude must be between -80° and 84°"
        }

        require(lon in -180.0..180.0) {
            "Longitude must be between -180° and 180°"
        }

        val a = WGS84_A
        val f = WGS84_F
        val k0 = UTM_K0

        val longitudeOrigin =
            Math.toRadians(UTM_ZONE_44_CENTRAL_MERIDIAN)

        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)

        // First eccentricity squared
        val e2 = 2.0 * f - f * f

        // Second eccentricity squared
        val ep2 = e2 / (1.0 - e2)

        val sinPhi = sin(phi)
        val cosPhi = cos(phi)
        val tanPhi = tan(phi)

        // Radius of curvature in prime vertical
        val n = a / sqrt(
            1.0 - e2 * sinPhi * sinPhi
        )

        val t = tanPhi * tanPhi

        val c = ep2 * cosPhi * cosPhi

        // Difference in longitude
        val aTerm =
            cosPhi * (lambda - longitudeOrigin)

        // Meridional arc
        val m = a * (
                (1.0 - e2 / 4.0
                        - 3.0 * e2 * e2 / 64.0
                        - 5.0 * e2 * e2 * e2 / 256.0) * phi

                        - (3.0 * e2 / 8.0
                        + 3.0 * e2 * e2 / 32.0
                        + 45.0 * e2 * e2 * e2 / 1024.0)
                        * sin(2.0 * phi)

                        + (15.0 * e2 * e2 / 256.0
                        + 45.0 * e2 * e2 * e2 / 1024.0)
                        * sin(4.0 * phi)

                        - (35.0 * e2 * e2 * e2 / 3072.0)
                        * sin(6.0 * phi)
                )

        // A²
        val a2 = aTerm * aTerm

        // A³
        val a3 = a2 * aTerm

        // A⁴
        val a4 = a2 * a2

        // A⁵
        val a5 = a4 * aTerm

        // A⁶
        val a6 = a5 * aTerm

        // UTM Easting
        val easting =
            k0 * n * (
                    aTerm
                            + (1.0 - t + c) * a3 / 6.0
                            + (
                            5.0
                                    - 18.0 * t
                                    + t * t
                                    + 72.0 * c
                                    - 58.0 * ep2
                            ) * a5 / 120.0
                    ) + FALSE_EASTING

        // UTM Northing
        var northing =
            k0 * (
                    m
                            + n * tanPhi * (
                            a2 / 2.0
                                    + (
                                    5.0
                                            - t
                                            + 9.0 * c
                                            + 4.0 * c * c
                                    ) * a4 / 24.0
                                    + (
                                    61.0
                                            - 58.0 * t
                                            + t * t
                                            + 600.0 * c
                                            - 330.0 * ep2
                                    ) * a6 / 720.0
                            )
                    )

        // Southern Hemisphere false northing
        if (lat < 0.0) {
            northing += 10_000_000.0
        } else {
            northing += FALSE_NORTHING_NORTHERN
        }

        return Pair(
            easting,
            northing
        )
    }

    // ============================================================
    // BASE SHIFT
    // ============================================================

    /**
     * Applies a simple 2D translation/base shift.
     *
     * New Easting  = Easting + Shift East
     * New Northing = Northing + Shift North
     */
    fun applyBaseShift(
        e: Double,
        n: Double,
        shiftE: Double,
        shiftN: Double
    ): Pair<Double, Double> {

        return Pair(
            e + shiftE,
            n + shiftN
        )
    }

    // ============================================================
    // POLYGON AREA
    // ============================================================

    /**
     * Calculates polygon area using Shoelace Formula.
     *
     * Coordinates:
     * Pair(Easting, Northing)
     *
     * Result:
     * Square metres
     */
    fun calculateArea(
        coords: List<Pair<Double, Double>>
    ): Double {

        if (coords.size < 3) {
            return 0.0
        }

        var area = 0.0

        for (i in coords.indices) {

            val j = (i + 1) % coords.size

            val x1 = coords[i].first
            val y1 = coords[i].second

            val x2 = coords[j].first
            val y2 = coords[j].second

            area += (x1 * y2) - (x2 * y1)
        }

        return abs(area) / 2.0
    }

    // ============================================================
    // AREA CONVERSION
    // ============================================================

    /**
     * Converts square metres into commonly used survey units.
     *
     * Returns:
     * SqM
     * Acres
     * Hectares
     * SqFt
     */
    fun convertAreaToUnits(
        areaSqM: Double
    ): Map<String, Double> {

        if (areaSqM < 0.0) {
            return emptyMap()
        }

        return mapOf(

            "SqM" to areaSqM,

            "SqFt" to areaSqM * 10.7639104167,

            "Acres" to areaSqM * 0.000247105381,

            "Hectares" to areaSqM * 0.0001
        )
    }

    // ============================================================
    // DISTANCE
    // ============================================================

    /**
     * Calculates straight-line distance between two
     * projected coordinates.
     *
     * Result is in metres.
     */
    fun calculateDistance(
        e1: Double,
        n1: Double,
        e2: Double,
        n2: Double
    ): Double {

        val dE = e2 - e1
        val dN = n2 - n1

        return sqrt(
            dE * dE +
                    dN * dN
        )
    }

    // ============================================================
    // POLYGON PERIMETER
    // ============================================================

    /**
     * Calculates total perimeter of a polygon.
     *
     * Result is in metres.
     */
    fun calculatePerimeter(
        coords: List<Pair<Double, Double>>
    ): Double {

        if (coords.size < 2) {
            return 0.0
        }

        var perimeter = 0.0

        for (i in coords.indices) {

            val j = (i + 1) % coords.size

            perimeter += calculateDistance(
                coords[i].first,
                coords[i].second,
                coords[j].first,
                coords[j].second
            )
        }

        return perimeter
    }

    // ============================================================
    // FORMAT HELPERS
    // ============================================================

    fun formatCoordinate(
        value: Double
    ): String {

        return String.format(
            java.util.Locale.US,
            "%.3f",
            value
        )
    }

    fun formatArea(
        areaSqM: Double
    ): String {

        val units = convertAreaToUnits(areaSqM)

        return String.format(
            java.util.Locale.US,
            "Area: %.2f Sq.m\nAcres: %.4f\nHectares: %.4f\nSq.ft: %.2f",
            units["SqM"] ?: 0.0,
            units["Acres"] ?: 0.0,
            units["Hectares"] ?: 0.0,
            units["SqFt"] ?: 0.0
        )
    }
}