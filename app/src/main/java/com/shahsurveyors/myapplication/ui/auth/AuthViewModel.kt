package com.shahsurveyors.myapplication.ui.auth

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shahsurveyors.myapplication.data.AuthRepository
import com.shahsurveyors.myapplication.data.SessionManager
import com.shahsurveyors.myapplication.data.UserRepository
import com.shahsurveyors.myapplication.models.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
                if (firebaseUser != null) {
                    currentUserUid = firebaseUser.uid
                    loadUserFromFirestore(firebaseUser.uid)
                } else {
                    val session = sessionManager.userSession.first()
                    val uid = session["uid"]
                    if (!uid.isNullOrBlank()) {
                        currentUserUid = uid
                        userName = session["name"] ?: ""
                        userRole = session["role"] ?: ""
                        userAccess = session["access"] ?: ""
                        userDepartment = session["dept"] ?: ""
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
                val uid = authRepository.currentUser?.uid ?: throw Exception("Auth failed")
                currentUserUid = uid
                loadUserFromFirestore(uid)
            } catch (e: Exception) {
                authError = e.localizedMessage
                isUserLoggedIn = false
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
            authError = "Profile not found."
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

        if (!profile.approved && profile.role != "admin") {
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
        sessionManager.saveSession(
            uid = profile.uid,
            email = profile.email,
            name = profile.name,
            role = profile.role,
            dept = profile.department,
            access = profile.access
        )
        userName = profile.name
        userRole = profile.role
        userAccess = profile.access
        userDepartment = profile.department
        userStatus = "APPROVED"
        isUserLoggedIn = true
        currentUserUid = profile.uid
        authError = null
    }

    fun signup(name: String, email: String, pass: String, dept: String) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            authError = null
            try {
                if (name.isBlank() || email.isBlank() || pass.length < 6 || dept.isBlank()) {
                    authError = "Invalid input details."
                    return@launch
                }

                authRepository.signUp(email, pass)
                val uid = authRepository.currentUser?.uid ?: throw Exception("Signup failed")

                val profile = UserProfile(
                    uid = uid,
                    name = name,
                    email = email,
                    department = dept.uppercase(),
                    role = "employee",
                    approved = false,
                    active = true
                )
                userRepository.saveUserProfile(profile)

                authRepository.signOut()
                sessionManager.clearSession()
                isUserLoggedIn = false
                currentUserUid = null
                userStatus = "PENDING"
                authError = "Registration submitted. Waiting for approval."
            } catch (e: Exception) {
                authError = e.localizedMessage
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
