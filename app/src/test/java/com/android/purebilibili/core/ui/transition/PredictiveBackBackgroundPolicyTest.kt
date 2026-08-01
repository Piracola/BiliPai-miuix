package com.android.purebilibili.core.ui.transition

import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.navigation3.BiliPaiNavKey
import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PredictiveBackBackgroundPolicyTest {
    @Test
    fun gestureProgressAndFramesRemainStable() {
        assertEquals(1f, resolvePredictiveBackGestureBlurProgress(0f))
        assertEquals(0.25f, resolvePredictiveBackGestureBlurProgress(0.5f))
        assertEquals(0f, resolvePredictiveBackGestureBlurProgress(1f))
        assertEquals(0f, resolvePredictiveBackBlurFrame(1f, MotionTier.Reduced).blurRadiusPx)
    }

    @Test
    fun predictiveBackgroundBlurIsDisabledForPageStack() {
        assertFalse(
            shouldApplyPredictiveBackGestureBlur(
                routeTransition = BiliPaiNavRouteTransition.STACK,
                predictiveBackEnabled = true,
                gestureReturningVideoCard = false,
                motionTier = MotionTier.Normal,
            )
        )
    }

    @Test
    fun blurRouteMatcherStillTargetsOnlyTheBackEntry() {
        assertTrue(
            shouldApplyPredictiveBackBlurToRoute(
                entryKey = BiliPaiNavKey.MainHost,
                targetBackKey = BiliPaiNavKey.MainHost,
            )
        )
        assertFalse(
            shouldApplyPredictiveBackBlurToRoute(
                entryKey = BiliPaiNavKey.Search,
                targetBackKey = BiliPaiNavKey.MainHost,
            )
        )
    }
}
