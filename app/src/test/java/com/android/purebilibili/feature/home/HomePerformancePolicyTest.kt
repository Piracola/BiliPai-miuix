package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HomePerformancePolicyTest {

    @Test
    fun keepsHomeVisualSettingsWhenDataSaverOff() {
        val config = resolveHomePerformanceConfig(
            headerBlurEnabled = true,
            bottomBarBlurEnabled = false,
            cardAnimationEnabled = false,
            cardTransitionEnabled = true,
            isDataSaverActive = false,
            smartVisualGuardEnabled = false,
            normalPreloadAheadCount = 5
        )

        assertTrue(config.headerBlurEnabled)
        assertFalse(config.bottomBarBlurEnabled)
        assertFalse(config.isAnyLiquidGlassEnabled)
        assertFalse(config.cardAnimationEnabled)
        assertTrue(config.cardTransitionEnabled)
        assertFalse(config.isDataSaverActive)
        assertEquals(2, config.preloadAheadCount)
    }

    @Test
    fun dataSaverDisablesPreloadAhead() {
        val config = resolveHomePerformanceConfig(
            headerBlurEnabled = true,
            bottomBarBlurEnabled = true,
            cardAnimationEnabled = true,
            cardTransitionEnabled = true,
            isDataSaverActive = true,
            smartVisualGuardEnabled = false,
            normalPreloadAheadCount = 5
        )

        assertTrue(config.isDataSaverActive)
        assertTrue(config.preloadAheadCount == 0)
    }

    @Test
    fun smartGuardFlag_noLongerAffectsHomePerformanceConfig() {
        val config = resolveHomePerformanceConfig(
            headerBlurEnabled = true,
            bottomBarBlurEnabled = true,
            cardAnimationEnabled = true,
            cardTransitionEnabled = true,
            isDataSaverActive = false,
            smartVisualGuardEnabled = true,
            normalPreloadAheadCount = 5
        )

        assertFalse(config.isDataSaverActive)
        assertFalse(config.isAnyLiquidGlassEnabled)
        assertEquals(2, config.preloadAheadCount)
    }

    @Test
    fun normalMode_capsPreloadAheadToConservativeBudget() {
        val config = resolveHomePerformanceConfig(
            headerBlurEnabled = true,
            bottomBarBlurEnabled = true,
            cardAnimationEnabled = true,
            cardTransitionEnabled = true,
            isDataSaverActive = false,
            smartVisualGuardEnabled = false,
            normalPreloadAheadCount = 5
        )

        assertEquals(2, config.preloadAheadCount)
    }

    @Test
    fun coverPreloadRange_waitsUntilFeedScrollSettles() {
        assertNull(
            resolveHomeCoverPreloadRange(
                isDataSaverActive = false,
                isScrollInProgress = true,
                lastVisibleIndex = 8,
                totalItemCount = 20,
                preloadAheadCount = 2
            )
        )
    }

    @Test
    fun coverPreloadRange_usesConservativeWindowAfterSettledScroll() {
        assertEquals(
            9 until 11,
            resolveHomeCoverPreloadRange(
                isDataSaverActive = false,
                isScrollInProgress = false,
                lastVisibleIndex = 8,
                totalItemCount = 20,
                preloadAheadCount = 4
            )
        )
    }

    @Test
    fun coverPreloadRange_disablesWhenDataSaverActive() {
        assertNull(
            resolveHomeCoverPreloadRange(
                isDataSaverActive = true,
                isScrollInProgress = false,
                lastVisibleIndex = 8,
                totalItemCount = 20,
                preloadAheadCount = 2
            )
        )
    }
}
