package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveSideNavigationRailPolicyTest {

    @Test
    fun singleMiuixStyleUsesMiuixRailRenderer() {
        assertEquals(
            AdaptiveSideNavigationRailRenderer.MIUIX,
            resolveAdaptiveSideNavigationRailRenderer(),
        )
    }

    @Test
    fun expandableOnlyWhenExpandedWidthClass() {
        assertTrue(shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass = true))
        assertFalse(shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass = false))
    }
}
