package com.android.purebilibili.core.ui

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.ui.unit.IntOffset

import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.core.ui.motion.navigationSlideSpring
import com.android.purebilibili.core.ui.motion.pullRefreshReleaseSpring
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppMotionTokensTest {

    @Test
    fun singleMiuix_standardSpec_isTween180ms() {
        val spec = AppMotionTokens.resolveStandardSpec<Float>()
        val tween = spec as? TweenSpec<Float>
            ?: error("expected TweenSpec, got ${spec::class.simpleName}")
        assertEquals(180, tween.durationMillis)
    }

    @Test
    fun singleMiuix_emphasizedSpec_isTween240ms() {
        val spec = AppMotionTokens.resolveEmphasizedSpec<Float>()
        val tween = spec as? TweenSpec<Float>
            ?: error("expected TweenSpec, got ${spec::class.simpleName}")
        assertEquals(240, tween.durationMillis)
    }

    @Test
    fun spatialSpec_keepsSharedElementSpringParameters() {
        val spec = AppMotionTokens.resolveSpatialSpec<androidx.compose.ui.geometry.Rect>()
        val spring = spec as? SpringSpec<androidx.compose.ui.geometry.Rect>
            ?: error("expected SpringSpec, got ${spec::class.simpleName}")

        assertEquals(0.82f, spring.dampingRatio, "spatial damping")
        assertEquals(380f, spring.stiffness, "spatial stiffness")
    }

    @Test
    fun chromeTokens_exposeMotionMillis() {
        val miuix = resolveAndroidNativeChromeTokens()

        assertEquals(180, miuix.motionStandardMillis)
        assertEquals(240, miuix.motionEmphasizedMillis)
    }

    @Test
    fun singleMiuix_bottomSheetSlideSpec_usesDenserTween() {
        val spec = AppMotionTokens.resolveBottomSheetSlideSpec<Int>()
        val tween = spec as? TweenSpec<Int>
            ?: error("expected TweenSpec, got ${spec::class.simpleName}")
        assertEquals(180, tween.durationMillis)
    }

    @Test
    fun pullRefreshReleaseSpring_usesTightDampingForSmallRebound() {
        val spring = pullRefreshReleaseSpring()

        assertTrue(spring.dampingRatio >= 0.94f, "pull refresh release should not visibly bounce")
        assertTrue(spring.stiffness >= 480f, "pull refresh release should settle quickly")
    }

    @Test
    fun navigationSlideSpring_scalesStiffnessWithTargetDuration() {
        val fast = navigationSlideSpring(durationMillis = 220)
        val standard = navigationSlideSpring(durationMillis = 300)
        val slow = navigationSlideSpring(durationMillis = 350)

        assertEquals(1f, fast.dampingRatio)
        assertTrue(fast.stiffness > standard.stiffness)
        assertTrue(standard.stiffness > slow.stiffness)
        assertTrue(fast.stiffness >= 1_700f)
        assertTrue(standard.stiffness >= 900f)
        assertEquals(700f, slow.stiffness)
        assertEquals(IntOffset(1, 1), slow.visibilityThreshold)
    }
}
