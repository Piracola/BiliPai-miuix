package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppDrawerContainerTreatment
import com.android.purebilibili.core.ui.resolveAppDrawerVisualPolicy
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class MineSideDrawerVisualPolicyTest {

    @Test
    fun `blur-enabled drawer should keep translucent glass surface`() {
        val light = resolveDrawerGlassPalette(isDark = false, blurEnabled = true)
        val dark = resolveDrawerGlassPalette(isDark = true, blurEnabled = true)

        assertTrue(light.drawerBaseAlpha <= 0.34f)
        assertTrue(dark.drawerBaseAlpha <= 0.38f)
        assertTrue(light.itemSurfaceAlpha <= 0.22f)
        assertTrue(dark.itemSurfaceAlpha <= 0.20f)
    }

    @Test
    fun `blur-disabled drawer can stay opaque for readability`() {
        val light = resolveDrawerGlassPalette(isDark = false, blurEnabled = false)
        val dark = resolveDrawerGlassPalette(isDark = true, blurEnabled = false)

        assertTrue(light.drawerBaseAlpha >= 0.92f)
        assertTrue(dark.drawerBaseAlpha >= 0.92f)
    }

    @Test
    fun `drawer scrim should stay light when blur is enabled`() {
        val blurScrim = resolveHomeDrawerScrimAlpha(blurEnabled = true)
        val opaqueScrim = resolveHomeDrawerScrimAlpha(blurEnabled = false)

        assertTrue(blurScrim <= 0.16f)
        assertTrue(opaqueScrim >= 0.24f)
    }

    @Test
    fun `drawer transition should use more solid glass palette while blur budget is reduced`() {
        val stable = resolveDrawerGlassPalette(
            isDark = false,
            blurEnabled = true,
            budget = DrawerMotionBudget.FULL
        )
        val transitioning = resolveDrawerGlassPalette(
            isDark = false,
            blurEnabled = true,
            budget = DrawerMotionBudget.REDUCED
        )

        assertTrue(transitioning.drawerBaseAlpha > stable.drawerBaseAlpha)
        assertTrue(transitioning.itemSurfaceAlpha >= stable.itemSurfaceAlpha)
        assertTrue(transitioning.hazeBackgroundAlpha > stable.hazeBackgroundAlpha)
    }

    @Test
    fun `drawer should use opaque containers and larger chevron when blur is off`() {
        val policy = resolveAppDrawerVisualPolicy(
            blurEnabled = false
        )

        assertEquals(AppDrawerContainerTreatment.OPAQUE, policy.containerTreatment)
        assertEquals(20, policy.profileChevronSizeDp)
    }

    @Test
    fun `drawers keep translucent glass while blur is active`() {
        val policy = resolveAppDrawerVisualPolicy(
            blurEnabled = true,
        )

        assertEquals(AppDrawerContainerTreatment.TRANSLUCENT, policy.containerTreatment)
        assertEquals(20, policy.profileChevronSizeDp)
    }
}
