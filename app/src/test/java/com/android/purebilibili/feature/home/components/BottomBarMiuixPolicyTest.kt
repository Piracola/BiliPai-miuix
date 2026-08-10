package com.android.purebilibili.feature.home.components

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.components.AppPlatformNavigationBarDisplayMode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BottomBarMiuixPolicyTest {

    @Test
    fun `runtime low blur budget disables expensive liquid glass effects`() {
        assertTrue(
            shouldRenderBottomBarLiquidGlassEffects(
                glassEnabled = true,
                forceLowBlurBudget = false,
            )
        )
        assertFalse(
            shouldRenderBottomBarLiquidGlassEffects(
                glassEnabled = true,
                forceLowBlurBudget = true,
            )
        )
        assertFalse(
            shouldRenderBottomBarLiquidGlassEffects(
                glassEnabled = false,
                forceLowBlurBudget = false,
            )
        )

        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")
        val renderer = source
            .substringAfter("private fun KernelSuAlignedBottomBar(")
            .substringBefore("@Composable\nprivate fun AndroidNativeBottomBarItem(")
        assertTrue(renderer.contains("val effectiveGlassEnabled = shouldRenderBottomBarLiquidGlassEffects("))
        assertTrue(renderer.contains("val glassLayersAlwaysOn = effectiveGlassEnabled && miuixBackdrop != null"))
        assertTrue(renderer.contains("shouldUseBottomBarCaptureLens(effectiveGlassEnabled)"))
        assertTrue(renderer.contains("glassEnabled = effectiveGlassEnabled"))
    }

    @Test
    fun `floating android native bottom bar adopts miuix chrome defaults`() {
        val spec = resolveMd3BottomBarFloatingChromeSpec(isFloating = true)

        assertEquals(50f, spec.cornerRadiusDp)
        assertEquals(36f, spec.horizontalOutsidePaddingDp)
        assertEquals(12f, spec.innerHorizontalPaddingDp)
        assertEquals(12f, spec.itemSpacingDp)
        assertEquals(1f, spec.shadowElevationDp)
        assertFalse(spec.showDivider)
    }

    @Test
    fun `material label mode maps to matching miuix display mode`() {
        assertEquals(
            Md3BottomBarDisplayMode.IconAndText,
            resolveMd3BottomBarDisplayMode(labelMode = 0)
        )
        assertEquals(
            Md3BottomBarDisplayMode.IconOnly,
            resolveMd3BottomBarDisplayMode(labelMode = 1)
        )
        assertEquals(
            Md3BottomBarDisplayMode.TextOnly,
            resolveMd3BottomBarDisplayMode(labelMode = 2)
        )
        assertEquals(
            Md3BottomBarDisplayMode.IconAndText,
            resolveMd3BottomBarDisplayMode(labelMode = 99)
        )
    }

    @Test
    fun `platform navigation display mode maps text-only onto icon-with-selected-label`() {
        assertEquals(
            AppPlatformNavigationBarDisplayMode.ICON_AND_TEXT,
            Md3BottomBarDisplayMode.IconAndText.toAppPlatformNavigationDisplayMode()
        )
        assertEquals(
            AppPlatformNavigationBarDisplayMode.ICON_ONLY,
            Md3BottomBarDisplayMode.IconOnly.toAppPlatformNavigationDisplayMode()
        )
        assertEquals(
            AppPlatformNavigationBarDisplayMode.ICON_WITH_SELECTED_LABEL,
            Md3BottomBarDisplayMode.TextOnly.toAppPlatformNavigationDisplayMode()
        )
    }

    @Test
    fun `official miuix navigation item is used without skin chrome`() {
        assertTrue(
            shouldUseMiuixOfficialNavigationBarItem(
                skinIconPath = null,
                labelScrimAlpha = 0f
            )
        )
        assertFalse(
            shouldUseMiuixOfficialNavigationBarItem(
                skinIconPath = "/skin/home.png",
                labelScrimAlpha = 0f
            )
        )
        assertFalse(
            shouldUseMiuixOfficialNavigationBarItem(
                skinIconPath = null,
                labelScrimAlpha = 0.4f
            )
        )
    }

    @Test
    fun `docked miuix bottom item uses theme color when selected`() {
        val themeColor = Color(0xFFE85A91)
        val neutralColor = Color(0xFF9A9AA0)

        assertEquals(
            themeColor,
            resolveMiuixDockedBottomBarItemColor(
                selected = true,
                selectedColor = themeColor,
                unselectedColor = neutralColor
            )
        )
        assertEquals(
            neutralColor,
            resolveMiuixDockedBottomBarItemColor(
                selected = false,
                selectedColor = themeColor,
                unselectedColor = neutralColor
            )
        )
    }

    @Test
    fun `android native floating branch declares its own tuning entrypoint`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")

        assertTrue(source.contains("resolveAndroidNativeBottomBarTuning("))
        assertTrue(source.contains("resolveAndroidNativeBottomBarContainerColor("))
        assertTrue(source.contains("KernelSuAlignedBottomBar("))
        assertTrue(source.contains("iconStyle = sharedBarIconStyle"))
        assertTrue(source.contains("SharedFloatingBottomBarIconStyle.MATERIAL"))
    }

    @Test
    fun `android native floating branch uses sukisu three layer backdrop structure`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")

        assertTrue(source.contains("val tabsBackdrop = rememberMiuixLayerBackdrop()"))
        assertTrue(source.contains(".miuixLayerBackdrop(tabsBackdrop)"))
        assertTrue(source.contains("val contentBackdrop = if (shouldRenderIndicatorBackdrop && miuixBackdrop != null)"))
        assertTrue(source.contains("rememberMiuixCombinedBackdrop(miuixBackdrop, tabsBackdrop)"))
        assertTrue(
            source.contains(
                "miuixBlur(AppSpacingTokens.ExtraSmall.toPx(), AppSpacingTokens.ExtraSmall.toPx())"
            )
        )
        assertTrue(source.contains("refractionHeight = AppSpacingTokens.ExtraLarge.toPx()"))
        assertTrue(source.contains("refractionAmount = AppSpacingTokens.ExtraLarge.toPx()"))
        assertTrue(source.contains("BOTTOM_BAR_INDICATOR_DRAG_SCALE_TARGET = 78f / 56f"))
    }

    @Test
    fun `android native indicator backdrop matches sukisu lens order`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")

        assertTrue(
            Regex(
                """rememberMiuixCombinedBackdrop\(miuixBackdrop, tabsBackdrop\)[\s\S]*?miuixDrawBackdrop\([\s\S]*?effects = \{[\s\S]*?miuixLens\(""",
                RegexOption.MULTILINE
            ).containsMatchIn(source)
        )
    }

    @Test
    fun `android native indicator follows InstallerX combined page plus tabs capture`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")
        val renderer = source
            .substringAfter("private fun KernelSuAlignedBottomBar(")
            .substringBefore("@Composable\nprivate fun AndroidNativeBottomBarItem(")
        val refractionCaptureSource = source
            .substringAfter("if (shouldRenderIndicatorContentCapture && miuixBackdrop != null) {")
            .substringBefore("KernelSuMiuixBottomBarIndicatorLayer(")

        assertTrue(renderer.contains("rememberMiuixCombinedBackdrop(miuixBackdrop, tabsBackdrop)"))
        assertTrue(refractionCaptureSource.contains(".miuixLayerBackdrop(tabsBackdrop)"))
        assertTrue(refractionCaptureSource.contains("backdrop = miuixBackdrop"))
        assertTrue(refractionCaptureSource.contains("miuixDrawBackdrop("))
        assertTrue(refractionCaptureSource.contains("BOTTOM_BAR_INDICATOR_DOCK_BAND_HEIGHT_DP.dp"))
        assertFalse(refractionCaptureSource.contains("resolveBottomBarIndicatorExportCaptureHeightDp("))
        assertFalse(refractionCaptureSource.contains(".background(ksuContainerColor, shellShape)"))
        assertTrue(source.contains("BOTTOM_BAR_INDICATOR_DRAG_SCALE_TARGET = 78f / 56f"))
    }

    @Test
    fun `android native ordinary blur does not redraw raw backdrop over haze`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt")

        assertTrue(source.contains("if (backdrop != null && !useHazeBlur)"))
        assertTrue(source.contains("Modifier.unifiedBlur("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
