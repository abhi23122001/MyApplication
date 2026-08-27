package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.EquipmentModel
import com.shahsurveyors.myapplication.models.EquipmentHandoverRecord
import com.shahsurveyors.myapplication.models.ProjectModel
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.tasks.await

class EquipmentRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    private val equipmentCollection = firestore.collection(FirebaseConstants.COLLECTION_EQUIPMENT)
    private val historyCollection = firestore.collection("equipment_handover_history")

    suspend fun getAllEquipment(): List<EquipmentModel> = equipmentCollection.get().await().toObjects(EquipmentModel::class.java)
    suspend fun saveEquipment(equipment: EquipmentModel) { val id=equipment.id.ifBlank{equipmentCollection.document().id}; equipmentCollection.document(id).set(equipment.copy(id=id)).await() }
    suspend fun updateEquipmentStatus(id:String,status:String,uid:String?,name:String?) { equipmentCollection.document(id).update(mapOf("status" to status,"assignedToUid" to uid,"assignedToName" to name)).await() }
    suspend fun getActiveEmployees():List<UserProfile> = try { firestore.collection(FirebaseConstants.COLLECTION_USERS).whereEqualTo("active",true).whereEqualTo("approved",true).get().await().toObjects(UserProfile::class.java).filter{it.role.lowercase()!="admin"}.sortedBy{it.name.lowercase()} } catch(_:Exception){emptyList()}
    suspend fun getActiveProjects():List<ProjectModel> = try { firestore.collection(FirebaseConstants.COLLECTION_PROJECTS).whereEqualTo("status","ACTIVE").get().await().toObjects(ProjectModel::class.java).sortedBy{it.name.lowercase()} } catch(_:Exception){emptyList()}
    suspend fun getHandoverHistory(equipmentId:String):List<EquipmentHandoverRecord> = try { historyCollection.whereEqualTo("equipmentId",equipmentId).get().await().documents.map{d->EquipmentHandoverRecord(id=d.id,equipmentId=d.getString("equipmentId")?:"",equipmentName=d.getString("equipmentName")?:"",action=d.getString("action")?:"",employeeUid=d.getString("employeeUid")?:"",employeeName=d.getString("employeeName")?:"",projectId=d.getString("projectId")?:"",projectName=d.getString("projectName")?:"",location=d.getString("location")?:"",accessories=(d.get("accessories") as? List<*>)?.filterIsInstance<String>()?:emptyList(),remarks=d.getString("remarks")?:"",actionAt=d.getTimestamp("actionAt")?:d.getTimestamp("timestamp"),actionByUid=d.getString("actionByUid")?:d.getString("performedByUid")?:"",actionByName=d.getString("actionByName")?:d.getString("performedByName")?:"" )}.sortedByDescending{it.actionAt?.toDate()?.time?:0L} } catch(_:Exception){emptyList()}

    suspend fun saveHandover(id:String,employee:UserProfile,project:ProjectModel,location:String,accessories:List<String>,handoverByUid:String,handoverByName:String) {
        require(employee.uid.isNotBlank()) { "Please select an employee" }; require(project.id.isNotBlank()) { "Please select a project" }; require(location.isNotBlank()) { "Please enter handover location" }
        val now=Timestamp.now(); val cleanAccessories=accessories.map{it.trim()}.filter{it.isNotBlank()}.distinct()
        val updates=mapOf<String,Any>("status" to "IN_USE","assignedToUid" to employee.uid,"assignedToName" to employee.name,"assignedProjectId" to project.id,"assignedProjectName" to project.name,"handoverLocation" to location.trim(),"handoverAt" to now,"handoverByUid" to handoverByUid,"handoverByName" to handoverByName,"handoverAccessories" to cleanAccessories)
        equipmentCollection.document(id).update(updates).await()
        historyCollection.document().set(mapOf("equipmentId" to id,"equipmentName" to (getAllEquipment().firstOrNull{it.id==id}?.name ?: ""),"action" to "HANDOVER","employeeUid" to employee.uid,"employeeName" to employee.name,"projectId" to project.id,"projectName" to project.name,"location" to location.trim(),"accessories" to cleanAccessories,"actionByUid" to handoverByUid,"actionByName" to handoverByName,"actionAt" to now)).await()
    }

    suspend fun returnEquipment(id:String,location:String,remarks:String) {
        val admin=FirebaseAuth.getInstance().currentUser; val now=Timestamp.now(); val current=getAllEquipment().firstOrNull{it.id==id}
        historyCollection.document().set(mapOf("equipmentId" to id,"equipmentName" to (current?.name ?: ""),"action" to "RETURN","employeeUid" to current?.assignedToUid,"employeeName" to current?.assignedToName,"projectId" to current?.assignedProjectId,"projectName" to current?.assignedProjectName,"location" to location.trim(),"accessories" to (current?.handoverAccessories ?: emptyList<String>()),"remarks" to remarks.trim(),"actionByUid" to (admin?.uid ?: "ADMIN"),"actionByName" to (admin?.displayName ?: "Admin"),"actionAt" to now)).await()
        equipmentCollection.document(id).update(mapOf("status" to "AVAILABLE","assignedToUid" to null,"assignedToName" to null,"assignedProjectId" to null,"assignedProjectName" to null,"handoverLocation" to null,"lastReturnAt" to now,"lastReturnByUid" to (admin?.uid ?: "ADMIN"),"lastReturnByName" to (admin?.displayName ?: "Admin"),"lastReturnLocation" to location.trim(),"returnRemarks" to remarks.trim())).await()
    }
}
