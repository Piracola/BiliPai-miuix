package com.android.purebilibili.feature.settings

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.components.resolveAppSegmentedControlColors
import kotlin.test.Test
import kotlin.test.assertEquals

class AppearanceMd3SegmentedControlPolicyTest {

    @Test
    fun `segmented control keeps miuix secondary roles`() {
        val tokens = resolveAppSegmentedControlColors(
            miuixSecondaryContainer = Color(0xFF7A4828),
            miuixOnSecondaryContainer = Color(0xFFFFE0D1),
            miuixSurfaceContainerHigh = Color(0xFF302322),
            miuixOnSurfaceVariantSummary = Color(0xFFEAD0CD)
        )

        assertEquals(Color(0xFF302322), tokens.outerContainerColor)
        assertEquals(Color(0xFF7A4828), tokens.activeContainerColor)
        assertEquals(Color(0xFFFFE0D1), tokens.activeContentColor)
        assertEquals(Color(0xFFEAD0CD), tokens.inactiveContentColor)
    }
}
