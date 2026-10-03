package com.shahsurveyors.myapplication.data

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /**
     * Uploads an attendance selfie to Firebase Storage and returns its download URL.
     *
     * This replaces the old Supabase dependency so attendance uses the same
     * Firebase project as Authentication, Firestore and the rest of the ERP.
     */
    suspend fun uploadBytes(
        path: String,
        bytes: ByteArray
    ): String {
        val fileName = "${path.trimEnd('/')}/${java.util.UUID.randomUUID()}.jpg"
        val reference = storage.reference.child(fileName)

        reference.putBytes(bytes).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun uploadFile(
        path: String,
        uri: Uri
    ): String {
        val fileName = "${path.trimEnd('/')}/${java.util.UUID.randomUUID()}.jpg"
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
