package com.federico.mylibrary.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun LibraryAppTheme(
    themeStyle: AppThemeStyle = AppThemeStyle.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (themeStyle) {
        AppThemeStyle.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
                else dynamicLightColorScheme(context)
            } else if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
        }

        AppThemeStyle.LIGHT -> LightColorScheme
        AppThemeStyle.DARK -> DarkColorScheme

        AppThemeStyle.NIGHT_BLUE -> darkColorScheme(
            primary = Color(0xFF0D47A1),
            background = Color(0xFF121212),
            onBackground = Color.White
        )

        AppThemeStyle.COFFEE -> darkColorScheme(
            primary = Color(0xFF6D4C41),
            background = Color(0xFF3E2723),
            onBackground = Color.White
        )

        AppThemeStyle.FOREST_GREEN -> darkColorScheme(
            primary = Color(0xFF2E7D32),
            background = Color(0xFF1B5E20),
            onBackground = Color.White
        )

        AppThemeStyle.PASTEL -> lightColorScheme(
            primary = Color(0xFFF48FB1),
            background = Color(0xFFFFF1F1),
            onBackground = Color.Black
        )

        AppThemeStyle.HIGH_CONTRAST -> darkColorScheme(
            primary = Color.White,
            onPrimary = Color.Black,
            background = Color.Black,
            onBackground = Color.White,
            surface = Color(0xFF121212),
            onSurface = Color.White,
            surfaceVariant = Color(0xFF2C2C2C),
            outline = Color.Gray
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF03DAC6)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFF018786)
)
