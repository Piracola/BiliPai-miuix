package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveSideNavigationRailIntegrationTest {

    @Test
    fun adaptiveNavigationUsesSharedRailPolicyWithoutLegacyAppImplementation() {
        val legacySource = File(
            "src/main/java/com/android/purebilibili/core/ui/AdaptiveNavigation.kt"
        ).takeIf { it.exists() } ?: File(
            "app/src/main/java/com/android/purebilibili/core/ui/AdaptiveNavigation.kt"
        )
        // 阶段 3 收敛：AdaptiveSideNavigationRailPolicy（单值 renderer 抽象）已删除，
        // 侧栏渲染直接走 app 的 SideBarRendererPolicy（Miuix-only）。
        val railPolicySource = listOf(
            File(
                "design-system/src/main/java/com/android/purebilibili/core/ui/" +
                    "AdaptiveSideNavigationRailPolicy.kt"
            ),
            File(
                "../design-system/src/main/java/com/android/purebilibili/core/ui/" +
                    "AdaptiveSideNavigationRailPolicy.kt"
            ),
        ).filter { it.exists() }
        val appPolicySource = listOf(
            File(
                "src/main/java/com/android/purebilibili/feature/home/components/" +
                    "SideBarRendererPolicy.kt"
            ),
            File(
                "app/src/main/java/com/android/purebilibili/feature/home/components/" +
                    "SideBarRendererPolicy.kt"
            ),
        ).filter { it.exists() }

        assertFalse(legacySource.exists())
        // 共享 policy 已随收敛删除，不再要求它存在。
        assertTrue(railPolicySource.isEmpty())
        assertTrue(appPolicySource.isNotEmpty())
    }
}
