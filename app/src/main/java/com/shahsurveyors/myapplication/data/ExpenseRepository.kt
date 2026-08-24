package com.shahsurveyors.myapplication.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shahsurveyors.myapplication.models.ExpenseRecord
import kotlinx.coroutines.tasks.await

class ExpenseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val expensesCollection = firestore.collection(FirebaseConstants.COLLECTION_EXPENSES)

    suspend fun getExpensesForUser(uid: String): List<ExpenseRecord> {
        return try {
            val snapshot = expensesCollection
                .whereEqualTo("uid", uid)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ExpenseRecord::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAllExpenses(): List<ExpenseRecord> {
        return try {
            val snapshot = expensesCollection
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(ExpenseRecord::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveExpense(expense: ExpenseRecord) {
        val id = expense.id.ifBlank { expensesCollection.document().id }
        expensesCollection.document(id)
            .set(expense.copy(id = id))
            .await()
    }

    suspend fun updateExpenseStatus(id: String, status: String) {
        expensesCollection.document(id)
            .update("status", status)
            .await()
    }
}
