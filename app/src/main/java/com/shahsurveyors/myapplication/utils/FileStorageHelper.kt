package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileStorageHelper {

    /**
     * Saves an image selected through Android's content picker
     * into the application's private internal storage.
     *
     * Returns the absolute file path on success.
     * Returns null if saving fails.
     */
    fun saveImageToInternalStorage(
        context: Context,
        uri: Uri,
        fileName: String
    ): String? {

        return try {

            if (fileName.isBlank()) {
                return null
            }

            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: return null

            val safeFileName = fileName
                .replace("/", "_")
                .replace("\\", "_")

            val file = File(
                context.filesDir,
                safeFileName
            )

            inputStream.use { input ->

                FileOutputStream(
                    file,
                    false
                ).use { output ->

                    input.copyTo(output)
                    output.flush()
                }
            }

            if (file.exists() && file.length() > 0L) {
                file.absolutePath
            } else {
                null
            }

        } catch (e: Exception) {

            e.printStackTrace()

            null
        }
    }

    /**
     * Checks whether a stored image path is still available.
     */
    fun isFileAvailable(path: String?): Boolean {
        if (path.isNullOrBlank()) {
            return false
        }

        return try {
            File(path).exists()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Deletes a stored image from internal storage.
     */
    fun deleteFile(path: String?): Boolean {

        if (path.isNullOrBlank()) {
            return false
        }

        return try {
            val file = File(path)

            if (file.exists()) {
                file.delete()
            } else {
                false
            }

        } catch (_: Exception) {
            false
        }
    }
}