package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LeaveRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val leaveCollection = firestore.collection("leaveRequests")
    private val attendanceCollection = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun getRequestsForUser(uid: String): List<LeaveRequestModel> {
        return try {
            leaveCollection
                .whereEqualTo("userUid", uid)
                .get()
                .await()
                .toObjects(LeaveRequestModel::class.java)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllRequests(): List<LeaveRequestModel> {
        return try {
            leaveCollection
                .get()
                .await()
                .toObjects(LeaveRequestModel::class.java)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitRequest(request: LeaveRequestModel): String {
        validateDates(request.fromDate, request.toDate)

        val existing = getRequestsForUser(request.userUid)
        val newFrom = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val newTo = LocalDate.parse(request.toDate.trim(), dateFormatter)
        val overlaps = existing.any { old ->
            old.status != "REJECTED" && old.status != "CANCELLED" &&
                runCatching {
                    val oldFrom = LocalDate.parse(old.fromDate.trim(), dateFormatter)
                    val oldTo = LocalDate.parse(old.toDate.trim(), dateFormatter)
                    !newTo.isBefore(oldFrom) && !newFrom.isAfter(oldTo)
                }.getOrDefault(false)
        }
        require(!overlaps) { "A leave request already exists for one or more selected dates." }

        val ref = if (request.id.isBlank()) leaveCollection.document() else leaveCollection.document(request.id)
        val saved = request.copy(
            id = ref.id,
            status = if (request.status.isBlank()) "PENDING" else request.status,
            createdAt = request.createdAt ?: Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        ref.set(saved).await()
        return ref.id
    }

    suspend fun updateStatus(
        id: String,
        status: String,
        approvedBy: String,
        adminRemark: String = ""
    ) {
        require(status in setOf("APPROVED", "REJECTED", "CANCELLED")) { "Invalid leave status" }

        val requestRef = leaveCollection.document(id)
        val request = requestRef.get().await().toObject(LeaveRequestModel::class.java)
            ?: throw IllegalArgumentException("Leave request not found")

        requestRef.update(
            mapOf(
                "status" to status,
                "approvedBy" to approvedBy,
                "adminRemark" to adminRemark.trim(),
                "updatedAt" to Timestamp.now()
            )
        ).await()

        if (status == "APPROVED") {
            createApprovedLeaveAttendance(
                request.copy(
                    status = status,
                    approvedBy = approvedBy,
                    adminRemark = adminRemark.trim()
                )
            )
        }
    }

    private fun validateDates(fromDate: String, toDate: String) {
        val from = runCatching { LocalDate.parse(fromDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid From Date. Use yyyy-MM-dd") }
        val to = runCatching { LocalDate.parse(toDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid To Date. Use yyyy-MM-dd") }
        require(!to.isBefore(from)) { "To Date cannot be before From Date" }
    }

    private suspend fun createApprovedLeaveAttendance(request: LeaveRequestModel) {
        val from = runCatching { LocalDate.parse(request.fromDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid leave From Date. Use yyyy-MM-dd") }
        val to = runCatching { LocalDate.parse(request.toDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid leave To Date. Use yyyy-MM-dd") }

        require(!to.isBefore(from)) { "Leave To Date cannot be before From Date" }

        val batch = firestore.batch()
        var pendingWrites = 0
        var date = from

        while (!date.isAfter(to)) {
            val dateString = date.format(dateFormatter)
            val docId = "${request.userUid}_$dateString"
            val attendanceRef = attendanceCollection.document(docId)
            val existing = attendanceRef.get().await()

            if (!existing.exists()) {
                batch.set(
                    attendanceRef,
                    mapOf(
                        "id" to docId,
                        "uid" to request.userUid,
                        "userName" to request.employeeName,
                        "date" to dateString,
                        "punchInTime" to null,
                        "punchOutTime" to null,
                        "punchInLat" to null,
                        "punchInLng" to null,
                        "punchOutLat" to null,
                        "punchOutLng" to null,
                        "siteName" to "",
                        "selfieUrl" to null,
                        "status" to "APPROVED_LEAVE",
                        "lateMinutes" to 0,
                        "earlyOutMinutes" to 0,
                        "workingMinutes" to 0,
                        "overtimeMinutes" to 0,
                        "isLate" to false,
                        "isEarlyOut" to false,
                        "punchOutMissing" to false,
                        "leaveRequestId" to request.id
                    )
                )
                pendingWrites++
            }
            date = date.plusDays(1)
        }

        if (pendingWrites > 0) batch.commit().await()
    }
}
