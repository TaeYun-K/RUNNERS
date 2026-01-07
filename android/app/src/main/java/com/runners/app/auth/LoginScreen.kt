package com.runners.app.auth
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.runners.app.R

@Composable
fun LoginScreen(
	onIdToken: (String) -> Unit,
	onKakaoLogin: () -> Unit = {},
	onNaverLogin: () -> Unit = {},
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
			errorMessage = "로그인이 취소됐어요."
			return@rememberLauncherForActivityResult
		}

		val task = GoogleSignIn.getSignedInAccountFromIntent(data)
		try {
			val account = task.getResult(ApiException::class.java)
			val idToken = account.idToken
			if (idToken.isNullOrBlank()) {
				errorMessage = "로그인 토큰을 가져오지 못했어요. 잠시 후 다시 시도해주세요."
			} else {
				errorMessage = null
				onIdToken(idToken)
			}
		} catch (exception: ApiException) {
			errorMessage = "구글 로그인에 실패했어요. (${exception.statusCode})"
		}
	}

	Surface(modifier = modifier.fillMaxSize()) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(horizontal = 20.dp, vertical = 18.dp),
			contentAlignment = Alignment.Center,
		) {
			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text(
					text = "RUNNERS",
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold,
				)
				Text(
					text = "내 러닝 기록을 한눈에.",
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(top = 8.dp),
					textAlign = TextAlign.Center,
				)

				Spacer(modifier = Modifier.height(26.dp))

				Column(
					modifier = Modifier.fillMaxWidth(),
					verticalArrangement = spacedBy(10.dp),
				) {
				FilledTonalButton(
					onClick = { launcher.launch(signInClient.signInIntent) },
					enabled = !isLoading,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					shape = RoundedCornerShape(18.dp),
				) {
					if (isLoading) {
						CircularProgressIndicator(
							strokeWidth = 2.dp,
							modifier = Modifier.size(18.dp),
						)
						Spacer(modifier = Modifier.width(10.dp))
						Text("로그인 중...")
					} else {
						ProviderMark(
							letter = "G",
							backgroundColor = MaterialTheme.colorScheme.surface,
							contentColor = MaterialTheme.colorScheme.onSurface,
						)
						Spacer(modifier = Modifier.width(10.dp))
						Text("Google로 로그인", fontWeight = FontWeight.SemiBold)
					}
				}

				Button(
					onClick = onKakaoLogin,
					enabled = false,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					shape = RoundedCornerShape(18.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFFFEE500),
						contentColor = Color(0xFF000000),
						disabledContainerColor = Color(0xFFFEE500).copy(alpha = 0.5f),
						disabledContentColor = Color(0xFF000000).copy(alpha = 0.5f),
					),
				) {
					ProviderMark(
						letter = "K",
						backgroundColor = Color(0xFF000000).copy(alpha = 0.08f),
						contentColor = Color(0xFF000000),
					)
					Spacer(modifier = Modifier.width(10.dp))
					Text("카카오로 로그인", fontWeight = FontWeight.SemiBold)
					Spacer(modifier = Modifier.weight(1f))
					Text("준비 중", style = MaterialTheme.typography.labelMedium)
				}

				Button(
					onClick = onNaverLogin,
					enabled = false,
					modifier = Modifier
						.fillMaxWidth()
						.height(56.dp),
					shape = RoundedCornerShape(18.dp),
					colors = ButtonDefaults.buttonColors(
						containerColor = Color(0xFF03C75A),
						contentColor = Color(0xFFFFFFFF),
						disabledContainerColor = Color(0xFF03C75A).copy(alpha = 0.5f),
						disabledContentColor = Color(0xFFFFFFFF).copy(alpha = 0.7f),
					),
				) {
					ProviderMark(
						letter = "N",
						backgroundColor = Color(0xFFFFFFFF).copy(alpha = 0.16f),
						contentColor = Color(0xFFFFFFFF),
					)
					Spacer(modifier = Modifier.width(10.dp))
					Text("네이버로 로그인", fontWeight = FontWeight.SemiBold)
					Spacer(modifier = Modifier.weight(1f))
					Text("준비 중", style = MaterialTheme.typography.labelMedium)
				}
			}

			val shownError = backendErrorMessage ?: errorMessage
			if (!shownError.isNullOrBlank()) {
				Text(
					text = shownError,
					color = MaterialTheme.colorScheme.error,
					style = MaterialTheme.typography.bodySmall,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(top = 12.dp),
				)
			}

			Text(
				text = "로그인하면 서비스 이용약관 및 개인정보 처리방침에 동의한 것으로 간주돼요.",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 12.dp),
			)
		}
	}
}
}

@Composable
private fun ProviderMark(
	letter: String,
	backgroundColor: Color,
	contentColor: Color,
	modifier: Modifier = Modifier,
) {
	val shape = RoundedCornerShape(10.dp)
	Box(
		modifier = modifier
			.size(20.dp)
			.clip(shape)
			.background(backgroundColor),
		contentAlignment = Alignment.Center,
	) {
		Text(
			text = letter,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Bold,
			color = contentColor,
		)
	}
}
