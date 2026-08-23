package com.shahsurveyors.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {
    companion object {
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_DEPT = stringPreferencesKey("user_dept")
    }

    suspend fun saveSession(email: String, name: String, role: String, dept: String) {
        context.dataStore.edit { prefs ->
            prefs[USER_EMAIL] = email
            prefs[USER_NAME] = name
            prefs[USER_ROLE] = role
            prefs[USER_DEPT] = dept
        }
    }

    val userSession: Flow<Map<String, String?>> = context.dataStore.data.map { prefs ->
        mapOf(
            "email" to prefs[USER_EMAIL],
            "name" to prefs[USER_NAME],
            "role" to prefs[USER_ROLE],
            "dept" to prefs[USER_DEPT]
        )
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
