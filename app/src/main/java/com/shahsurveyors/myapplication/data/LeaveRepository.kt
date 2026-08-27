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
        if (uid.isBlank()) return emptyList()
        return leaveCollection
            .whereEqualTo("userUid", uid)
            .get()
            .await()
            .toObjects(LeaveRequestModel::class.java)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    suspend fun getAllRequests(): List<LeaveRequestModel> {
        return leaveCollection
            .get()
            .await()
            .toObjects(LeaveRequestModel::class.java)
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
    }

    suspend fun submitRequest(request: LeaveRequestModel): String {
        require(request.userUid.isNotBlank()) { "Employee account not found." }
        require(request.employeeName.isNotBlank()) { "Employee name is required." }
        require(request.leaveType in LEAVE_TYPES) { "Invalid leave type." }
        require(request.reason.isNotBlank()) { "Leave reason is required." }

        validateDates(request.fromDate, request.toDate)

        val existing = getRequestsForUser(request.userUid)
        val newFrom = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val newTo = LocalDate.parse(request.toDate.trim(), dateFormatter)
        val overlaps = existing.any { old ->
            old.status != STATUS_REJECTED && old.status != STATUS_CANCELLED &&
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
            status = STATUS_PENDING,
            approvedBy = "",
            adminRemark = "",
            createdAt = request.createdAt ?: Timestamp.now(),
            updatedAt = Timestamp.now()
        )
        ref.set(saved).await()
        return ref.id
    }

    /**
     * Safe leave state machine:
     * PENDING -> APPROVED / REJECTED / CANCELLED
     * APPROVED -> CANCELLED
     * REJECTED/CANCELLED -> terminal
     *
     * Approval is only committed after attendance has been synchronised successfully.
     */
    suspend fun updateStatus(
        id: String,
        status: String,
        actionBy: String,
        adminRemark: String = ""
    ) {
        require(id.isNotBlank()) { "Leave request id is required." }
        require(actionBy.isNotBlank()) { "Action user is required." }
        require(status in ALLOWED_STATUSES) { "Invalid leave status" }

        val requestRef = leaveCollection.document(id)
        val request = requestRef.get().await().toObject(LeaveRequestModel::class.java)
            ?: throw IllegalArgumentException("Leave request not found")

        when (request.status) {
            STATUS_PENDING -> Unit
            STATUS_APPROVED -> require(status == STATUS_CANCELLED) {
                "An approved leave can only be cancelled."
            }
            STATUS_REJECTED, STATUS_CANCELLED -> throw IllegalStateException(
                "This leave request is already ${request.status.lowercase()}."
            )
            else -> throw IllegalStateException("Unknown leave request status.")
        }

        val cleanedRemark = adminRemark.trim()

        when (status) {
            STATUS_APPROVED -> {
                createOrUpdateApprovedLeaveAttendance(request)
                requestRef.update(
                    mapOf(
                        "status" to STATUS_APPROVED,
                        "approvedBy" to actionBy,
                        "adminRemark" to cleanedRemark,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            }

            STATUS_REJECTED -> {
                requestRef.update(
                    mapOf(
                        "status" to STATUS_REJECTED,
                        "approvedBy" to actionBy,
                        "adminRemark" to cleanedRemark,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            }

            STATUS_CANCELLED -> {
                if (request.status == STATUS_APPROVED) {
                    cancelApprovedLeaveAttendance(request)
                }
                requestRef.update(
                    mapOf(
                        "status" to STATUS_CANCELLED,
                        "approvedBy" to request.approvedBy,
                        "adminRemark" to cleanedRemark,
                        "updatedAt" to Timestamp.now()
                    )
                ).await()
            }
        }
    }

    /** Cancels an already-approved request and removes only attendance created by that request. */
    private suspend fun cancelApprovedLeaveAttendance(request: LeaveRequestModel) {
        val from = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val to = LocalDate.parse(request.toDate.trim(), dateFormatter)
        val dates = datesBetween(from, to)

        dates.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch()
            var writes = 0

            chunk.forEach { date ->
                val attendanceRef = attendanceCollection.document("${request.userUid}_${date.format(dateFormatter)}")
                val snapshot = attendanceRef.get().await()
                if (snapshot.exists() && snapshot.getString("leaveRequestId") == request.id &&
                    snapshot.getString("status") == STATUS_APPROVED_LEAVE
                ) {
                    batch.delete(attendanceRef)
                    writes++
                }
            }

            if (writes > 0) batch.commit().await()
        }
    }

    private fun validateDates(fromDate: String, toDate: String) {
        val from = runCatching { LocalDate.parse(fromDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid From Date. Use yyyy-MM-dd") }
        val to = runCatching { LocalDate.parse(toDate.trim(), dateFormatter) }
            .getOrElse { throw IllegalArgumentException("Invalid To Date. Use yyyy-MM-dd") }
        require(!to.isBefore(from)) { "To Date cannot be before From Date" }
    }

    private suspend fun createOrUpdateApprovedLeaveAttendance(request: LeaveRequestModel) {
        val from = LocalDate.parse(request.fromDate.trim(), dateFormatter)
        val to = LocalDate.parse(request.toDate.trim(), dateFormatter)
        require(!to.isBefore(from)) { "Leave To Date cannot be before From Date" }

        val dates = datesBetween(from, to)
        dates.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch()
            var pendingWrites = 0

            chunk.forEach { date ->
                val dateString = date.format(dateFormatter)
                val attendanceRef = attendanceCollection.document("${request.userUid}_$dateString")
                val existing = attendanceRef.get().await()

                if (existing.exists()) {
                    val existingLeaveRequestId = existing.getString("leaveRequestId") ?: ""
                    val existingStatus = existing.getString("status") ?: ""
                    val hasPunchIn = existing.getTimestamp("punchInTime") != null
                    val hasPunchOut = existing.getTimestamp("punchOutTime") != null

                    if (existingLeaveRequestId == request.id && existingStatus == STATUS_APPROVED_LEAVE) {
                        return@forEach
                    }

                    require(!hasPunchIn && !hasPunchOut) {
                        "Cannot approve leave for $dateString because attendance is already marked/worked."
                    }
                    require(existingStatus != STATUS_APPROVED_LEAVE) {
                        "${dateString} is already covered by another approved leave request."
                    }

                    batch.update(
                        attendanceRef,
                        mapOf(
                            "userName" to request.employeeName,
                            "status" to STATUS_APPROVED_LEAVE,
                            "punchInTime" to null,
                            "punchOutTime" to null,
                            "punchInLat" to null,
                            "punchInLng" to null,
                            "punchOutLat" to null,
                            "punchOutLng" to null,
                            "siteName" to "",
                            "selfieUrl" to null,
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
                } else {
                    batch.set(
                        attendanceRef,
                        mapOf(
                            "id" to attendanceRef.id,
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
                            "status" to STATUS_APPROVED_LEAVE,
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
            }

            if (pendingWrites > 0) batch.commit().await()
        }
    }

    private fun datesBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
        val result = mutableListOf<LocalDate>()
        var date = from
        while (!date.isAfter(to)) {
            result += date
            date = date.plusDays(1)
        }
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
