package com.android.purebilibili.feature.list

import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import com.android.purebilibili.core.ui.AppTopChromePolicy
import com.android.purebilibili.core.ui.AppTopTabPresentation
import com.android.purebilibili.core.ui.CompactCapsuleChromeSpec

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryFilterTabChromePolicyTest {
    @Test
    fun liquidDock_isRetiredAfterLiquidGlassRemoval() {
        val spec = resolveHistoryFilterTabChromeSpec(
            homeSettings = HomeSettings(),
            topChromePolicy = testTopChromePolicy(),
        )

        assertFalse(spec.useLiquidDock)
        assertFalse(spec.dragSelectionEnabled)
    }

    @Test
    fun itemWidth_scalesWithFilterCount() {
        assertEquals(56, resolveHistoryFilterTabItemWidthDp(filterCount = 5))
        assertEquals(60, resolveHistoryFilterTabItemWidthDp(filterCount = 4))
        assertEquals(66, resolveHistoryFilterTabItemWidthDp(filterCount = 3))
    }

    private fun testTopChromePolicy() = AppTopChromePolicy(
        tabPresentation = AppTopTabPresentation.MOVING_CAPSULE,
        iconFamily = AppSemanticIconFamily.MATERIAL,
        compactChromeSpec = CompactCapsuleChromeSpec(
            primaryHeightDp = 44,
            secondaryButtonSizeDp = 40,
            chipHeightDp = 36,
            compactChipHeightDp = 32,
            primaryCornerRadiusDp = 22,
            secondaryButtonCornerRadiusDp = 20,
            chipCornerRadiusDp = 18,
            compactChipCornerRadiusDp = 16,
            iconSizeDp = 20,
            smallIconSizeDp = 16,
            inputHorizontalPaddingDp = 12,
            chipHorizontalPaddingDp = 12,
            compactChipHorizontalPaddingDp = 10,
            standardGapDp = 8,
        ),
    )
}
