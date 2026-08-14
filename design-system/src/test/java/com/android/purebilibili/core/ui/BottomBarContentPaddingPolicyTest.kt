package com.android.purebilibili.core.ui

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class BottomBarContentPaddingPolicyTest {

    @Test
    fun `floating phone content includes body inset gap and navigation bars`() {
        assertEquals(
            120.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = 24.dp,
                reserveBottomBar = true,
                isBottomBarFloating = true,
                bottomBarLabelMode = 0,
                isTablet = false,
                hasUiSkinDecoration = false,
                extraContentPadding = 8.dp,
            ),
        )
    }

    @Test
    fun `docked tablet content uses docked body without floating inset`() {
        assertEquals(
            108.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = 24.dp,
                reserveBottomBar = true,
                isBottomBarFloating = false,
                bottomBarLabelMode = 2,
                isTablet = true,
                hasUiSkinDecoration = false,
                extraContentPadding = 8.dp,
            ),
        )
    }

    @Test
    fun `side navigation content only keeps system and content padding`() {
        assertEquals(
            32.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = 24.dp,
                reserveBottomBar = false,
                isBottomBarFloating = true,
                bottomBarLabelMode = 0,
                isTablet = true,
                hasUiSkinDecoration = false,
                extraContentPadding = 8.dp,
            ),
        )
    }

    @Test
    fun `negative inputs are clamped`() {
        assertEquals(
            0.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = (-4).dp,
                reserveBottomBar = false,
                isBottomBarFloating = false,
                bottomBarLabelMode = 1,
                isTablet = false,
                hasUiSkinDecoration = false,
                extraContentPadding = (-8).dp,
            ),
        )
    }

    @Test
    fun `floating skin reserves actual decorated shell`() {
        assertEquals(
            144.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = 24.dp,
                reserveBottomBar = true,
                isBottomBarFloating = true,
                bottomBarLabelMode = 0,
                isTablet = false,
                hasUiSkinDecoration = true,
                extraContentPadding = 8.dp,
            ),
        )
    }

    @Test
    fun `docked navigation reserves docked body height`() {
        assertEquals(
            108.dp,
            resolveBottomBarContentPadding(
                navigationBarsBottom = 24.dp,
                reserveBottomBar = true,
                isBottomBarFloating = false,
                bottomBarLabelMode = 0,
                isTablet = false,
                hasUiSkinDecoration = false,
                extraContentPadding = 8.dp,
            ),
        )
    }
}
