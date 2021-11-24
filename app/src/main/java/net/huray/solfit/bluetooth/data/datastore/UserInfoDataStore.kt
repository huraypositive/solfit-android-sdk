package net.huray.solfit.bluetooth.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserInfoDataStore(private val context : Context) {

    private val Context.dataStore  by preferencesDataStore(name = "userinfo")

    private val sexKey = intPreferencesKey("sexKey")
    private val ageKey = intPreferencesKey("ageKey")
    private val heightKey = intPreferencesKey("heightKey")

    val sex : Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[sexKey] ?: 1
        }

    suspend fun setSex(sex: Int){
        context.dataStore.edit { preferences ->
            preferences[sexKey] = sex
        }
    }

    val age : Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[ageKey] ?: 30
        }

    suspend fun setAge(age: Int){
        context.dataStore.edit { preferences ->
            preferences[ageKey] = age
        }
    }

    val height : Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {preferences ->
            preferences[heightKey] ?: 1
        }

    suspend fun setheight(height: Int){
        context.dataStore.edit { preferences ->
            preferences[heightKey] = height
        }
    }
}
