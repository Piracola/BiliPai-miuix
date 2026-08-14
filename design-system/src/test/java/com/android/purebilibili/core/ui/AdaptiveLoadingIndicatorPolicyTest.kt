package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLoadingIndicatorPolicyTest {

    @Test
    fun `miuix page uses infinite orbit indicator`() {
        assertEquals(
            AdaptiveLoadingVisual.MIUIX_INFINITE,
            resolveAdaptiveLoadingVisual(
                density = AdaptiveLoadingDensity.PAGE,
            ),
        )
    }

    @Test
    fun `miuix compact uses circular progress`() {
        assertEquals(
            AdaptiveLoadingVisual.MIUIX_CIRCULAR,
            resolveAdaptiveLoadingVisual(
                density = AdaptiveLoadingDensity.COMPACT,
            ),
        )
    }

    @Test
    fun `size heuristic maps compact threshold`() {
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(null))
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(80f))
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(33f))
        assertEquals(AdaptiveLoadingDensity.COMPACT, resolveAdaptiveLoadingDensity(32f))
        assertEquals(AdaptiveLoadingDensity.COMPACT, resolveAdaptiveLoadingDensity(24f))
    }

}
