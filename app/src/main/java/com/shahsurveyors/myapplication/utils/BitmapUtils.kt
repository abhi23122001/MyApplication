package com.shahsurveyors.myapplication.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

object BitmapUtils {

    /**
     * Resize + JPEG compress bitmap.
     *
     * @param bitmap Source bitmap
     * @param maxDimension Maximum width/height
     * @param targetSizeKb Target maximum size in KB
     * @param quality Starting JPEG quality
     */
    fun compressBitmap(
        bitmap: Bitmap,
        maxDimension: Int = 1024,
        targetSizeKb: Int = 240,
        quality: Int = 70
    ): ByteArray {

        require(maxDimension > 0) {
            "maxDimension must be greater than 0"
        }

        require(targetSizeKb > 0) {
            "targetSizeKb must be greater than 0"
        }

        val safeQuality = quality.coerceIn(5, 100)

        // ------------------------------------------------------------
        // STEP 1: Resize
        // ------------------------------------------------------------

        val width = bitmap.width
        val height = bitmap.height

        val scaledBitmap: Bitmap

        if (width > maxDimension || height > maxDimension) {

            val scale = maxDimension.toFloat() / max(width, height)

            val matrix = Matrix().apply {
                postScale(scale, scale)
            }

            scaledBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                width,
                height,
                matrix,
                true
            )

        } else {
            scaledBitmap = bitmap
        }

        // ------------------------------------------------------------
        // STEP 2: JPEG Compression
        // ------------------------------------------------------------

        val targetBytes = targetSizeKb * 1024

        var currentQuality = safeQuality
        var result: ByteArray

        do {

            val outputStream = ByteArrayOutputStream()

            scaledBitmap.compress(
                Bitmap.CompressFormat.JPEG,
                currentQuality,
                outputStream
            )

            result = outputStream.toByteArray()

            outputStream.close()

            currentQuality -= 5

        } while (
            result.size > targetBytes &&
            currentQuality >= 5
        )

        // Don't recycle if the original bitmap was used directly.
        if (scaledBitmap !== bitmap && !scaledBitmap.isRecycled) {
            scaledBitmap.recycle()
        }

        return result
    }

    /**
     * Convert image bytes to Base64 without line breaks.
     */
    fun toBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(
            byteArray,
            Base64.NO_WRAP
        )
    }

    /**
     * Convert Base64 back to ByteArray.
     */
    fun fromBase64(base64: String): ByteArray {
        return Base64.decode(
            base64,
            Base64.NO_WRAP
        )
    }
}