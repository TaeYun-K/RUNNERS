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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.runners.app.auth.LoginScreen
import com.runners.app.ui.theme.RUNNERSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RUNNERSTheme {
				var idToken by remember { mutableStateOf<String?>(null) }

				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					if (idToken == null) {
						LoginScreen(
							onIdToken = { idToken = it },
							modifier = Modifier.padding(innerPadding)
						)
					} else {
						Greeting(
							name = "Logged in (idToken received)",
							modifier = Modifier.padding(innerPadding)
						)
					}
				}
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RUNNERSTheme {
        Greeting("Android")
    }
}
