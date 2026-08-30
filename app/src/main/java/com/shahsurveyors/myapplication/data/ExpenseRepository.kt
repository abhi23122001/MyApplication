package com.shahsurveyors.myapplication.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shahsurveyors.myapplication.models.ExpenseRecord
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val expensesCollection =
        firestore.collection(FirebaseConstants.COLLECTION_EXPENSES)

    private val notificationRepository = NotificationRepository(firestore)

    suspend fun getExpensesForUser(uid: String): List<ExpenseRecord> = try {
        expensesCollection
            .whereEqualTo("uid", uid)
            .get()
            .await()
            .toObjects(ExpenseRecord::class.java)
            .sortedByDescending { it.date?.seconds ?: 0L }
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun getAllExpenses(): List<ExpenseRecord> = try {
        expensesCollection
            .get()
            .await()
            .toObjects(ExpenseRecord::class.java)
            .sortedByDescending { it.date?.seconds ?: 0L }
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun hasDuplicateExpense(uid: String, duplicateKey: String): Boolean {
        if (uid.isBlank() || duplicateKey.isBlank()) return false

        return getExpensesForUser(uid)
            .any {
                it.duplicateKey == duplicateKey &&
                        it.status != "REJECTED"
            }
    }

    // Employee submits expense -> Admin notification
    suspend fun saveExpense(expense: ExpenseRecord) {
        val id = expense.id.ifBlank {
            expensesCollection.document().id
        }

        val saved = expense.copy(id = id)

        expensesCollection
            .document(id)
            .set(saved)
            .await()

        runCatching {
            notificationRepository.createForAdmins(
                type = "EXPENSE_SUBMITTED",
                title = "New Expense Request",
                message = "${saved.userName.ifBlank { "Employee" }} submitted an expense of ₹${"%.2f".format(saved.amount)}" +
                        saved.projectName
                            .takeIf { it.isNotBlank() }
                            ?.let { " for $it" }
                            .orEmpty() +
                        ".",
                actorUid = saved.uid,
                actorName = saved.userName,
                referenceId = id,
                route = "expense"
            )
        }
    }

    // Admin approves/rejects expense -> Employee notification
    suspend fun updateExpenseReview(
        id: String,
        status: String,
        adminRemark: String,
        reviewerUid: String,
        reviewerName: String
    ) {
        val normalizedStatus = status.uppercase()

        require(
            normalizedStatus == "APPROVED" ||
                    normalizedStatus == "REJECTED"
        ) {
            "Invalid expense status"
        }

        // Get expense BEFORE updating it so we know the employee UID/name.
        val expenseSnapshot = expensesCollection
            .document(id)
            .get()
            .await()

        require(expenseSnapshot.exists()) {
            "Expense not found"
        }

        val expense = expenseSnapshot
            .toObject(ExpenseRecord::class.java)
            ?: throw IllegalStateException("Expense data unavailable")

        val updates = mutableMapOf<String, Any>(
            "status" to normalizedStatus,
            "adminRemark" to adminRemark.trim(),
            "reviewedByUid" to reviewerUid,
            "reviewedByName" to reviewerName,
            "reviewedAt" to Timestamp.now()
        )

        updates["paymentStatus"] =
            if (normalizedStatus == "REJECTED") {
                "NOT_PAYABLE"
            } else {
                "UNPAID"
            }

        expensesCollection
            .document(id)
            .update(updates)
            .await()

        // Notify the employee who submitted the expense.
        if (expense.uid.isNotBlank()) {
            runCatching {
                val title =
                    if (normalizedStatus == "APPROVED") {
                        "Expense Approved"
                    } else {
                        "Expense Rejected"
                    }

                val message =
                    if (normalizedStatus == "APPROVED") {
                        "Your expense request has been approved by ${reviewerName.ifBlank { "Admin" }}."
                    } else {
                        "Your expense request has been rejected by ${reviewerName.ifBlank { "Admin" }}." +
                                adminRemark
                                    .takeIf { it.isNotBlank() }
                                    ?.let { " Remark: $it" }
                                    .orEmpty()
                    }

                notificationRepository.createForUser(
                    expense.uid,
                    "EXPENSE_$normalizedStatus",
                    title,
                    message,
                    id,
                    "expense"
                )
            }
        }
    }

    // Admin marks expense as PAID -> Employee notification
    suspend fun markExpensePaid(id: String) {
        val adminUid =
            FirebaseAuth.getInstance().currentUser?.uid ?: "ADMIN"

        val expenseSnapshot = expensesCollection
            .document(id)
            .get()
            .await()

        require(expenseSnapshot.exists()) {
            "Expense not found"
        }

        val expense = expenseSnapshot
            .toObject(ExpenseRecord::class.java)
            ?: throw IllegalStateException("Expense data unavailable")

        expensesCollection
            .document(id)
            .update(
                mapOf(
                    "paymentStatus" to "PAID",
                    "paidByUid" to adminUid,
                    "paidAt" to Timestamp.now()
                )
            )
            .await()

        if (expense.uid.isNotBlank()) {
            runCatching {
                notificationRepository.createForUser(
                    expense.uid,
                    "EXPENSE_PAID",
                    "Expense Paid",
                    "Your expense has been marked as paid.",
                    id,
                    "expense"
                )
            }
        }
    }

    // Admin changes PAID -> UNPAID -> Employee notification
    suspend fun markExpenseUnpaid(id: String) {
        val expenseSnapshot = expensesCollection
            .document(id)
            .get()
            .await()

        require(expenseSnapshot.exists()) {
            "Expense not found"
        }

        val expense = expenseSnapshot
            .toObject(ExpenseRecord::class.java)
            ?: throw IllegalStateException("Expense data unavailable")

        expensesCollection
            .document(id)
            .update(
                mapOf(
                    "paymentStatus" to "UNPAID",
                    "paidByUid" to "",
                    "paidAt" to null
                )
            )
            .await()

        if (expense.uid.isNotBlank()) {
            runCatching {
                notificationRepository.createForUser(
                    expense.uid,
                    "EXPENSE_UNPAID",
                    "Expense Payment Updated",
                    "Your expense payment status has been changed to unpaid.",
                    id,
                    "expense"
                )
            }
        }
    }
}