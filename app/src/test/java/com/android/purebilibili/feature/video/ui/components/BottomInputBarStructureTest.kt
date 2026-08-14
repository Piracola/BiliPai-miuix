package com.android.purebilibili.feature.video.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.calculateContrastRatio
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BottomInputBarStructureTest {

    @Test
    fun bottomInputBar_keepsSolidDockedPathWhenLiquidReuseAndBlurAreOff() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/ui/components/BottomInputBar.kt")
            .readText()

        assertTrue(source.contains("MaterialTheme.colorScheme.surface"))
        assertTrue(source.contains("MaterialTheme.colorScheme.surfaceContainerHighest"))
        assertTrue(source.contains("DockedSolidBottomInputBar("))
        assertTrue(!source.contains("surfaceVariant.copy(alpha = 0.65f)"))
        assertTrue(!source.contains("liquidGlassBackground"))
    }

    @Test
    fun floatingLiquidGate_isRetiredAfterLiquidGlassRemoval() {
        assertFalse(shouldUseFloatingLiquidBottomInputBar())
    }

    @Test
    fun frostedCommentBar_followsBottomBarBlurPreferenceWhenLiquidGlassIsOff() {
        assertTrue(
            shouldUseFrostedBottomInputBar(
                bottomBarBlurEnabled = true,
                floatingLiquidGlass = false,
                hasHazeState = true
            )
        )
        assertFalse(
            shouldUseFrostedBottomInputBar(
                bottomBarBlurEnabled = false,
                floatingLiquidGlass = false,
                hasHazeState = true
            )
        )
        assertFalse(
            shouldUseFrostedBottomInputBar(
                bottomBarBlurEnabled = true,
                floatingLiquidGlass = true,
                hasHazeState = true
            )
        )
        assertFalse(
            shouldUseFrostedBottomInputBar(
                bottomBarBlurEnabled = true,
                floatingLiquidGlass = false,
                hasHazeState = false
            )
        )
    }

    @Test
    fun frostedCommentBar_reusesHomeBottomBarBlurPipeline() {
        val source = File("src/main/java/com/android/purebilibili/feature/video/ui/components/BottomInputBar.kt")
            .readText()

        assertTrue(source.contains("resolveBottomBarSurfaceColor("))
        assertTrue(source.contains("Modifier.unifiedBlur("))
        assertTrue(source.contains("surfaceType = BlurSurfaceType.BOTTOM_BAR"))
        assertFalse(source.contains("HazeMaterials.ultraThin()"))
    }

    @Test
    fun contentBottomPadding_growsWhenFloatingLiquidChrome() {
        assertEquals(
            112.dp,
            resolveBottomInputBarContentBottomPadding(
                showBar = true,
                floatingLiquidGlass = true,
                showActionButtonsFallback = true
            )
        )
        assertEquals(
            96.dp,
            resolveBottomInputBarContentBottomPadding(
                showBar = true,
                floatingLiquidGlass = false,
                showActionButtonsFallback = true
            )
        )
        assertEquals(
            84.dp,
            resolveBottomInputBarContentBottomPadding(
                showBar = false,
                floatingLiquidGlass = true,
                showActionButtonsFallback = true
            )
        )
        assertEquals(
            12.dp,
            resolveBottomInputBarContentBottomPadding(
                showBar = false,
                floatingLiquidGlass = false,
                showActionButtonsFallback = false
            )
        )
    }

    @Test
    fun bottomInputBarPlaceholderTextKeepsReadableContrastInLightTheme() {
        val inputContainerColor = Color(0xFFE6E0E9)
        val textColor = resolveBottomInputBarPlaceholderTextColor(
            inputContainerColor = inputContainerColor,
            onSurfaceColor = Color(0xFF1D1B20),
            onSurfaceVariantColor = Color(0xFF49454F)
        )

        assertTrue(
            calculateContrastRatio(textColor, inputContainerColor) >=
                BOTTOM_INPUT_BAR_PLACEHOLDER_MIN_CONTRAST
        )
    }

    @Test
    fun bottomInputBarPlaceholderTextKeepsReadableContrastInDarkTheme() {
        val inputContainerColor = Color(0xFF36343B)
        val textColor = resolveBottomInputBarPlaceholderTextColor(
            inputContainerColor = inputContainerColor,
            onSurfaceColor = Color(0xFFE6E1E5),
            onSurfaceVariantColor = Color(0xFFCAC4D0)
        )

        assertTrue(
            calculateContrastRatio(textColor, inputContainerColor) >=
                BOTTOM_INPUT_BAR_PLACEHOLDER_MIN_CONTRAST
        )
    }
}
