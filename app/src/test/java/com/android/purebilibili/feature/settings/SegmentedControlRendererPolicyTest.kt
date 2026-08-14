package com.android.purebilibili.feature.settings

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.components.AppSegmentedControlColors
import com.android.purebilibili.core.ui.components.AppSegmentedRenderer
import com.android.purebilibili.core.ui.components.resolveAppMiuixSegmentedColors
import com.android.purebilibili.core.ui.components.resolveAppSegmentedRenderer
import org.junit.Assert.assertEquals
import org.junit.Test

class SegmentedControlRendererPolicyTest {

    @Test
    fun `single miuix renderer routes segmented chrome to tab row`() {
        assertEquals(
            AppSegmentedRenderer.MIUIX,
            resolveAppSegmentedRenderer()
        )
    }

    @Test
    fun `miuix segmented tab row colors map secondary container roles`() {
        val tokens = AppSegmentedControlColors(
            outerContainerColor = Color(0xFF302322),
            activeContainerColor = Color(0xFF7A4828),
            activeContentColor = Color(0xFFFFE0D1),
            inactiveContentColor = Color(0xFFEAD0CD)
        )
        val colors = resolveAppMiuixSegmentedColors(tokens)

        assertEquals(Color.Transparent, colors.backgroundColor)
        assertEquals(Color(0xFF7A4828), colors.selectedBackgroundColor)
        assertEquals(Color(0xFFFFE0D1), colors.selectedContentColor)
        assertEquals(Color(0xFFEAD0CD), colors.contentColor)
    }
}
