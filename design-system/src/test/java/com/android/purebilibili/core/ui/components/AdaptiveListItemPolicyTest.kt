package com.android.purebilibili.core.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveListItemPolicyTest {

    @Test
    fun `miuix clickable item with chevron routes to arrow preference`() {
        assertEquals(
            AppClickableItemRenderer.MIUIX_ARROW,
            resolveAppClickableItemRenderer(
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
        assertTrue(
            shouldRouteClickableItemToMiuixArrowPreference(
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
    }

    @Test
    fun `miuix clickable item without chevron routes to basic component`() {
        assertEquals(
            AppClickableItemRenderer.MIUIX_BASIC,
            resolveAppClickableItemRenderer(
                onClick = {},
                showChevron = false,
                centered = false
            )
        )
        assertFalse(
            shouldRouteClickableItemToMiuixArrowPreference(
                onClick = {},
                showChevron = false,
                centered = false
            )
        )
    }

    @Test
    fun `centered clickable item keeps legacy cupertino row renderer`() {
        assertEquals(
            AppClickableItemRenderer.CUPERTINO,
            resolveAppClickableItemRenderer(
                onClick = {},
                showChevron = true,
                centered = true
            )
        )
    }

    @Test
    fun `miuix switch item routes to switch preference`() {
        assertTrue(
            shouldRouteSwitchItemToMiuixSwitchPreference()
        )
    }

    @Test
    fun `miuix slider preference routes to official slider preference`() {
        assertTrue(
            shouldRouteSliderPreferenceToMiuixSliderPreference()
        )
    }
}
