package com.aydanailroom.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ChalkColorScheme = darkColorScheme(
    primary = ChalkColors.Pink,
    onPrimary = Color(0xFF38202A),
    secondary = ChalkColors.Mint,
    onSecondary = Color(0xFF18332A),
    tertiary = ChalkColors.Lilac,
    onTertiary = Color(0xFF302344),
    background = ChalkColors.Board,
    onBackground = ChalkColors.Chalk,
    surface = ChalkColors.BoardDeep,
    onSurface = ChalkColors.Chalk,
    surfaceVariant = ChalkColors.BoardLight,
    onSurfaceVariant = ChalkColors.ChalkDim,
    error = ChalkColors.Coral,
    onError = Color(0xFF3A1715),
)

@Composable
fun AydaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChalkColorScheme,
        typography = AydaTypography,
        shapes = AydaShapes,
        content = content,
    )
}
