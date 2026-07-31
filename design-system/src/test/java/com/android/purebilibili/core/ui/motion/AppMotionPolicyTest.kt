package com.android.purebilibili.core.ui.motion

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppMotionPolicyTest {

    @Test
    fun defaultPolicyAllowsOnlyShortTransientMotion() {
        val policy = AppMotionPolicy.default()

        assertEquals(100, policy.transientVisibilityDurationMillis)
        assertTrue(policy.shouldUsePlatformPredictiveBack())
        assertFalse(policy.shouldRunDecorativeMotion())
    }

    @Test
    fun systemReducedMotionDisablesAllAppInitiatedMotion() {
        val policy = AppMotionPolicy.default(systemReduceMotion = true)

        assertEquals(0, policy.transientVisibilityDurationMillis)
        assertFalse(policy.shouldUsePlatformPredictiveBack())
        assertFalse(policy.isMotionEnabled)
    }

    @Test
    fun appOffModeDisablesTransientMotion() {
        val policy = AppMotionPolicy.default(mode = AppMotionMode.OFF)

        assertEquals(0, policy.transientVisibilityDurationMillis)
        assertFalse(policy.isMotionEnabled)
        assertFalse(policy.shouldRunDecorativeMotion())
    }
}
