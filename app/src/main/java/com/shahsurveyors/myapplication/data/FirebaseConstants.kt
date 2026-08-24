package com.shahsurveyors.myapplication.data

object FirebaseConstants {

    // Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_ATTENDANCE = "attendance"
    const val COLLECTION_PROJECTS = "projects"
    const val COLLECTION_EMPLOYEES = "employees"
    const val COLLECTION_EXPENSES = "expenses"
    const val COLLECTION_DSR = "dsr"
    const val COLLECTION_EQUIPMENT = "equipment"
    const val COLLECTION_TASKS = "tasks"
    const val COLLECTION_CLIENTS = "clients"
    const val COLLECTION_CONFIG = "config"
    const val COLLECTION_BILLING = "billing"
    const val COLLECTION_INVOICES = "invoices"
    const val COLLECTION_NOTIFICATIONS = "notifications"

    // Storage Folders
    const val STORAGE_SELFIES = "attendance/selfies"
    const val STORAGE_RECEIPTS = "expenses/receipts"
    const val STORAGE_DSR_PHOTOS = "dsr/photos"
    const val STORAGE_DOCUMENTS = "billing/documents"

    // User Roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_EMPLOYEE = "employee"
    const val ROLE_SITE_MANAGER = "site_manager"
    const val ROLE_SURVEYOR = "surveyor"
    const val ROLE_MARKETING = "marketing"

    // Statuses
    const val STATUS_PENDING = "PENDING"
    const val STATUS_APPROVED = "APPROVED"
    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_DISABLED = "DISABLED"
}