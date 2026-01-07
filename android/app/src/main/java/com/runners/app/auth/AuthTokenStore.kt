package com.runners.app.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "auth")

object AuthTokenStore {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")

    @Volatile
    private var cachedAccessToken: String? = null

    @Volatile
    private var cachedRefreshToken: String? = null

    suspend fun load(context: Context) {
        val prefs = context.dataStore.data.first()
        cachedAccessToken = prefs[accessTokenKey]
        cachedRefreshToken = prefs[refreshTokenKey]
    }

    fun peekAccessToken(): String? = cachedAccessToken

    fun peekRefreshToken(): String? = cachedRefreshToken

    suspend fun setAccessToken(context: Context, accessToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
        }
        cachedAccessToken = accessToken
    }

    suspend fun setTokens(context: Context, accessToken: String, refreshToken: String) {
        context.dataStore.edit { prefs ->
            prefs[accessTokenKey] = accessToken
            prefs[refreshTokenKey] = refreshToken
        }
        cachedAccessToken = accessToken
        cachedRefreshToken = refreshToken
    }

    fun setAccessTokenBlocking(context: Context, accessToken: String) {
        runBlocking { setAccessToken(context, accessToken) }
    }

    fun clearBlocking(context: Context) {
        runBlocking { clear(context) }
    }

    suspend fun clear(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(accessTokenKey)
            prefs.remove(refreshTokenKey)
        }
        cachedAccessToken = null
        cachedRefreshToken = null
    }
}
