package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Asserts shared adaptive primitives converge on the single Miuix renderer
 * decision so feature screens get the right look without primitive call sites
 * changing. Compose UI tests would assert actual rendered nodes; here we
 * assert the policy layer that drives the dispatch.
 */
class PrimitivePresetCoverageTest {

    @Test
    fun unifiedRenderer_matchesMiuixOnlyModel() {
        // 单主题模型：所有风格统一走 Miuix 桥接渲染器。
        assertEquals(
            PresetPrimitiveRenderer.MIUIX_BRIDGED,
            resolvePresetPrimitiveRenderer()
        )
    }

    @Test
    fun legacyLargeTitleBar_isRemovedAfterFeatureMigration() {
        val legacySource = listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/iOSLargeTitleBar.kt"),
        )
        assertEquals(false, legacySource.any { it.exists() })
    }

    @Test
    fun dialogActionLayoutPolicy_isConstantAfterIosMigration() {
        // 2B 迁移：iOS 全宽铺满操作区行为已随单向迁移删除，布局政策收敛为常量。
        assertEquals(false, resolveDialogActionLayoutPolicy().expandToContainer)
    }

    @Test
    fun adaptiveBottomSheetVisual_usesMiuixCapsule() {
        // 单主题模型：统一使用胶囊圆角与 Material 拖拽把手。
        val spec = resolveAdaptiveBottomSheetVisualSpec()
        assertEquals(22, spec.cornerRadiusDp)
        assertTrue(spec.useMaterialDragHandle)
    }
}
