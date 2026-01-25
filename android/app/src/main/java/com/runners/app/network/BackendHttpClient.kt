package com.runners.app.network

import com.runners.app.BuildConfig
import com.runners.app.RunnersApplication
import com.runners.app.auth.AuthTokenStore
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.time.Duration

object BackendHttpClient {
    private val backendBaseUrl = BuildConfig.BACKEND_BASE_URL.trimEnd('/')
    private val backendBaseHttpUrl = backendBaseUrl.toHttpUrlOrNull()

    private val cookieJar: SecureCookieJar by lazy {
        SecureCookieJar(RunnersApplication.appContext)
    }

    fun clearCookies() {
        cookieJar.clear()
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .callTimeout(Duration.ofSeconds(30))
            .cookieJar(cookieJar)
            .authenticator(TokenRefreshAuthenticator(RunnersApplication.appContext))
            .addInterceptor(
                Interceptor { chain ->
                    val original = chain.request()

                    val hasAuthorization = original.header("Authorization") != null
                    val token = AuthTokenStore.peekAccessToken()

                    val shouldAttachToken =
                        !hasAuthorization &&
                            !token.isNullOrBlank() &&
                            backendBaseHttpUrl != null &&
                            original.url.host == backendBaseHttpUrl.host &&
                            original.url.port == backendBaseHttpUrl.port &&
                            !original.url.encodedPath.startsWith("/api/auth/")

                    if (!shouldAttachToken) {
                        return@Interceptor chain.proceed(original)
                    }

                    val authenticatedRequest = original.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(authenticatedRequest)
                }
            )
            .build()
    }
}

