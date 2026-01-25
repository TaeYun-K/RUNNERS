package com.runners.app.network

import android.content.Context
import com.runners.app.auth.AuthTokenStore
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenRefreshAuthenticator(
    private val context: Context,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        if (request.url.encodedPath.startsWith("/api/auth/")) return null
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val cachedAccessToken = AuthTokenStore.peekAccessToken()
            val requestToken = request.header("Authorization")
                ?.removePrefix("Bearer")
                ?.trim()

            if (!cachedAccessToken.isNullOrBlank() && !requestToken.isNullOrBlank() && requestToken != cachedAccessToken) {
                return request.newBuilder()
                    .header("Authorization", "Bearer $cachedAccessToken")
                    .build()
            }

            val newAccessToken = try {
                BackendAuthApi.refreshAccessToken()
            } catch (e: Exception) {
                BackendHttpClient.clearCookies()
                AuthTokenStore.clearBlocking(context)
                return null
            }

            AuthTokenStore.setAccessTokenBlocking(context, newAccessToken)
            return request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
