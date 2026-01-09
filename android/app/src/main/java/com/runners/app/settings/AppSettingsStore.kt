package com.runners.app.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "app_settings")

object AppSettingsStore {
    private val showTotalDistanceInCommunityCreateKey =
        booleanPreferencesKey("show_total_distance_in_community_create")

    fun showTotalDistanceInCommunityCreateFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[showTotalDistanceInCommunityCreateKey] ?: true
        }

    suspend fun setShowTotalDistanceInCommunityCreate(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[showTotalDistanceInCommunityCreateKey] = enabled
        }
    }
}

