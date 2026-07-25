package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prefs")

class GemDataStore(private val context: Context) {
    private val GEMS_KEY = intPreferencesKey("demo_gems")

    val gemCount: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[GEMS_KEY] ?: 0
    }

    suspend fun addGems(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[GEMS_KEY] ?: 0
            preferences[GEMS_KEY] = current + amount
        }
    }
}
