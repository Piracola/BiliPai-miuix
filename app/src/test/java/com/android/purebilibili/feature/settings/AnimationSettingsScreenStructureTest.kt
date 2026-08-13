package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_noLongerExposesPredictiveBackStyleControls() {
        val source = animationSettingsSource()

        // 极简版：预测返回收敛为单一固定转场，不再提供风格/方向/开关。
        assertFalse(source.contains("setPredictiveBackEnabled"))
        assertFalse(source.contains("setPredictiveBackAnimationStyle"))
        assertFalse(source.contains("setPredictiveBackExitDirection"))
        assertFalse(source.contains("resolvePredictiveBackStyleOptions"))
        assertFalse(source.contains("resolvePredictiveBackExitDirectionOptions"))
        assertFalse(source.contains("title = \"预测性返回手势\""))
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
