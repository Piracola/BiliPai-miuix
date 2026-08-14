package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertNull

class HomeScrollOffsetPolicyTest {

    @Test
    fun retiredLiquidGlassScrollCoupling_neverPublishesGlobalOffset() {
        val next = resolveNextHomeGlobalScrollOffset(
            currentOffset = 120f,
            scrollDeltaY = -8f,
            minUpdateDeltaPx = 0.5f
        )

        assertNull(next)
    }
}
