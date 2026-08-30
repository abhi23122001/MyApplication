package com.shahsurveyors.myapplication.utils

import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Geodetic / UTM calculation utilities.
 *
 * Datum: WGS84
 *
 * Supports:
 * - Latitude / Longitude -> UTM
 * - Automatic UTM zone calculation
 * - UTM central meridian calculation
 * - Base shift calculation
 *
 * UTM coordinates are returned as:
 * Pair(Easting, Northing)
 */
object GeodeticCalculator {

    // ============================================================
    // WGS84 ELLIPSOID
    // ============================================================

    private const val SEMI_MAJOR_AXIS = 6378137.0

    private const val INVERSE_FLATTENING = 298.257223563


    // ============================================================
    // UTM CONSTANTS
    // ============================================================

    private const val SCALE_FACTOR = 0.9996

    private const val FALSE_EASTING = 500000.0

    private const val FALSE_NORTHING_SOUTH = 10000000.0


    // ============================================================
    // WGS84 LAT/LONG -> UTM
    // ============================================================

    /**
     * Converts WGS84 latitude / longitude to UTM.
     *
     * UTM zone is calculated automatically from longitude.
     *
     * Returns:
     * Pair(
     *     easting,
     *     northing
     * )
     */
    fun latLongToUTM(
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {

        require(latitude in -80.0..84.0) {
            "Latitude must be between -80° and 84°"
        }

        require(longitude in -180.0..180.0) {
            "Longitude must be between -180° and 180°"
        }


        // --------------------------------------------------------
        // WGS84 parameters
        // --------------------------------------------------------

        val flattening =
            1.0 / INVERSE_FLATTENING

        val eccentricitySquared =
            2.0 * flattening -
                    flattening.pow(2)

        val secondEccentricitySquared =
            eccentricitySquared /
                    (1.0 - eccentricitySquared)


        // --------------------------------------------------------
        // Automatically determine UTM zone
        // --------------------------------------------------------

        val zone =
            getUtmZone(longitude)

        val centralMeridian =
            getCentralMeridian(zone)


        // --------------------------------------------------------
        // Convert degrees -> radians
        // --------------------------------------------------------

        val latRad =
            Math.toRadians(latitude)

        val lonRad =
            Math.toRadians(longitude)

        val centralMeridianRad =
            Math.toRadians(centralMeridian)


        // --------------------------------------------------------
        // Trigonometric values
        // --------------------------------------------------------

        val sinLat =
            sin(latRad)

        val cosLat =
            cos(latRad)

        val tanLat =
            tan(latRad)


        // --------------------------------------------------------
        // Radius of curvature
        // --------------------------------------------------------

        val radiusOfCurvature =
            SEMI_MAJOR_AXIS /
                    sqrt(
                        1.0 -
                                eccentricitySquared *
                                sinLat.pow(2)
                    )


        // --------------------------------------------------------
        // UTM variables
        // --------------------------------------------------------

        val t =
            tanLat.pow(2)

        val c =
            secondEccentricitySquared *
                    cosLat.pow(2)

        val a =
            (lonRad - centralMeridianRad) *
                    cosLat


        // ========================================================
        // MERIDIAN ARC
        // ========================================================

        val meridianArc =
            SEMI_MAJOR_AXIS * (

                    (
                            1.0
                                    - eccentricitySquared / 4.0
                                    - 3.0 *
                                    eccentricitySquared.pow(2) / 64.0
                                    - 5.0 *
                                    eccentricitySquared.pow(3) / 256.0
                            ) * latRad

                            - (

                            3.0 *
                                    eccentricitySquared / 8.0

                                    + 3.0 *
                                    eccentricitySquared.pow(2) / 32.0

                                    + 45.0 *
                                    eccentricitySquared.pow(3) / 1024.0

                            ) * sin(
                        2.0 * latRad
                    )

                            + (

                            15.0 *
                                    eccentricitySquared.pow(2) / 256.0

                                    + 45.0 *
                                    eccentricitySquared.pow(3) / 1024.0

                            ) * sin(
                        4.0 * latRad
                    )

                            - (

                            35.0 *
                                    eccentricitySquared.pow(3) / 3072.0

                            ) * sin(
                        6.0 * latRad
                    )
                    )


        // ========================================================
        // UTM EASTING
        // ========================================================

        val easting =
            SCALE_FACTOR *
                    radiusOfCurvature *
                    (

                            a

                                    + (
                                    1.0
                                            - t
                                            + c
                                    ) *
                                    a.pow(3) / 6.0

                                    + (

                                    5.0
                                            - 18.0 * t
                                            + t.pow(2)
                                            + 72.0 * c
                                            - 58.0 *
                                            secondEccentricitySquared

                                    ) *
                                    a.pow(5) / 120.0

                            ) +
                    FALSE_EASTING


        // ========================================================
        // UTM NORTHING
        // ========================================================

        var northing =
            SCALE_FACTOR *
                    (

                            meridianArc

                                    + radiusOfCurvature *
                                    tanLat *
                                    (

                                            a.pow(2) / 2.0

                                                    + (

                                                    5.0
                                                            - t
                                                            + 9.0 * c
                                                            + 4.0 *
                                                            c.pow(2)

                                                    ) *
                                                    a.pow(4) / 24.0

                                                    + (

                                                    61.0
                                                            - 58.0 * t
                                                            + t.pow(2)
                                                            + 600.0 * c
                                                            - 330.0 *
                                                            secondEccentricitySquared

                                                    ) *
                                                    a.pow(6) / 720.0

                                            )
                            )


        // --------------------------------------------------------
        // Southern hemisphere false northing
        // --------------------------------------------------------

        if (latitude < 0.0) {

            northing +=
                FALSE_NORTHING_SOUTH
        }


        return Pair(
            easting,
            northing
        )
    }


    // ============================================================
    // UTM ZONE 44N
    // ============================================================

    /**
     * Converts WGS84 coordinates specifically to UTM Zone 44.
     *
     * Zone 44 central meridian = 81°E.
     *
     * Useful for Singrauli / central India survey work.
     */
    fun latLongToUTM44N(
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {

        require(
            getUtmZone(longitude) == 44
        ) {
            "Longitude $longitude is not inside UTM Zone 44"
        }

        return latLongToUTM(
            latitude = latitude,
            longitude = longitude
        )
    }


    // ============================================================
    // WGS84 -> UTM 44N
    // ============================================================

    /**
     * Convenience method for Zone 44N.
     */
    fun wgs84ToUtm44N(
        latitude: Double,
        longitude: Double
    ): Pair<Double, Double> {

        return latLongToUTM44N(
            latitude = latitude,
            longitude = longitude
        )
    }


    // ============================================================
    // APPLY BASE SHIFT
    // ============================================================

    /**
     * Applies a simple 2D coordinate base shift.
     *
     * Easting  = Easting + dE
     * Northing = Northing + dN
     */
    fun applyBaseShift(
        east: Double,
        north: Double,
        dE: Double,
        dN: Double
    ): Pair<Double, Double> {

        return Pair(
            east + dE,
            north + dN
        )
    }


    // ============================================================
    // GET UTM ZONE
    // ============================================================

    /**
     * Calculates UTM zone from longitude.
     *
     * Example:
     *
     * 81°E -> Zone 44
     * 82°E -> Zone 44
     * 83°E -> Zone 44
     * 87°E -> Zone 45
     */
    fun getUtmZone(
        longitude: Double
    ): Int {

        require(longitude in -180.0..180.0) {
            "Longitude must be between -180° and 180°"
        }

        return (
                (longitude + 180.0) / 6.0
                ).toInt() + 1
    }


    // ============================================================
    // CENTRAL MERIDIAN
    // ============================================================

    /**
     * Returns the central meridian for a UTM zone.
     *
     * Zone 44 -> 81°E
     * Zone 45 -> 87°E
     */
    fun getCentralMeridian(
        zone: Int
    ): Double {

        require(zone in 1..60) {
            "UTM zone must be between 1 and 60"
        }

        return zone * 6.0 - 183.0
    }
}