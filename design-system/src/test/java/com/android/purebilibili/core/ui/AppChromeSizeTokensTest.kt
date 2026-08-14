package com.android.purebilibili.core.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class AppChromeSizeTokensTest {

    @Test
    fun `icon actions keep a 48dp outer touch target`() {
        assertEquals(48.dp, AppChromeSizeTokens.MinimumTouchTarget)
    }

    @Test
    fun `single miuix compact capsule tokens keep denser search chrome within material touch bounds`() {
        val spec = resolveCompactCapsuleChromeSpec()

        assertEquals(48, spec.primaryHeightDp)
        assertEquals(48, spec.secondaryButtonSizeDp)
        // 48 * 0.3 = 14; never full pill (20)
        assertEquals(14, spec.primaryCornerRadiusDp)
        assertEquals(14, spec.inputHorizontalPaddingDp)
        assertEquals(8, spec.standardGapDp)
    }
}
