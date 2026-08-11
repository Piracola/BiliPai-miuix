package com.android.purebilibili.navigation3.predictiveback

import kotlin.test.Test
import kotlin.test.assertEquals

class BiliPaiMiuixNavTransitionTest {

    @Test
    fun coveredPageBlurStartsStrongAndClearsWithNavigationDepth() {
        assertEquals(0f, resolveMiuixNavCoveredBlurProgress(-1f))
        assertEquals(0f, resolveMiuixNavCoveredBlurProgress(0f))
        assertEquals(0.0625f, resolveMiuixNavCoveredBlurProgress(0.25f))
        assertEquals(0.25f, resolveMiuixNavCoveredBlurProgress(0.5f))
        assertEquals(1f, resolveMiuixNavCoveredBlurProgress(1f))
        assertEquals(1f, resolveMiuixNavCoveredBlurProgress(2f))
    }
}
