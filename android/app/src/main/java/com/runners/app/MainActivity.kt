package com.runners.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.runners.app.auth.LoginScreen
import com.runners.app.navigation.RunnersBottomBar
import com.runners.app.navigation.RunnersNavHost
import com.runners.app.network.BackendAuthApi
import com.runners.app.auth.AuthTokenStore
import com.runners.app.network.BackendHttpClient
import com.runners.app.network.BackendUserApi
import com.runners.app.network.GoogleLoginResult
import com.runners.app.ui.theme.RUNNERSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.runners.app.navigation.shouldShowBottomBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RUNNERSTheme {
				var session by remember { mutableStateOf<GoogleLoginResult?>(null) }
				var isLoading by remember { mutableStateOf(false) }
				var errorMessage by remember { mutableStateOf<String?>(null) }
				val context = LocalContext.current
				val scope = rememberCoroutineScope()
				val navController = rememberNavController()
				val navBackStackEntry by navController.currentBackStackEntryAsState()
				val currentRoute = navBackStackEntry?.destination?.route

				LaunchedEffect(Unit) {
					AuthTokenStore.load(context)

					if (session != null) return@LaunchedEffect

                    isLoading = true
                    errorMessage = null
                    try {
                        val cachedAccessToken = AuthTokenStore.peekAccessToken()
                        val accessToken = if (cachedAccessToken.isNullOrBlank()) {
                            val refreshed = withContext(Dispatchers.IO) { BackendAuthApi.refreshAccessToken() }
                            AuthTokenStore.setAccessToken(context, refreshed)
                            refreshed
                        } else {
                            cachedAccessToken
                        }

                        val me = withContext(Dispatchers.IO) { BackendUserApi.getMe() }
                        val finalAccessToken = AuthTokenStore.peekAccessToken() ?: accessToken
                        session = GoogleLoginResult(
                            userId = me.userId,
                            email = me.email,
                            name = me.name,
                            nickname = me.nickname,
                            picture = me.picture,
                            accessToken = finalAccessToken,
                            isNewUser = false,
                        )
                    } catch (e: Exception) {
                        BackendHttpClient.clearCookies()
                        AuthTokenStore.clear(context)
                        session = null
                    } finally {
                        isLoading = false
                    }
				}

				Scaffold(
					modifier = Modifier.fillMaxSize(),
					bottomBar = {
						if (session != null && shouldShowBottomBar(currentRoute)) {
							RunnersBottomBar(navController)
						}
					},
				) { innerPadding ->
					if (session == null) {
						LoginScreen(
							onIdToken = { idToken ->
								scope.launch {
									isLoading = true
									errorMessage = null
									try {
										val result = withContext(Dispatchers.IO) {
											BackendAuthApi.googleLogin(idToken)
										}
										AuthTokenStore.setAccessToken(context, result.accessToken)
										session = result
									} catch (e: Exception) {
										errorMessage = e.message ?: "Backend login failed"
									} finally {
										isLoading = false
									}
								}
							},
							isLoading = isLoading,
							backendErrorMessage = errorMessage,
							modifier = Modifier.padding(innerPadding)
						)
					} else {
						RunnersNavHost(
							navController = navController,
							session = session!!,
							onLogout = {
								scope.launch {
                                    BackendHttpClient.clearCookies()
									AuthTokenStore.clear(context)
									session = null
								}
							},
							modifier = Modifier.padding(innerPadding),
						)
					}
				}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RUNNERSTheme {
		Text("Preview")
    }
}
