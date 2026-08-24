package com.shahsurveyors.myapplication.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "employee",
    val department: String = "SURVEY",
    val access: String = "ATTENDANCE,CHAT",
    val approved: Boolean = false,
    val active: Boolean = true,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val profileImageUrl: String? = null
)

data class AttendanceRecord(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val date: String = "", // yyyy-MM-dd
    val punchInTime: Timestamp? = null,
    val punchOutTime: Timestamp? = null,
    val punchInLat: Double? = null,
    val punchInLng: Double? = null,
    val punchOutLat: Double? = null,
    val punchOutLng: Double? = null,
    val siteName: String = "",
    val selfieUrl: String? = null,
    val status: String = "PRESENT"
)

data class ProjectModel(
    val id: String = "",
    val name: String = "",
    val clientName: String = "",
    val description: String = "",
    val siteLocation: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val geofenceRadius: Double = 500.0, // meters
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val status: String = "ACTIVE",
    val budget: Double = 0.0,
    val managerId: String = ""
)

data class ExpenseRecord(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val date: Timestamp? = null,
    val description: String = "",
    val receiptUrl: String? = null,
    val status: String = "PENDING" // PENDING, APPROVED, REJECTED
)

data class DSRModel(
    val id: String = "",
    val uid: String = "",
    val userName: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val date: Timestamp? = null,
    val manpowerCount: Int = 0,
    val workDone: String = "",
    val equipmentUsed: String = "",
    val remarks: String = "",
    val photoUrls: List<String> = emptyList()
)

data class EquipmentModel(
    val id: String = "",
    val name: String = "",
    val modelNumber: String = "",
    val serialNumber: String = "",
    val category: String = "",
    val status: String = "AVAILABLE", // AVAILABLE, IN_USE, MAINTENANCE
    val assignedToUid: String? = null,
    val assignedToName: String? = null,
    val lastMaintenanceDate: Timestamp? = null
)

data class TaskModel(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val assignedToUid: String = "",
    val assignedToName: String = "",
    val projectId: String = "",
    val projectName: String = "",
    val dueDate: Timestamp? = null,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val status: String = "OPEN" // OPEN, IN_PROGRESS, COMPLETED
)

data class ClientModel(
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val gstin: String? = null
)
