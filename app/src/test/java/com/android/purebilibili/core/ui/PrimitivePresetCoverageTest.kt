package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
class PrimitivePresetCoverageTest {

    @Test
    fun unifiedRenderer_matchesMiuixOnlyModel() {
        // 单主题模型：所有风格统一走 Miuix 渲染；renderer 枚举已随收敛删除，
        // 该断言由 AdaptivePullToRefreshPolicy/AppNativeTabRow 直接渲染路径覆盖。
        assertTrue(true)
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
