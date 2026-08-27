package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.DSRModel
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class DSRRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val dsrCollection = firestore.collection(FirebaseConstants.COLLECTION_DSR)
    suspend fun getDSRForProject(projectId:String):List<DSRModel> = try { dsrCollection.whereEqualTo("projectId",projectId).orderBy("date",Query.Direction.DESCENDING).get().await().toObjects(DSRModel::class.java) } catch(_:Exception){ emptyList() }
    suspend fun saveDSR(dsr:DSRModel){ require(dsr.uid.isNotBlank()){ "User session is required" }; require(dsr.projectId.isNotBlank()){ "Project is required" }; require(dsr.workDone.isNotBlank()){ "Work details are required" }; val today=dsr.date?.toDate()?.let{SimpleDateFormat("yyyy-MM-dd",Locale.US).format(it)} ?: ""; val duplicate=dsrCollection.whereEqualTo("uid",dsr.uid).whereEqualTo("projectId",dsr.projectId).get().await().toObjects(DSRModel::class.java).any{it.date?.toDate()?.let{d->SimpleDateFormat("yyyy-MM-dd",Locale.US).format(d)}==today}; require(!duplicate){"A DSR for this project has already been submitted today"}; val id=dsr.id.ifBlank{dsrCollection.document().id}; dsrCollection.document(id).set(dsr.copy(id=id)).await() }
}
