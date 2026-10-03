package com.shahsurveyors.myapplication.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    /**
     * Attendance selfies are stored under the authenticated user's UID.
     * This matches storage.rules: attendance/selfies/{uid}/{fileName}
     */
    suspend fun uploadBytes(
        path: String,
        uid: String,
        bytes: ByteArray
    ): String {
        require(uid.isNotBlank()) { "User ID is missing" }

        val fileName = path.trimEnd('/') + "/" + uid + "/" + java.util.UUID.randomUUID() + ".jpg"
        val reference = storage.reference.child(fileName)

        reference.putBytes(bytes).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun uploadFile(
        path: String,
        uid: String,
        uri: Uri
    ): String {
        require(uid.isNotBlank()) { "User ID is missing" }

        val fileName = path.trimEnd('/') + "/" + uid + "/" + java.util.UUID.randomUUID() + ".jpg"
        val reference = storage.reference.child(fileName)

        reference.putFile(uri).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun deleteFile(url: String) {
        if (url.isBlank()) return
        runCatching {
            FirebaseStorage.getInstance().getReferenceFromUrl(url).delete().await()
        }
    }
}
