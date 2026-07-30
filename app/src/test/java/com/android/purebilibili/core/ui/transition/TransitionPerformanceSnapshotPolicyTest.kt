package com.android.purebilibili.core.ui.transition

import com.android.purebilibili.core.ui.adaptive.MotionTier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TransitionPerformanceSnapshotPolicyTest {

    @Test
    fun `enhanced healthy transition permits limited live surface`() {
        val snapshot = resolveTransitionPerformanceSnapshot(
            TransitionPerformanceInputs(
                motionTier = MotionTier.Enhanced,
                systemAnimationsEnabled = true,
                powerSaveMode = false,
                thermalLevel = TransitionThermalLevel.NONE,
                runtimeGuardDowngraded = false,
            )
        )

        assertEquals(TransitionPerformanceTier.FULL, snapshot.tier)
        assertEquals(TransitionReturnOutputMode.LIVE_SURFACE, snapshot.returnOutputMode)
        assertTrue(snapshot.allowNavigationTextureSurface)
        assertFalse(snapshot.force60Hz)
    }

    @Test
    fun `normal motion uses balanced cover takeover by default`() {
        val snapshot = resolveTransitionPerformanceSnapshot(
            TransitionPerformanceInputs(
                motionTier = MotionTier.Normal,
                systemAnimationsEnabled = true,
                powerSaveMode = false,
                thermalLevel = TransitionThermalLevel.NONE,
                runtimeGuardDowngraded = false,
            )
        )

        assertEquals(TransitionPerformanceTier.BALANCED, snapshot.tier)
        assertEquals(TransitionVisualBudget.FROZEN, snapshot.visualBudget)
        assertEquals(TransitionReturnOutputMode.DETACH_AFTER_COVER_READY, snapshot.returnOutputMode)
        assertTrue(snapshot.suppressPlayerOverlays)
        assertFalse(snapshot.allowNavigationTextureSurface)
    }

    @Test
    fun `power save thermal reduced motion and guard all select critical`() {
        listOf(
            TransitionPerformanceInputs(MotionTier.Normal, true, true, TransitionThermalLevel.NONE, false),
            TransitionPerformanceInputs(MotionTier.Normal, true, false, TransitionThermalLevel.MODERATE, false),
            TransitionPerformanceInputs(MotionTier.Reduced, true, false, TransitionThermalLevel.NONE, false),
            TransitionPerformanceInputs(MotionTier.Normal, false, false, TransitionThermalLevel.NONE, false),
            TransitionPerformanceInputs(MotionTier.Normal, true, false, TransitionThermalLevel.NONE, true),
        ).forEach { inputs ->
            val snapshot = resolveTransitionPerformanceSnapshot(inputs)
            assertEquals(TransitionPerformanceTier.CRITICAL, snapshot.tier)
            assertEquals(TransitionVisualBudget.SOLID_TINT, snapshot.visualBudget)
            assertTrue(snapshot.force60Hz)
        }
    }

    @Test
    fun `detach waits for the cover to be ready`() {
        assertFalse(
            shouldDetachVideoOutputAfterCoverReady(
                TransitionReturnOutputMode.DETACH_AFTER_COVER_READY,
                coverReady = false,
            )
        )
        assertTrue(
            shouldDetachVideoOutputAfterCoverReady(
                TransitionReturnOutputMode.DETACH_AFTER_COVER_READY,
                coverReady = true,
            )
        )
    }
}
