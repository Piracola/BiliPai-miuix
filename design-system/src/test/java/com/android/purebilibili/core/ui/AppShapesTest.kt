package com.android.purebilibili.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AppShapesTest {

    @Test
    fun pillRadius_singleMiuixTheme_is22Dp() {
        val dp = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Pill,
        )
        assertEquals(22.dp, dp)
    }

    @Test
    fun cardRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Card,
        )
        assertTrue(radius.value in 13.79f..13.81f, "actual ${radius.value}")
    }

    @Test
    fun dialogRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Dialog,
        )
        assertEquals(16.1.dp, radius)
    }

    @Test
    fun fieldRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Field,
        )
        assertEquals(11.5.dp, radius)
    }

    @Test
    fun tagRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Tag,
        )
        assertEquals(4.6.dp, radius)
    }

    @Test
    fun chipRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Chip,
        )
        assertTrue(radius.value in 6.89f..6.91f, "actual ${radius.value}")
    }

    @Test
    fun floatingRadius_usesMiuixScale() {
        val radius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Floating,
        )
        assertEquals(32.2.dp, radius)
    }

    @Test
    fun containerShape_usesRoundedCornerShapeForSingleTheme() {
        val shape = AppShapes.resolveContainerShape(
            level = ContainerLevel.Card,
        )
        assertIs<RoundedCornerShape>(shape)
    }

    @Test
    fun sheetContainerShape_isTopRounded() {
        val shape = AppShapes.resolveContainerShape(
            level = ContainerLevel.Sheet,
        ) as RoundedCornerShape
        // Miuix Sheet = 20 * 1.15 = 23，仅顶部圆角。
        assertEquals(RoundedCornerShape(23.dp, 23.dp, 0.dp, 0.dp), shape)
    }

    @Test
    fun borderedContainerShape_usesRoundedCornerShape() {
        val shape = AppShapes.resolveBorderedContainerShape(
            level = ContainerLevel.Dialog,
        )
        assertIs<RoundedCornerShape>(shape)
    }
}
