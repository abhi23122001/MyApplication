package com.shahsurveyors.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "user_session"
)

class SessionManager(
    private val context: Context
) {

    companion object {

        const val SESSION_FILE_NAME = "user_session"

        val USER_UID =
            stringPreferencesKey("user_uid")

        val USER_EMAIL =
            stringPreferencesKey("user_email")

        val USER_NAME =
            stringPreferencesKey("user_name")

        val USER_ROLE =
            stringPreferencesKey("user_role")

        val USER_DEPT =
            stringPreferencesKey("user_dept")

        val USER_ACCESS =
            stringPreferencesKey("user_access")
    }


    // =====================================================
    // SAVE SESSION
    // =====================================================

    suspend fun saveSession(
        uid: String,
        email: String,
        name: String,
        role: String,
        dept: String,
        access: String
    ) {

        context.dataStore.edit { preferences ->

            preferences[USER_UID] =
                uid.trim()

            preferences[USER_EMAIL] =
                email.trim()

            preferences[USER_NAME] =
                name.trim()

            preferences[USER_ROLE] =
                role.trim().uppercase()

            preferences[USER_DEPT] =
                dept.trim().uppercase()

            preferences[USER_ACCESS] =
                access.trim()
        }
    }


    // =====================================================
    // USER SESSION
    // =====================================================

    val userSession: Flow<Map<String, String?>>
        get() = context.dataStore.data.map { preferences ->

            mapOf(

                "uid" to
                        preferences[USER_UID],

                "email" to
                        preferences[USER_EMAIL],

                "name" to
                        preferences[USER_NAME],

                "role" to
                        preferences[USER_ROLE],

                "dept" to
                        preferences[USER_DEPT],

                "access" to
                        preferences[USER_ACCESS]
            )
        }


    // =====================================================
    // CHECK SESSION
    // =====================================================

    val isLoggedIn: Flow<Boolean>
        get() = context.dataStore.data.map { preferences ->

            !preferences[USER_UID]
                .isNullOrBlank()
        }


    // =====================================================
    // CLEAR SESSION / LOGOUT
    // =====================================================

    suspend fun clearSession() {

        context.dataStore.edit { preferences ->

            preferences.clear()
        }
    }
}