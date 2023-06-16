package com.example.nattklar.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

// We only define one color scheme (dark) as this fits the theme of the app
private val mainColorScheme = darkColorScheme(
    primary = Color(0xFFfafafa),
    secondary = Color(0xFF2d2d2d),
    tertiary = Color(0xFF0d0d0d),
    outline = Color(0xFF2b2b2b),
    outlineVariant = Color(0xFF2b2b2b),
    background = Color(0xFF141414),
    surface = Color(0xFF009092),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF2d2d2d),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

@Composable
fun NattklarTheme(
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> mainColorScheme
    }


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.onTertiary.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}