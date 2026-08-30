package com.shahsurveyors.myapplication.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.shahsurveyors.myapplication.data.AuthRepository
import com.shahsurveyors.myapplication.data.FirebaseConstants
import com.shahsurveyors.myapplication.data.SessionManager
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    var isLoading by mutableStateOf(false)
    var authError by mutableStateOf<String?>(null)
    var isUserLoggedIn by mutableStateOf(false)
    var userStatus by mutableStateOf("PENDING")
    var userName by mutableStateOf("")
    var userRole by mutableStateOf("")
    var userAccess by mutableStateOf("")
    var userDepartment by mutableStateOf("")

    var currentUserUid by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                val firebaseUser = authRepository.currentUser
                if (firebaseUser == null) {
                    sessionManager.clearSession()
                    isUserLoggedIn = false
                    return@launch
                }
                currentUserUid = firebaseUser.uid
                loadUserFromFirestore(firebaseUser.uid)
            } catch (e: Exception) {
                sessionManager.clearSession()
                isUserLoggedIn = false
                currentUserUid = null
                authError = e.localizedMessage ?: "Unable to restore your account session."
            }
        }
    }

    fun login(email: String, pass: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            authError = null
            userStatus = "PENDING"
            try {
                val cleanEmail = email.trim()
                if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                    authError = "Please enter a valid email address."
                    return@launch
                }
                if (pass.isBlank()) {
                    authError = "Please enter your password."
                    return@launch
                }
                authRepository.signIn(cleanEmail, pass)
                val uid = authRepository.currentUser?.uid ?: throw Exception("Authentication failed")
                currentUserUid = uid
                loadUserFromFirestore(uid)
            } catch (e: Exception) {
                authError = e.localizedMessage ?: "Unable to sign in."
                isUserLoggedIn = false
                currentUserUid = null
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadUserFromFirestore(uid: String) {
        val profile = userRepository.getUserProfile(uid)
        if (profile == null) {
            authRepository.signOut()
            sessionManager.clearSession()
            isUserLoggedIn = false
            currentUserUid = null
            userStatus = "PENDING"
            authError = "Your account profile was not found. Please contact Admin."
            return
        }
        if (profile.uid != uid) {
            authRepository.signOut()
            sessionManager.clearSession()
            isUserLoggedIn = false
            currentUserUid = null
            authError = "Account identity verification failed."
            return
        }
        if (!profile.active) {
            authRepository.signOut()
            sessionManager.clearSession()
            isUserLoggedIn = false
            currentUserUid = null
            userStatus = "DISABLED"
            authError = "Account disabled."
            return
        }
        if (!profile.approved && !profile.role.equals(FirebaseConstants.ROLE_ADMIN, ignoreCase = true)) {
            authRepository.signOut()
            sessionManager.clearSession()
            isUserLoggedIn = false
            currentUserUid = null
            userStatus = "PENDING"
            authError = "Waiting for Admin approval."
            return
        }
        saveUserSession(profile)
    }

    private suspend fun saveUserSession(profile: UserProfile) {
        val normalizedRole = profile.role.trim()
        sessionManager.saveSession(
            uid = profile.uid,
            email = profile.email,
            name = profile.name,
            role = normalizedRole,
            dept = profile.department,
            access = profile.access
        )
        userName = profile.name
        userRole = normalizedRole
        userAccess = profile.access
        userDepartment = profile.department
        userStatus = "APPROVED"
        isUserLoggedIn = true
        currentUserUid = profile.uid
        authError = null
        registerFcmToken(profile.uid)
    }

    private fun registerFcmToken(uid: String) {
        if (uid.isBlank()) return
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result ?: return@addOnCompleteListener
            FirebaseFirestore.getInstance().collection("users").document(uid).set(
                mapOf("fcmToken" to token, "fcmTokenUpdatedAt" to FieldValue.serverTimestamp()),
                SetOptions.merge()
            )
        }
    }

    fun signup(name: String, email: String, pass: String, dept: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            authError = null
            var createdUid: String? = null
            try {
                val cleanName = name.trim()
                val cleanEmail = email.trim()
                val cleanDept = dept.trim().uppercase()
                if (cleanName.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches() || pass.length < 6 || cleanDept.isBlank()) {
                    authError = "Invalid input details."
                    return@launch
                }
                authRepository.signUp(cleanEmail, pass)
                val uid = authRepository.currentUser?.uid ?: throw Exception("Signup failed")
                createdUid = uid
                val profile = UserProfile(
                    uid = uid,
                    name = cleanName,
                    email = cleanEmail,
                    department = cleanDept,
                    role = "employee",
                    access = "",
                    approved = false,
                    active = true
                )
                try {
                    userRepository.saveUserProfile(profile)
                } catch (profileError: Exception) {
                    runCatching { authRepository.currentUser?.delete()?.await() }
                    throw profileError
                }
                authRepository.signOut()
                sessionManager.clearSession()
                isUserLoggedIn = false
                currentUserUid = null
                userStatus = "PENDING"
                authError = "Registration submitted. Waiting for approval."
            } catch (e: Exception) {
                if (createdUid != null && authRepository.currentUser?.uid == createdUid) {
                    runCatching { authRepository.currentUser?.delete()?.await() }
                }
                authError = e.localizedMessage ?: "Unable to complete registration."
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            sessionManager.clearSession()
            userName = ""
            userRole = ""
            userAccess = ""
            userDepartment = ""
            currentUserUid = null
            userStatus = "PENDING"
            isUserLoggedIn = false
            authError = null
        }
    }
}
