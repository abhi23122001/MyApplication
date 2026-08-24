package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ClientModel
import kotlinx.coroutines.tasks.await

class ClientRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val clientsCollection = firestore.collection(FirebaseConstants.COLLECTION_CLIENTS)

    suspend fun getAllClients(): List<ClientModel> {
        return try {
            val snapshot = clientsCollection.get().await()
            snapshot.toObjects(ClientModel::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveClient(client: ClientModel) {
        val id = client.id.ifBlank { clientsCollection.document().id }
        clientsCollection.document(id)
            .set(client.copy(id = id))
            .await()
    }
}
