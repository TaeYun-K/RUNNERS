package com.runners.app

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.runners.app.auth.LoginScreen
import com.runners.app.healthconnect.HealthConnectRepository
import com.runners.app.notification.NotificationConstants
import com.runners.app.notification.NotificationTokenManager
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

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    private val requestHealthConnectPermission = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { _ -> }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractAndStoreNotificationPostId(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        extractAndStoreNotificationPostId(intent)
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
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
						if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
							requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
						}
					}
					if (HealthConnectRepository.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
						val client = HealthConnectRepository.getClient(context)
						val hasAll = withContext(Dispatchers.IO) {
							HealthConnectRepository.hasAllPermissions(
								client,
								HealthConnectRepository.requestedPermissions,
							)
						}
						if (!hasAll) {
							requestHealthConnectPermission.launch(HealthConnectRepository.requestedPermissions)
						}
					}
				}

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

				LaunchedEffect(session) {
					if (session != null) {
						NotificationTokenManager.registerTokenIfNeeded(context, scope)
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
									withContext(Dispatchers.IO) {
										runCatching { BackendAuthApi.logout() }
										NotificationTokenManager.removeTokenOnLogout(context)
									}
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

    private fun extractAndStoreNotificationPostId(intent: Intent?) {
        val postIdFromAppNotification = intent
            ?.getLongExtra(NotificationConstants.EXTRA_POST_ID, -1L)
            ?.takeIf { it > 0 }

        val extras = intent?.extras
        val postIdFromFcmData = when {
            extras == null -> null
            extras.containsKey(NotificationConstants.DATA_KEY_POST_ID) -> {
                (extras.getString(NotificationConstants.DATA_KEY_POST_ID)
                    ?: extras.getLong(NotificationConstants.DATA_KEY_POST_ID, -1L).takeIf { it > 0 }?.toString()
                    )?.toLongOrNull()
            }
            else -> null
        }?.takeIf { it > 0 }

        val postId = postIdFromAppNotification ?: postIdFromFcmData
        if (postId != null) {
            RunnersApplication.setPendingNotificationPostId(postId)
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
