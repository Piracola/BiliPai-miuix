package com.android.purebilibili.core.theme

import androidx.compose.material3.MotionScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MiuixThemePolicyTest {

    @Test
    fun singleMiuixTheme_usesMiuixAlignedTypography() {
        val typography = resolveMaterialTypography()

        assertEquals(BiliMiuixTypography.bodyMedium.fontSize, typography.bodyMedium.fontSize)
        assertEquals(BiliMiuixTypography.titleMedium.letterSpacing, typography.titleMedium.letterSpacing)
    }

    @Test
    fun singleMiuixTheme_usesLargerCornerScale() {
        assertEquals(
            MIUIX_CORNER_RADIUS_SCALE,
            resolveCornerRadiusScale()
        )
    }

    @Test
    fun singleMiuixTheme_keepsStandardMotionScheme() {
        val motionScheme = resolveMaterialMotionScheme()

        assertSame(MotionScheme.standard(), motionScheme)
    }

    @Test
    fun singleMiuixTheme_resolvesChromeTokens() {
        val miuix = resolveAndroidNativeChromeTokens()

        assertEquals(20, miuix.containerCornerRadiusDp)
        assertEquals(1f, miuix.motionScale)
        assertEquals(0, miuix.tonalSurfaceElevationDp)
        assertEquals(240, miuix.motionEmphasizedMillis)
        assertEquals(180, miuix.motionStandardMillis)
    }

    @Test
    fun singleMiuixTheme_resolvesShapes() {
        val shapes = resolveMaterialShapes()

        assertSame(MiuixAlignedShapes, shapes)
    }
}
