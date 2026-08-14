package com.android.purebilibili.feature.search

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.resolveAppTopChromePolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchChromePolicyTest {

    @Test
    fun `single miuix theme uses shared search chrome sizing`() {
        val spec = resolveSearchChromeVisualSpec(
            resolveAppTopChromePolicy()
        )

        assertEquals(ContainerLevel.Field, spec.inputShapeLevel)
        assertEquals(ContainerLevel.Card, spec.actionShapeLevel)
        assertEquals(ContainerLevel.Card, spec.suggestionShapeLevel)
        assertEquals(ContainerLevel.Chip, spec.chipShapeLevel)
        assertEquals(48, spec.clearActionSizeDp)
        assertEquals(48, spec.submitActionSizeDp)
        assertTrue(spec.useFilledSearchAction)
        assertTrue(spec.inputHeightDp >= 40)
    }

    @Test
    fun `global wallpaper makes search top bar protected but translucent`() {
        assertEquals(
            Color.White.copy(alpha = 0.96f),
            resolveSearchTopBarHeaderColor(
                surfaceColor = Color.White,
                backgroundAlpha = 0.96f,
                globalWallpaperVisible = true,
                useHeaderBlur = false
            )
        )
    }

    @Test
    fun `search top bar keeps fallback surface without wallpaper or blur`() {
        assertEquals(
            Color.White.copy(alpha = 0.96f),
            resolveSearchTopBarHeaderColor(
                surfaceColor = Color.White,
                backgroundAlpha = 0.96f,
                globalWallpaperVisible = false,
                useHeaderBlur = false
            )
        )
    }

    @Test
    fun `global wallpaper disables search header blur`() {
        assertFalse(
            shouldUseSearchTopBarHeaderBlur(
                hazeSourceEnabled = true,
                globalWallpaperVisible = true
            )
        )
        assertTrue(
            shouldUseSearchTopBarHeaderBlur(
                hazeSourceEnabled = true,
                globalWallpaperVisible = false
            )
        )
    }
}
