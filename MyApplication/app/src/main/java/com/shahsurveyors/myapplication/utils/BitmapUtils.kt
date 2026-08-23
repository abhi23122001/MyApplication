package com.shahsurveyors.myapplication.utils

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Base64
import java.io.ByteArrayOutputStream

object BitmapUtils {
    /**
     * Resizes and compresses a bitmap to stay under a specific size (KB).
     * @param bitmap The source bitmap.
     * @param maxDimension The maximum width or height (e.g., 1024).
     * @param targetSizeKb The target size in KB (e.g., 250).
     * @param quality Initial JPEG quality.
     */
    fun compressBitmap(bitmap: Bitmap, maxDimension: Int = 1024, targetSizeKb: Int = 240, quality: Int = 70): ByteArray {
        // Step 1: Scale down if it exceeds max dimension
        val width = bitmap.width
        val height = bitmap.height
        var scaledBitmap = bitmap

        if (width > maxDimension || height > maxDimension) {
            val scale = maxDimension.toFloat() / Math.max(width, height)
            val matrix = Matrix().apply { postScale(scale, scale) }
            scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
        }

        // Step 2: Iterative compression to meet target size
        val outputStream = ByteArrayOutputStream()
        var currentQuality = quality
        var result: ByteArray
        
        do {
            outputStream.reset()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream)
            result = outputStream.toByteArray()
            currentQuality -= 5
        } while (result.size > targetSizeKb * 1024 && currentQuality > 5)

        return result
    }

    fun toBase64(byteArray: ByteArray): String {
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
