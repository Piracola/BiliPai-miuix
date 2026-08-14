package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveBottomSheetPolicyTest {

    @Test
    fun `app sheet facade preserves caller content color`() {
        val path = "src/main/java/com/android/purebilibili/core/ui/AppSheetComponents.kt"
        val source = listOf(File(path), File("design-system/$path"))
            .firstOrNull(File::exists)
            ?.readText()
            ?: error("Cannot locate AppSheetComponents.kt from ${File(".").absolutePath}")

        assertTrue(source.contains("contentColor: Color = MaterialTheme.colorScheme.onSurface"))
        assertTrue(source.contains("contentColor = contentColor"))
        assertTrue(source.contains("content: @Composable ColumnScope.() -> Unit"))
    }

    @Test
    fun `single miuix theme uses native drag handle and miuix corner radius`() {
        val spec = resolveAdaptiveBottomSheetVisualSpec()

        assertEquals(22, spec.cornerRadiusDp)
        assertTrue(spec.useMaterialDragHandle)
    }

    @Test
    fun `single miuix theme uses softer sheet motion`() {
        val spec = resolveAdaptiveBottomSheetMotionSpec()

        assertEquals(240, spec.scrimEnterDurationMillis)
        assertEquals(180, spec.scrimExitDurationMillis)
        assertEquals(240, spec.contentEnterFadeDurationMillis)
        assertEquals(180, spec.contentExitFadeDurationMillis)
        assertTrue(spec.scrimExitDurationMillis < spec.scrimEnterDurationMillis)
        assertTrue(spec.contentExitFadeDurationMillis < spec.contentEnterFadeDurationMillis)
    }

    @Test
    fun `host contract resolves single theme to overlay host`() {
        assertEquals(BottomSheetHost.MIUIX_OVERLAY, resolveBottomSheetHost())
    }

    @Test
    fun `app sheet facade stays on neutral material host instead of mechanical overlay swap`() {
        val path = "src/main/java/com/android/purebilibili/core/ui/AppSheetComponents.kt"
        val source = listOf(File(path), File("design-system/$path"))
            .firstOrNull(File::exists)
            ?.readText()
            ?: error("Cannot locate AppSheetComponents.kt from ${File(".").absolutePath}")

        // OverlayBottomSheet 依赖 Miuix popup host（仅 AdaptiveScaffold 挂载），
        // AppModalBottomSheet 调用点无法保证处于该宿主之下 —— 宿主契约
        // 由 resolveBottomSheetHost 独立承担，facade 本身禁止机械替换。
        assertTrue(source.contains("ModalBottomSheet("))
        assertFalse(source.contains("OverlayBottomSheet("))
        assertFalse(source.contains("import top.yukonga.miuix.kmp.overlay"))
        assertTrue(source.contains("fun resolveBottomSheetHost("))
        assertTrue(source.contains("enum class BottomSheetHost"))
    }

    @Test
    fun `overlay visual progress should scale scrim and disable blur when hidden`() {
        val hidden = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 0f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )
        val half = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 0.5f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )
        val shown = resolveInteractiveOverlayProgressVisual(
            presentationProgress = 1f,
            surfaceType = InteractiveOverlaySurfaceType.BOTTOM_SHEET,
            blurActive = true,
            maxScrimAlpha = 0.5f
        )

        assertEquals(0f, hidden.scrimAlpha, 0.001f)
        assertFalse(hidden.blurEnabled)
        assertTrue(hidden.forceLowBlurBudget)

        assertEquals(0.25f, half.scrimAlpha, 0.001f)
        assertTrue(half.blurEnabled)
        assertTrue(half.forceLowBlurBudget)
        assertTrue(half.surfaceAlphaMultiplier < shown.surfaceAlphaMultiplier)

        assertEquals(0.5f, shown.scrimAlpha, 0.001f)
        assertTrue(shown.blurEnabled)
        assertFalse(shown.forceLowBlurBudget)
        assertEquals(1f, shown.surfaceAlphaMultiplier, 0.001f)
    }
}
