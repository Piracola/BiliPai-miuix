package com.android.purebilibili.core.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AdaptiveListVisualCapabilitiesTest {

    @Test
    fun `single miuix capabilities use native action affordance and dense metrics`() {
        val capabilities = resolveAdaptiveListVisualCapabilities()

        assertFalse(capabilities.showExplicitActionChevron)
        assertEquals(38, capabilities.componentSpec.iconContainerSizeDp)
        assertEquals(16, capabilities.rowSpec.insideHorizontalPaddingDp)
        assertEquals(48, capabilities.rowSpec.minTouchTargetHeightDp)
    }
}
