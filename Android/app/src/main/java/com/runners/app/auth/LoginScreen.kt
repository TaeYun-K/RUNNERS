package com.runners.app.auth
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.runners.app.R

@Composable
fun LoginScreen(
	onIdToken: (String) -> Unit,
	isLoading: Boolean = false,
	backendErrorMessage: String? = null,
	modifier: Modifier = Modifier
) {
	val context = LocalContext.current
	val webClientId = stringResource(R.string.google_web_client_id)
	var errorMessage by remember { mutableStateOf<String?>(null) }

	val signInClient = remember(webClientId) {
		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.requestIdToken(webClientId)
			.build()
		GoogleSignIn.getClient(context, gso)
	}

	val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		val data = result.data
		if (data == null) {
			errorMessage = "Google sign-in canceled"
			return@rememberLauncherForActivityResult
		}

		val task = GoogleSignIn.getSignedInAccountFromIntent(data)
		try {
			val account = task.getResult(ApiException::class.java)
			val idToken = account.idToken
			if (idToken.isNullOrBlank()) {
				errorMessage = "Missing Google ID token (check Web client ID)"
			} else {
				errorMessage = null
				onIdToken(idToken)
			}
		} catch (exception: ApiException) {
            errorMessage = "Google sign-in failed: ${exception.statusCode} / ${exception.message}"
		}
	}

	Column(
		modifier = modifier.fillMaxSize().padding(24.dp),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text("RUNNERS", style = MaterialTheme.typography.headlineMedium)
		Text("구글로 로그인", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

		Button(
			onClick = { launcher.launch(signInClient.signInIntent) },
			enabled = !isLoading
		) {
			Text("Google로 로그인")
		}

		if (isLoading) {
			Text(
				text = "서버 로그인 중...",
				modifier = Modifier.padding(top = 16.dp)
			)
		}

		if (errorMessage != null) {
			Text(
				text = errorMessage!!,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier.padding(top = 16.dp)
			)
		}

		if (backendErrorMessage != null) {
			Text(
				text = backendErrorMessage,
				color = MaterialTheme.colorScheme.error,
				modifier = Modifier.padding(top = 16.dp)
			)
		}
	}
}
