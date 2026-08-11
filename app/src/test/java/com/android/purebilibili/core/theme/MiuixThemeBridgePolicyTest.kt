package com.android.purebilibili.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiuixThemeBridgePolicyTest {

    @Test
    fun `material bridge preserves primary and surface roles from miuix colors`() {
        val materialScheme = resolveMaterialColorSchemeFromMiuixBridge(
            bridge = MiuixMaterialBridge(
                primary = Color(0xFF3482FF),
                onPrimary = Color.White,
                primaryFixed = Color(0xFFD7E3FF),
                onPrimaryFixed = Color(0xFF001B3F),
                primaryContainer = Color(0xFFE1ECFF),
                onPrimaryContainer = Color(0xFF001C3A),
                secondary = Color(0xFF5A5F71),
                onSecondary = Color.White,
                secondaryContainer = Color(0xFFDEE3F9),
                onSecondaryContainer = Color(0xFF171B2C),
                tertiary = Color(0xFF75546F),
                onTertiary = Color.White,
                tertiaryContainer = Color(0xFFFFD7F5),
                onTertiaryContainer = Color(0xFF2C1229),
                error = Color(0xFFBA1A1A),
                onError = Color.White,
                errorContainer = Color(0xFFFFDAD6),
                onErrorContainer = Color(0xFF410002),
                background = Color(0xFFF8F9FF),
                onBackground = Color(0xFF191C20),
                surface = Color(0xFFF8F9FF),
                onSurface = Color(0xFF191C20),
                surfaceVariant = Color(0xFFE0E2EC),
                onSurfaceVariant = Color(0xFF44474E),
                surfaceContainer = Color(0xFFECEEF4),
                surfaceContainerHigh = Color(0xFFE6E8EE),
                surfaceContainerHighest = Color(0xFFE0E2E8),
                outline = Color(0xFF74777F),
                outlineVariant = Color(0xFFC4C6D0)
            ),
            amoledDarkTheme = false
        )

        assertEquals(Color(0xFF3482FF), materialScheme.primary)
        assertEquals(Color(0xFFF8F9FF), materialScheme.background)
        assertEquals(Color(0xFFF8F9FF), materialScheme.surface)
        assertEquals(Color(0xFFECEEF4), materialScheme.surfaceContainer)
        assertEquals(Color(0xFFE6E8EE), materialScheme.surfaceContainerHigh)
    }

    @Test
    fun `miuix colors follow amoled material surfaces from bridge`() {
        val amoledScheme = applyAmoledSurfaceOverrides(
            darkColorScheme(
                primary = Color(0xFF84F2A4),
                background = Color(0xFF101414),
                surface = Color(0xFF161B1A),
                surfaceContainer = Color(0xFF1E2523)
            )
        )
        val bridge = createMiuixMaterialBridge(amoledScheme)
        val miuixColors = resolveMiuixColorsFromMaterialBridge(
            bridge = bridge,
            darkTheme = true
        )

        assertEquals(Color(0xFF84F2A4), miuixColors.primary)
        assertEquals(Color.Black, miuixColors.background)
        assertEquals(Color.Black, miuixColors.surface)
        assertEquals(Color(0xFF090909), miuixColors.surfaceContainer)
    }

    @Test
    fun `miuix colors track custom seed primary from material bridge`() {
        val seedPrimary = Color(0xFFFF5722)
        val bridge = createMiuixMaterialBridge(
            lightColorScheme(
                primary = seedPrimary,
                onPrimary = Color.White,
                background = Color(0xFFFFF8F6),
                surface = Color(0xFFFFF8F6),
                surfaceContainer = Color(0xFFFFEDE8)
            )
        )
        val miuixColors = resolveMiuixColorsFromMaterialBridge(
            bridge = bridge,
            darkTheme = false
        )

        assertEquals(seedPrimary, miuixColors.primary)
        assertEquals(Color(0xFFFFF8F6), miuixColors.background)
    }

    @Test
    fun `miuix bridge maps switch slider and disabled roles from material palette`() {
        val scheme = lightColorScheme(
            primary = Color(0xFF006A60),
            onPrimary = Color.White,
            primaryFixed = Color(0xFF9EF2E4),
            onPrimaryFixed = Color(0xFF00201C),
            outline = Color(0xFF6F7976),
            outlineVariant = Color(0xFFBEC9C5),
            surface = Color(0xFFF4FBF8),
            surfaceContainerHigh = Color(0xFFE2E9E6),
            surfaceContainerHighest = Color(0xFFDCE3E0),
        )
        val colors = resolveMiuixColorsFromMaterialBridge(
            bridge = createMiuixMaterialBridge(scheme),
            darkTheme = false,
        )

        assertEquals(scheme.primaryFixed, colors.primaryVariant)
        assertEquals(scheme.onPrimaryFixed, colors.onPrimaryVariant)
        assertEquals(scheme.outlineVariant, colors.secondary)
        assertTrue(calculateContrastRatio(colors.onSecondary, colors.secondary) >= 3f)
        assertEquals(scheme.surfaceContainerHighest, colors.secondaryContainerVariant)
        assertEquals(scheme.primary, colors.sliderKeyPoint)
        assertEquals(1f, colors.sliderBackground.alpha)
        assertEquals(1f, colors.disabledPrimary.alpha)
        assertEquals(1f, colors.disabledSecondary.alpha)
    }
}
