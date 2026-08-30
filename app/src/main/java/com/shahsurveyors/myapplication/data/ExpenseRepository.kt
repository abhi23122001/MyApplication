package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ExpenseRecord
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val expensesCollection = firestore.collection(FirebaseConstants.COLLECTION_EXPENSES)
    private val usersCollection = firestore.collection(FirebaseConstants.COLLECTION_USERS)
    private val notificationRepository = NotificationRepository(firestore)

    private fun requireCurrentUid(): String =
        auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Authentication required")

    private suspend fun requireAdminUid(): String {
        val uid = requireCurrentUid()
        val profile = usersCollection.document(uid).get().await()
            .toObject(com.shahsurveyors.myapplication.models.UserProfile::class.java)
        require(profile?.active == true && profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            "Admin authorization required"
        }
        return uid
    }

    suspend fun getExpensesForUser(uid: String): List<ExpenseRecord> = try {
        val currentUid = requireCurrentUid()
        require(uid == currentUid) { "Users may only access their own expenses" }
        expensesCollection.whereEqualTo("uid", currentUid).get().await()
            .toObjects(ExpenseRecord::class.java)
            .sortedByDescending { it.date?.seconds ?: 0L }
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun getAllExpenses(): List<ExpenseRecord> {
        requireAdminUid()
        return expensesCollection.get().await()
            .toObjects(ExpenseRecord::class.java)
            .sortedByDescending { it.date?.seconds ?: 0L }
    }

    suspend fun hasDuplicateExpense(uid: String, duplicateKey: String): Boolean {
        if (uid.isBlank() || duplicateKey.isBlank()) return false
        return getExpensesForUser(uid).any { it.duplicateKey == duplicateKey && it.status != "REJECTED" }
    }

    suspend fun saveExpense(expense: ExpenseRecord) {
        val currentUid = requireCurrentUid()
        val id = expense.id.ifBlank { expensesCollection.document().id }

        // Ownership is always taken from Firebase Authentication, never from the caller.
        val saved = expense.copy(
            id = id,
            uid = currentUid,
            status = "PENDING",
            paymentStatus = "UNPAID",
            reviewedByUid = "",
            reviewedByName = ""
        )

        expensesCollection.document(id).set(saved).await()
        runCatching {
            notificationRepository.createForAdmins(
                type = "EXPENSE_SUBMITTED",
                title = "New Expense Request",
                message = "${saved.userName.ifBlank { "Employee" }} submitted an expense of ₹${"%.2f".format(saved.amount)}${saved.projectName.takeIf { it.isNotBlank() }?.let { " for $it" } ?: ""}.",
                actorUid = currentUid,
                actorName = saved.userName,
                referenceId = id,
                route = "expense"
            )
        }
    }

    suspend fun updateExpenseReview(
        id: String,
        status: String,
        adminRemark: String,
        reviewerUid: String,
        reviewerName: String
    ) {
        requireAdminUid()
        require(reviewerUid == auth.currentUser?.uid) { "Reviewer must be the authenticated admin" }
        val normalizedStatus = status.uppercase()
        require(normalizedStatus == "APPROVED" || normalizedStatus == "REJECTED") { "Invalid expense status" }
        val updates = mutableMapOf<String, Any>(
            "status" to normalizedStatus,
            "adminRemark" to adminRemark.trim(),
            "reviewedByUid" to reviewerUid,
            "reviewedByName" to reviewerName,
            "reviewedAt" to Timestamp.now()
        )
        updates["paymentStatus"] = if (normalizedStatus == "REJECTED") "NOT_PAYABLE" else "UNPAID"
        expensesCollection.document(id).update(updates).await()
    }

    suspend fun markExpensePaid(id: String) {
        val uid = requireAdminUid()
        expensesCollection.document(id).update(
            mapOf("paymentStatus" to "PAID", "paidByUid" to uid, "paidAt" to Timestamp.now())
        ).await()
    }

    suspend fun markExpenseUnpaid(id: String) {
        requireAdminUid()
        expensesCollection.document(id).update(
            mapOf("paymentStatus" to "UNPAID", "paidByUid" to "", "paidAt" to null)
        ).await()
    }
}
