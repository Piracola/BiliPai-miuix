package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse

class AnimationSettingsPolicyTest {

    @Test
    fun bottomBarLiquidGlassUsesUnifiedMaterialWithoutPresetPicker() {
        val animationSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"
        )
        val bottomBarSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/BottomBarSettingsScreen.kt"
        )
        val settingsManagerSource = loadSource(
            "app/src/main/java/com/android/purebilibili/core/store/SettingsManager.kt"
        )

        assertFalse(animationSource.contains("当前底栏材质"))
        assertFalse(animationSource.contains("BottomBarLiquidGlassPreset"))
        assertFalse(animationSource.contains("getBottomBarLiquidGlassPreset"))
        assertFalse(settingsManagerSource.contains("getBottomBarLiquidGlassPreset"))
        assertFalse(settingsManagerSource.contains("setBottomBarLiquidGlassPreset"))
        assertFalse(animationSource.contains("底栏跟随高光"))
        assertFalse(animationSource.contains("getBottomBarInteractiveHighlightEnabled"))
        assertFalse(animationSource.contains("setBottomBarInteractiveHighlightEnabled"))
        assertFalse(bottomBarSource.contains("BottomBarLiquidGlassPreset"))
        assertFalse(bottomBarSource.contains("底栏跟随高光"))
    }

    @Test
    fun removedBackPreviewEntry_isRemovedFromAnimationSettings() {
        val animationSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"
        )
        val policySource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/AnimationSettingsPolicy.kt"
        )

        assertFalse(animationSource.contains("预测性返回动画"))
        assertFalse(animationSource.contains("Predictive" + "BackAnimationDialog"))
        assertFalse(policySource.contains("Predictive" + "BackToggleUiState"))
        assertFalse(policySource.contains("resolvePredictive" + "BackToggleUiState"))
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
