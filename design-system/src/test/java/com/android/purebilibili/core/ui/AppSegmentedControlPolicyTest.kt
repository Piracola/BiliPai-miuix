package com.android.purebilibili.core.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSegmentedControlPolicyTest {

    @Test
    fun `single miuix exposes native tab row capability`() {
        val policy = resolveAppSegmentedControlPolicy()

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesNativeTabRow)
        assertFalse(policy.usesMaterialColorTokens)
    }

    @Test
    fun `segmented corners are height-capped below half of tab height`() {
        // Card preferred then capped at 30% of 40dp tab height → max 12dp.
        val policy = resolveAppSegmentedControlPolicy()
        assertEquals(40.dp, policy.nativeTabRowHeight)
        // Miuix card 13.8 → capped to 12.
        assertEquals(12.dp, policy.pillCornerRadius)
        assertTrue(policy.pillCornerRadius < policy.nativeTabRowHeight / 2)
    }

    @Test
    fun `height cap prevents full capsule on 48dp bars`() {
        val preferred = 28.dp
        val capped = resolveHeightCappedCornerRadius(48.dp, preferred)
        assertTrue(capped.value in 14.39f..14.41f, "actual ${capped.value}")
        assertTrue(capped < 24.dp) // half of 48
    }
}
