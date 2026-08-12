package com.android.purebilibili.feature.audio.screen

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class MusicPlayerContentColorPolicyTest {

    private val onLight = Color(0xFF1C1B1F) // typical MD3 onSurface
    private val onDark = Color(0xFFE6E1E5) // typical MD3 inverseOnSurface

    @Test
    fun lightPaletteUsesThemeOnSurfaceToken() {
        assertEquals(
            onLight,
            resolveMusicPlayerContentColor(
                backgroundColor = Color(0xFFF5F5F5),
                onLightBackground = onLight,
                onDarkBackground = onDark,
            )
        )
    }

    @Test
    fun darkPaletteUsesThemeInverseOnSurfaceToken() {
        assertEquals(
            onDark,
            resolveMusicPlayerContentColor(
                backgroundColor = Color(0xFF342B42),
                onLightBackground = onLight,
                onDarkBackground = onDark,
            )
        )
    }

    @Test
    fun borderlineLightBackgroundStillPrefersOnSurface() {
        assertEquals(
            onLight,
            resolveMusicPlayerContentColor(
                backgroundColor = Color(0xFFBDBDBD),
                onLightBackground = onLight,
                onDarkBackground = onDark,
            )
        )
    }

    @Test
    fun accentColorUsesThemePrimary() {
        val primary = Color(0xFFFF2D55)
        assertEquals(primary, resolveMusicPlayerAccentColor(primary))
    }

    @Test
    fun darkBackgroundCanUseHighContrastWhite() {
        assertEquals(
            Color.White,
            resolveMusicPlayerContentColor(
                backgroundColor = Color(0xFF2A2A2A),
                onLightBackground = onLight,
                onDarkBackground = Color.White,
            )
        )
    }
}
