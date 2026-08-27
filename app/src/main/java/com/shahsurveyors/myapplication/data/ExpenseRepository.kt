package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.shahsurveyors.myapplication.models.ExpenseRecord
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val expensesCollection = firestore.collection(FirebaseConstants.COLLECTION_EXPENSES)
    private val notificationRepository = NotificationRepository(firestore)

    suspend fun getExpensesForUser(uid: String): List<ExpenseRecord> = try {
        expensesCollection.whereEqualTo("uid", uid).get().await().toObjects(ExpenseRecord::class.java).sortedByDescending { it.date?.seconds ?: 0L }
    } catch (_: Exception) { emptyList() }

    suspend fun getAllExpenses(): List<ExpenseRecord> = try {
        expensesCollection.get().await().toObjects(ExpenseRecord::class.java).sortedByDescending { it.date?.seconds ?: 0L }
    } catch (_: Exception) { emptyList() }

    suspend fun hasDuplicateExpense(uid: String, duplicateKey: String): Boolean {
        if (uid.isBlank() || duplicateKey.isBlank()) return false
        return getExpensesForUser(uid).any { it.duplicateKey == duplicateKey && it.status != "REJECTED" }
    }

    suspend fun saveExpense(expense: ExpenseRecord) {
        val id = expense.id.ifBlank { expensesCollection.document().id }
        val saved = expense.copy(id = id)
        expensesCollection.document(id).set(saved).await()
        runCatching {
            notificationRepository.createForAdmins(
                type = "EXPENSE_SUBMITTED",
                title = "New Expense Request",
                message = "${saved.userName.ifBlank { "Employee" }} submitted an expense of ₹${"%.2f".format(saved.amount)}${saved.projectName.takeIf { it.isNotBlank() }?.let { " for $it" } ?: ""}.",
                actorUid = saved.uid,
                actorName = saved.userName,
                referenceId = id,
                route = "expense"
            )
        }
    }

    suspend fun updateExpenseReview(id: String, status: String, adminRemark: String, reviewerUid: String, reviewerName: String) {
        val normalizedStatus = status.uppercase()
        require(normalizedStatus == "APPROVED" || normalizedStatus == "REJECTED") { "Invalid expense status" }
        val updates = mutableMapOf<String, Any>("status" to normalizedStatus, "adminRemark" to adminRemark.trim(), "reviewedByUid" to reviewerUid, "reviewedByName" to reviewerName, "reviewedAt" to Timestamp.now())
        updates["paymentStatus"] = if (normalizedStatus == "REJECTED") "NOT_PAYABLE" else "UNPAID"
        expensesCollection.document(id).update(updates).await()
    }

    suspend fun markExpensePaid(id: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"
        expensesCollection.document(id).update(mapOf("paymentStatus" to "PAID", "paidByUid" to uid, "paidAt" to Timestamp.now())).await()
    }

    suspend fun markExpenseUnpaid(id: String) {
        expensesCollection.document(id).update(mapOf("paymentStatus" to "UNPAID", "paidByUid" to "", "paidAt" to null)).await()
    }
}
