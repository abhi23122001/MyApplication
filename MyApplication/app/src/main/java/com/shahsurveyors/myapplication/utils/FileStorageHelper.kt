package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object FileStorageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri, fileName: String): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
