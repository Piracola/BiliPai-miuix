package com.android.purebilibili.core.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiuixThemeBridgePolicyTest {

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
