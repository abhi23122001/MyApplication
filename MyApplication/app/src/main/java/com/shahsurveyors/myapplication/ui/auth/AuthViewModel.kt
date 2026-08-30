package com.shahsurveyors.myapplication.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shahsurveyors.myapplication.data.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val firebaseAuth: FirebaseAuth =
        FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore =
        FirebaseFirestore.getInstance()

    var isLoading by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)

    var isUserLoggedIn by mutableStateOf(false)
    var userStatus by mutableStateOf("PENDING")

    var userUid by mutableStateOf("")
    var userEmployeeId by mutableStateOf("EMP001")
    var userName by mutableStateOf("")
    var userRole by mutableStateOf("")
    var userAccess by mutableStateOf("")
    var userDepartment by mutableStateOf("")


    // =========================================================
    // INITIAL SESSION CHECK
    // =========================================================

    init {
        viewModelScope.launch {
            try {

                val firebaseUser = firebaseAuth.currentUser

                if (firebaseUser != null) {

                    loadUserFromFirestore(firebaseUser.uid)

                } else {

                    val session = sessionManager.userSession.first()

                    val uid = session["uid"]

                    if (!uid.isNullOrBlank()) {
                        userUid = uid
                        userName = session["name"] ?: ""
                        userRole = session["role"] ?: ""
                        userAccess = session["access"] ?: ""
                        userDepartment = session["dept"] ?: ""
                        userEmployeeId = session["empId"] ?: uid.take(6).uppercase()

                        isUserLoggedIn = true
                        userStatus = "APPROVED"
                    }
                }

            } catch (e: Exception) {

                isUserLoggedIn = false
                authError = e.localizedMessage
            }
        }
    }


    // =========================================================
    // LOGIN
    // =========================================================

    fun login(
        email: String,
        pass: String
    ) {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            authError = null
            userStatus = "PENDING"

            try {

                val cleanEmail = email.trim()
                val cleanPassword = pass

                // -------------------------------------------------
                // VALIDATION
                // -------------------------------------------------

                if (cleanEmail.isBlank()) {
                    authError = "Please enter your email address."
                    return@launch
                }

                if (!Patterns.EMAIL_ADDRESS
                        .matcher(cleanEmail)
                        .matches()
                ) {
                    authError = "Please enter a valid email address."
                    return@launch
                }

                if (cleanPassword.isBlank()) {
                    authError = "Please enter your password."
                    return@launch
                }


                // -------------------------------------------------
                // FIREBASE AUTHENTICATION
                // -------------------------------------------------

                val result = firebaseAuth
                    .signInWithEmailAndPassword(
                        cleanEmail,
                        cleanPassword
                    )
                    .await()

                val firebaseUser = result.user
                    ?: throw Exception(
                        "Unable to identify Firebase user."
                    )


                // -------------------------------------------------
                // LOAD FIRESTORE PROFILE
                // -------------------------------------------------

                loadUserFromFirestore(firebaseUser.uid)

            } catch (e: Exception) {

                authError = getFirebaseErrorMessage(e)
                isUserLoggedIn = false

            } finally {

                isLoading = false
            }
        }
    }


    // =========================================================
    // LOAD USER PROFILE FROM FIRESTORE
    // =========================================================

    private suspend fun loadUserFromFirestore(
        uid: String
    ) {

        val document = firestore
            .collection("users")
            .document(uid)
            .get()
            .await()


        // -------------------------------------------------
        // PROFILE NOT FOUND
        // -------------------------------------------------

        if (!document.exists()) {

            firebaseAuth.signOut()
            sessionManager.clearSession()

            isUserLoggedIn = false
            userStatus = "PENDING"

            authError =
                "Your account profile was not found. Please contact Admin."

            return
        }


        // -------------------------------------------------
        // USER DATA
        // -------------------------------------------------

        val name =
            document.getString("name")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: "Staff User"

        val email =
            document.getString("email")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: firebaseAuth.currentUser?.email
                ?: ""

        // IMPORTANT:
        // Roles are stored in LOWERCASE consistently.
        val role =
            document.getString("role")
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotBlank() }
                ?: "employee"

        val department =
            document.getString("department")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: document.getString("dept")
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                ?: "SURVEY"

        val access =
            document.getString("access")
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?: getDefaultAccess(role)

        val approved =
            document.getBoolean("approved")
                ?: false

        val active =
            document.getBoolean("active")
                ?: true

        val empId = document.getString("employeeId")
            ?: document.getString("id")
            ?: uid.take(6).uppercase()

        userUid = uid
        userEmployeeId = empId


        // =====================================================
        // ACCOUNT DISABLED
        // =====================================================

        if (!active) {

            firebaseAuth.signOut()
            sessionManager.clearSession()

            isUserLoggedIn = false
            userStatus = "DISABLED"

            authError =
                "Your account has been disabled by Admin."

            return
        }


        // =====================================================
        // ADMIN
        // =====================================================

        if (role == "admin") {

            if (!approved) {

                firebaseAuth.signOut()
                sessionManager.clearSession()

                isUserLoggedIn = false
                userStatus = "PENDING"

                authError =
                    "Admin account is not approved."

                return
            }

            saveUserSession(
                uid = uid,
                email = email,
                name = name,
                role = "admin",
                department = department,
                access = if (access.isBlank()) {
                    "ALL"
                } else {
                    access
                }
            )

            return
        }


        // =====================================================
        // EMPLOYEE APPROVAL
        // =====================================================

        if (!approved) {

            firebaseAuth.signOut()
            sessionManager.clearSession()

            isUserLoggedIn = false
            userStatus = "PENDING"

            authError =
                "Your account is waiting for Admin approval."

            return
        }


        // =====================================================
        // APPROVED EMPLOYEE
        // =====================================================

        saveUserSession(
            uid = uid,
            email = email,
            name = name,
            role = role,
            department = department,
            access = access
        )
    }


    // =========================================================
    // SAVE SESSION
    // =========================================================

    private suspend fun saveUserSession(
        uid: String,
        email: String,
        name: String,
        role: String,
        department: String,
        access: String
    ) {

        sessionManager.saveSession(
            uid = uid,
            email = email,
            name = name,
            role = role,
            dept = department,
            access = access
        )

        userName = name
        userRole = role
        userAccess = access
        userDepartment = department

        userStatus = "APPROVED"
        isUserLoggedIn = true
        authError = null
    }


    // =========================================================
    // SIGNUP / REQUEST ACCESS
    // =========================================================

    fun signup(
        name: String,
        email: String,
        pass: String,
        dept: String
    ) {

        if (isLoading) return

        viewModelScope.launch {

            isLoading = true
            authError = null

            try {

                val cleanName = name.trim()
                val cleanEmail = email.trim()
                val cleanPassword = pass
                val cleanDepartment = dept.trim().uppercase()


                // -------------------------------------------------
                // VALIDATION
                // -------------------------------------------------

                if (cleanName.isBlank()) {
                    authError = "Please enter your full name."
                    return@launch
                }

                if (cleanEmail.isBlank()) {
                    authError = "Please enter your email address."
                    return@launch
                }

                if (!Patterns.EMAIL_ADDRESS
                        .matcher(cleanEmail)
                        .matches()
                ) {
                    authError = "Please enter a valid email address."
                    return@launch
                }

                if (cleanPassword.length < 6) {
                    authError =
                        "Password must contain at least 6 characters."
                    return@launch
                }

                if (cleanDepartment.isBlank()) {
                    authError = "Please select a department."
                    return@launch
                }


                // =================================================
                // CREATE FIREBASE AUTH ACCOUNT
                // =================================================

                val result = firebaseAuth
                    .createUserWithEmailAndPassword(
                        cleanEmail,
                        cleanPassword
                    )
                    .await()

                val firebaseUser = result.user
                    ?: throw Exception(
                        "Unable to create Firebase account."
                    )

                val uid = firebaseUser.uid


                // =================================================
                // CREATE PENDING FIRESTORE PROFILE
                // =================================================

                val userData = hashMapOf<String, Any>(

                    "uid" to uid,

                    "name" to cleanName,

                    "email" to cleanEmail,

                    // IMPORTANT:
                    // Must match Firestore Rules.
                    "role" to "employee",

                    "department" to cleanDepartment,

                    "access" to getDefaultAccess("employee"),

                    "approved" to false,

                    "active" to true,

                    "createdAt" to
                            com.google.firebase.firestore
                                .FieldValue
                                .serverTimestamp()
                )


                firestore
                    .collection("users")
                    .document(uid)
                    .set(
                        userData,
                        SetOptions.merge()
                    )
                    .await()


                // =================================================
                // SIGN OUT AFTER REGISTRATION
                // =================================================

                firebaseAuth.signOut()
                sessionManager.clearSession()

                isUserLoggedIn = false
                userStatus = "PENDING"

                authError =
                    "Registration submitted successfully. Waiting for Admin approval."

            } catch (e: Exception) {

                authError = getFirebaseErrorMessage(e)

            } finally {

                isLoading = false
            }
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    fun logout() {

        viewModelScope.launch {

            try {

                firebaseAuth.signOut()

            } finally {

                sessionManager.clearSession()

                userName = ""
                userRole = ""
                userAccess = ""
                userDepartment = ""

                userStatus = "PENDING"
                isUserLoggedIn = false
                authError = null
            }
        }
    }


    // =========================================================
    // DEFAULT ACCESS
    // =========================================================

    private fun getDefaultAccess(
        role: String
    ): String {

        return when (role.lowercase()) {

            "admin" ->
                "ALL"

            "site_manager" ->
                "ATTENDANCE,EXPENSE,PROJECT,TASK,CHAT"

            "surveyor" ->
                "ATTENDANCE,SURVEY,TASK,CHAT"

            "marketing" ->
                "ATTENDANCE,CRM,MARKETING,CHAT"

            "labour" ->
                "ATTENDANCE,TASK"

            "employee" ->
                "ATTENDANCE,CHAT"

            else ->
                "ATTENDANCE,CHAT"
        }
    }


    // =========================================================
    // FIREBASE ERROR HANDLER
    // =========================================================

    private fun getFirebaseErrorMessage(
        exception: Exception
    ): String {

        val message =
            (
                    exception.message
                        ?: exception.localizedMessage
                        ?: ""
                    ).lowercase()

        return when {

            message.contains("invalid credential") ->
                "Invalid email or password."

            message.contains("password is invalid") ->
                "Invalid email or password."

            message.contains("user not found") ->
                "No account found with this email."

            message.contains("no user record") ->
                "No account found with this email."

            message.contains(
                "email address is badly formatted"
            ) ->
                "Please enter a valid email address."

            message.contains(
                "email-already-in-use"
            ) ->
                "An account with this email already exists."

            message.contains(
                "email address is already in use"
            ) ->
                "An account with this email already exists."

            message.contains(
                "weak-password"
            ) ->
                "Password is too weak. Use at least 6 characters."

            message.contains(
                "network"
            ) ->
                "Network error. Please check your internet connection."

            message.contains(
                "permission-denied"
            ) ->
                "Access denied. Please contact Admin."

            message.contains(
                "failed to get document"
            ) ->
                "Unable to load your account profile."

            message.contains(
                "too many requests"
            ) ->
                "Too many attempts. Please try again later."

            else ->
                exception.localizedMessage
                    ?: "Something went wrong. Please try again."
        }
    }
}