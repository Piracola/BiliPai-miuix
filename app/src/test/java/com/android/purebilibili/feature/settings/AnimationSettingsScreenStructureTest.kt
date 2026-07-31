package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_exposesOnlyTheRetainedMotionPolicy() {
        val source = animationSettingsSource()

        assertTrue(source.contains("页面切换使用系统默认行为"))
        assertTrue(source.contains("应用仅保留播放器控制栏的短暂透明度反馈"))
        assertFalse(source.contains("SettingsManager.setPredictiveBackEnabled"))
        assertFalse(source.contains("setPredictiveBackAnimationStyle"))
        assertFalse(source.contains("setPredictiveBackExitDirection"))
        assertFalse(source.contains("resolvePredictiveBackStyleOptions"))
        assertFalse(source.contains("resolvePredictiveBackExitDirectionOptions"))
    }

    @Test
    fun animationSettingsScreen_doesNotExposeRemovedTransitionBlurToggle() {
        val source = animationSettingsSource()

        assertFalse(source.contains("过渡动画实时模糊"))
        assertFalse(source.contains("videoTransitionRealtimeBlurEnabled"))
        assertFalse(source.contains("toggleVideoTransitionRealtimeBlur"))
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
