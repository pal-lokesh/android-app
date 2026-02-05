package com.startup.recordservice.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class PreferencesManager(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_PHONE_KEY = stringPreferencesKey("user_phone")
        private val USER_TYPE_KEY = stringPreferencesKey("user_type")
    }
    
    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }
    
    val userPhone: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_PHONE_KEY]
    }
    
    val userType: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_TYPE_KEY]
    }
    
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }
    
    suspend fun saveUserPhone(phone: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_PHONE_KEY] = phone
        }
    }
    
    suspend fun saveUserType(userType: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_TYPE_KEY] = userType
        }
    }
    
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
