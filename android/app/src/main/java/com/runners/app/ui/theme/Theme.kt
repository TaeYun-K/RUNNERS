package com.runners.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Primary
    primary = Blue40,
    onPrimary = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,

    // Secondary
    secondary = Teal40,
    onSecondary = Color.White,
    secondaryContainer = Teal90,
    onSecondaryContainer = Teal10,

    // Tertiary
    tertiary = Blue60,
    onTertiary = Color.White,
    tertiaryContainer = Blue95,
    onTertiaryContainer = Blue20,

    // Error
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10,

    // Background & Surface
    background = SurfaceLight,
    onBackground = Gray10,
    surface = SurfaceLight,
    onSurface = Gray10,
    surfaceVariant = Gray95,
    onSurfaceVariant = Gray40,

    // Container
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Gray99,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = Gray95,
    surfaceContainerHighest = Gray90,

    // Others
    outline = Gray60,
    outlineVariant = Gray80,
    inverseSurface = Gray20,
    inverseOnSurface = Gray95,
    inversePrimary = Blue80,
)

private val DarkColorScheme = darkColorScheme(
    // Primary
    primary = Blue70,
    onPrimary = Blue10,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,

    // Secondary
    secondary = Teal70,
    onSecondary = Teal10,
    secondaryContainer = Teal30,
    onSecondaryContainer = Teal90,

    // Tertiary
    tertiary = Blue60,
    onTertiary = Blue10,
    tertiaryContainer = Blue20,
    onTertiaryContainer = Blue90,

    // Error
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,

    // Background & Surface
    background = SurfaceDark,
    onBackground = Gray90,
    surface = SurfaceDark,
    onSurface = Gray90,
    surfaceVariant = Gray30,
    onSurfaceVariant = Gray70,

    // Container
    surfaceContainerLowest = Gray10,
    surfaceContainerLow = Gray20,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = Gray30,
    surfaceContainerHighest = Gray40,

    // Others
    outline = Gray50,
    outlineVariant = Gray40,
    inverseSurface = Gray90,
    inverseOnSurface = Gray20,
    inversePrimary = Blue40,
)

@Composable
fun RUNNERSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // 커스텀 테마 사용을 위해 기본값 false
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
