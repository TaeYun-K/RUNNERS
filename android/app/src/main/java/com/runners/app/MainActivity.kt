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
import com.runners.app.network.BackendUserApi
import com.runners.app.network.GoogleLoginResult
import com.runners.app.ui.theme.RUNNERSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController

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

				LaunchedEffect(Unit) {
					AuthTokenStore.load(context)

					val refreshToken = AuthTokenStore.peekRefreshToken()
					if (!refreshToken.isNullOrBlank() && session == null) {
						isLoading = true
						errorMessage = null
						try {
							val newAccessToken = withContext(Dispatchers.IO) {
								BackendAuthApi.refreshAccessToken(refreshToken)
							}
							AuthTokenStore.setAccessToken(context, newAccessToken)

							val me = withContext(Dispatchers.IO) { BackendUserApi.getMe() }
							session = GoogleLoginResult(
								userId = me.userId,
								email = me.email,
								name = me.name,
								nickname = me.nickname,
								picture = me.picture,
								accessToken = newAccessToken,
								refreshToken = refreshToken,
								isNewUser = false,
							)
						} catch (e: Exception) {
							AuthTokenStore.clear(context)
							session = null
						} finally {
							isLoading = false
						}
					}
				}

				Scaffold(
					modifier = Modifier.fillMaxSize(),
					bottomBar = { if (session != null) RunnersBottomBar(navController) },
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
										AuthTokenStore.setTokens(context, result.accessToken, result.refreshToken)
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
