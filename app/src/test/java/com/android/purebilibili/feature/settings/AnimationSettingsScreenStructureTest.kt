package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_controlsGlobalPredictivePreviewIndependently() {
        val source = animationSettingsSource()

        // 全局返回动画（样式选择）独立于实时画面卡片转场开关。
        assertTrue(source.contains("title = \"全局返回动画\""))
        assertTrue(source.contains("SettingsManager.setPredictiveBackEnabled(context, true)"))
        val predictiveItem = source
            .substringAfter("title = \"全局返回动画\"")
            .substringBefore("title = \"全屏滑动返回\"")
        assertFalse(predictiveItem.contains("enabled = state.cardTransitionEnabled"))
        // 样式与退出方向选项存在，且选择样式即启用预测性返回。
        assertTrue(source.contains("SettingsManager.setPredictiveBackAnimationStyle("))
        assertTrue(source.contains("SettingsManager.setPredictiveBackExitDirection("))
        assertTrue(source.contains("predictiveBackStyleOptions"))
        assertTrue(source.contains("predictiveBackExitDirectionOptions"))
        // 全屏滑动返回是独立开关，不再与全局返回动画共用图标。
        assertTrue(source.contains("title = \"全屏滑动返回\""))
        assertTrue(source.contains("fullScreenSwipeBackEnabled"))
    }

    @Test
    fun animationSettingsScreen_exposesRealtimeTransitionBlurToggle() {
        val source = animationSettingsSource()

        assertTrue(source.contains("title = \"转场时模糊背景\""))
        assertTrue(source.contains("checked = videoTransitionRealtimeBlurEnabled"))
        assertTrue(source.contains("toggleVideoTransitionRealtimeBlur"))
    }

    @Test
    fun animationSettingsScreen_exposesLiveSurfaceCardTransitionToggle() {
        val source = animationSettingsSource()

        assertTrue(source.contains("title = \"实时画面转场\""))
        assertTrue(source.contains("checked = liveSurfaceCardTransitionEnabled"))
        assertTrue(source.contains("toggleLiveSurfaceCardTransition"))
        assertTrue(source.contains("enabled = state.cardTransitionEnabled"))
        assertTrue(source.contains("getLiveSurfaceCardTransitionEnabled"))
    }

    @Test
    fun animationSettingsScreen_doesNotExposeLiveReturnPreviewToggle() {
        val source = animationSettingsSource()

        assertFalse(source.contains("预测返回预览实时画面"))
        assertFalse(source.contains("videoTransitionLiveReturnPreviewEnabled"))
        assertFalse(source.contains("setVideoTransitionLiveReturnPreviewEnabled"))
        assertFalse(source.contains("getVideoTransitionLiveReturnPreviewEnabled"))
    }

    private fun animationSettingsSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
        ).first { it.exists() }.readText()
    }
}
