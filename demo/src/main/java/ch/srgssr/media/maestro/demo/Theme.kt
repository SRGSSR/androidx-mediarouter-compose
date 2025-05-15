/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.media.maestro.demo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
private val darkColorScheme = darkColorScheme(
    primary = Color(0xFF0B6997),
    onPrimary = Color(0xFFDADDDE),
    primaryContainer = Color(0xFF0B6997),
    onPrimaryContainer = Color(0xFFDADDDE),
    background = Color(0xFF1F1F21),
    surface = Color(0xFF3E3E3E),
    surfaceContainerHigh = Color(0xFF3E3E3E),
)

@Suppress("MagicNumber")
private val lightColorScheme = lightColorScheme(
    primary = Color(0xFF1C9BCE),
    onPrimary = Color(0xFFDADDDE),
    primaryContainer = Color(0xFF1C9BCE),
    onPrimaryContainer = Color(0xFFDADDDE),
    background = Color(0xFFDADDDE),
    surface = Color(0xFFD1D4D5),
    surfaceContainerHigh = Color(0xFFD1D4D5),
)

@Composable
fun DemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColorScheme else lightColorScheme,
        content = content,
    )
}
