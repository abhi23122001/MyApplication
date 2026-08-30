package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.LeaveRequestModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LeaveRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val leaveCollection = firestore.collection("leaveRequests")
    private val attendanceCollection = firestore.collection(FirebaseConstants.COLLECTION_ATTENDANCE)
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun requireCurrentUid(): String = auth.currentUser?.uid?.takeIf { it.isNotBlank() }
        ?: throw IllegalStateException("Authentication required")

    private suspend fun requireAdmin(): String {
        val uid = requireCurrentUid()
        val profile = firestore.collection(FirebaseConstants.COLLECTION_USERS).document(uid).get()
            .await().toObject(com.shahsurveyors.myapplication.models.UserProfile::class.java)
        require(profile?.active == true && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Admin authorization required"
        }
        return uid
    }

    suspend fun getRequestsForUser(uid: String): List<LeaveRequestModel> {
        val currentUid = requireCurrentUid()
        require(uid == currentUid) { "Users may only access their own leave requests" }
        return leaveCollection.whereEqualTo("userUid", currentUid).get().await()
            .toObjects(LeaveRequestModel::class.java)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    suspend fun getAllRequests(): List<LeaveRequestModel> {
        requireAdmin()
        return leaveCollection.get().await().toObjects(LeaveRequestModel::class.java)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    suspend fun submitRequest(request: LeaveRequestModel): String {
        val currentUid = requireCurrentUid()
        require(request.employeeName.isNotBlank()) { "Employee name is required." }
        require(request.leaveType in LEAVE_TYPES) { "Invalid leave type." }
        require(request.reason.isNotBlank()) { "Leave reason is required." }
        validateDates(request.fromDate, request.toDate)

        val existing = getRequestsForUser(currentUid)
        val newFrom = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val newTo = LocalDate.parse(request.toDate.trim(), dateFormatter)
        val overlaps = existing.any { old ->
            old.status != STATUS_REJECTED && old.status != STATUS_CANCELLED && runCatching {
                val oldFrom = LocalDate.parse(old.fromDate.trim(), dateFormatter)
                val oldTo = LocalDate.parse(old.toDate.trim(), dateFormatter)
                !newTo.isBefore(oldFrom) && !newFrom.isAfter(oldTo)
            }.getOrDefault(false)
        }
        require(!overlaps) { "A leave request already exists for one or more selected dates." }

        val ref = if (request.id.isBlank()) leaveCollection.document() else leaveCollection.document(request.id)
        val saved = request.copy(
            id = ref.id, userUid = currentUid, status = STATUS_PENDING,
            approvedBy = "", adminRemark = "",
            createdAt = request.createdAt ?: Timestamp.now(), updatedAt = Timestamp.now()
        )
        ref.set(saved).await()
        return ref.id
    }

    suspend fun updateStatus(id: String, status: String, actionBy: String, adminRemark: String = "") {
        requireAdmin()
        require(id.isNotBlank()) { "Leave request id is required." }
        require(status in ALLOWED_STATUSES) { "Invalid leave status" }
        val requestRef = leaveCollection.document(id)
        val request = requestRef.get().await().toObject(LeaveRequestModel::class.java)
            ?: throw IllegalArgumentException("Leave request not found")
        require(actionBy == requireCurrentUid()) { "Action user must be the authenticated admin" }

        when (request.status) {
            STATUS_PENDING -> Unit
            STATUS_APPROVED -> require(status == STATUS_CANCELLED) { "An approved leave can only be cancelled." }
            STATUS_REJECTED, STATUS_CANCELLED -> throw IllegalStateException("This leave request is already ${request.status.lowercase()}.")
            else -> throw IllegalStateException("Unknown leave request status.")
        }

        val cleanedRemark = adminRemark.trim()
        when (status) {
            STATUS_APPROVED -> {
                createOrUpdateApprovedLeaveAttendance(request)
                requestRef.update(mapOf("status" to STATUS_APPROVED, "approvedBy" to requireCurrentUid(), "adminRemark" to cleanedRemark, "updatedAt" to Timestamp.now())).await()
            }
            STATUS_REJECTED -> requestRef.update(mapOf("status" to STATUS_REJECTED, "approvedBy" to requireCurrentUid(), "adminRemark" to cleanedRemark, "updatedAt" to Timestamp.now())).await()
            STATUS_CANCELLED -> {
                if (request.status == STATUS_APPROVED) cancelApprovedLeaveAttendance(request)
                requestRef.update(mapOf("status" to STATUS_CANCELLED, "approvedBy" to request.approvedBy, "adminRemark" to cleanedRemark, "updatedAt" to Timestamp.now())).await()
            }
        }
    }

    private suspend fun cancelApprovedLeaveAttendance(request: LeaveRequestModel) {
        val from = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val to = LocalDate.parse(request.toDate.trim(), dateFormatter)
        datesBetween(from, to).chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch(); var writes = 0
            chunk.forEach { date ->
                val ref = attendanceCollection.document("${request.userUid}_${date.format(dateFormatter)}")
                val snapshot = ref.get().await()
                if (snapshot.exists() && snapshot.getString("leaveRequestId") == request.id && snapshot.getString("status") == STATUS_APPROVED_LEAVE) { batch.delete(ref); writes++ }
            }
            if (writes > 0) batch.commit().await()
        }
    }

    private fun validateDates(fromDate: String, toDate: String) {
        val from = runCatching { LocalDate.parse(fromDate.trim(), dateFormatter) }.getOrElse { throw IllegalArgumentException("Invalid From Date. Use yyyy-MM-dd") }
        val to = runCatching { LocalDate.parse(toDate.trim(), dateFormatter) }.getOrElse { throw IllegalArgumentException("Invalid To Date. Use yyyy-MM-dd") }
        require(!to.isBefore(from)) { "To Date cannot be before From Date" }
    }

    private suspend fun createOrUpdateApprovedLeaveAttendance(request: LeaveRequestModel) {
        val from = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val to = LocalDate.parse(request.toDate.trim(), dateFormatter)
        datesBetween(from, to).chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch(); var writes = 0
            chunk.forEach { date ->
                val dateString = date.format(dateFormatter)
                val ref = attendanceCollection.document("${request.userUid}_$dateString")
                val existing = ref.get().await()
                if (existing.exists()) {
                    val existingId = existing.getString("leaveRequestId") ?: ""
                    val existingStatus = existing.getString("status") ?: ""
                    if (existingId == request.id && existingStatus == STATUS_APPROVED_LEAVE) return@forEach
                    require(existing.getTimestamp("punchInTime") == null && existing.getTimestamp("punchOutTime") == null) { "Cannot approve leave for $dateString because attendance is already marked/worked." }
                    require(existingStatus != STATUS_APPROVED_LEAVE) { "$dateString is already covered by another approved leave request." }
                    batch.update(ref, mapOf("userName" to request.employeeName, "status" to STATUS_APPROVED_LEAVE, "punchInTime" to null, "punchOutTime" to null, "punchInLat" to null, "punchInLng" to null, "punchOutLat" to null, "punchOutLng" to null, "siteName" to "", "selfieUrl" to null, "lateMinutes" to 0, "earlyOutMinutes" to 0, "workingMinutes" to 0, "overtimeMinutes" to 0, "isLate" to false, "isEarlyOut" to false, "punchOutMissing" to false, "leaveRequestId" to request.id))
                } else {
                    batch.set(ref, mapOf("id" to ref.id, "uid" to request.userUid, "userName" to request.employeeName, "date" to dateString, "punchInTime" to null, "punchOutTime" to null, "punchInLat" to null, "punchInLng" to null, "punchOutLat" to null, "punchOutLng" to null, "siteName" to "", "selfieUrl" to null, "status" to STATUS_APPROVED_LEAVE, "lateMinutes" to 0, "earlyOutMinutes" to 0, "workingMinutes" to 0, "overtimeMinutes" to 0, "isLate" to false, "isEarlyOut" to false, "punchOutMissing" to false, "leaveRequestId" to request.id))
                }
                writes++
            }
            if (writes > 0) batch.commit().await()
        }
    }

    private fun datesBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>(); var date = from
        while (!date.isAfter(to)) { result += date; date = date.plusDays(1) }
        return result
    }

    companion object {
        private const val STATUS_PENDING = "PENDING"
        private const val STATUS_APPROVED = "APPROVED"
        private const val STATUS_REJECTED = "REJECTED"
        private const val STATUS_CANCELLED = "CANCELLED"
        private const val STATUS_APPROVED_LEAVE = "APPROVED_LEAVE"
        private const val MAX_BATCH_WRITES = 450
        private val LEAVE_TYPES = setOf("CASUAL", "SICK", "PAID", "UNPAID", "EMERGENCY")
        private val ALLOWED_STATUSES = setOf(STATUS_APPROVED, STATUS_REJECTED, STATUS_CANCELLED)
    }
}
