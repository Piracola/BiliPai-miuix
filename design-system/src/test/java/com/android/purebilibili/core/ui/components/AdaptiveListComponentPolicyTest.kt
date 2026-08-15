package com.android.purebilibili.core.ui.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.core.theme.iOSRed
import com.android.purebilibili.core.theme.iOSSystemGray
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveListComponentPolicyTest {

    @Test
    fun `single miuix style softens grouped settings geometry`() {
        val spec = resolveAdaptiveListComponentVisualSpec()

        assertEquals(48, spec.searchBarHeightDp)
        assertEquals(14, spec.searchBarCornerRadiusDp)
        assertEquals(20, spec.groupCornerRadiusDp)
        assertEquals(16, spec.sectionStartPaddingDp)
        assertEquals(16, spec.dividerStartIndentDp)
        assertEquals(10, spec.iconCornerRadiusDp)
        assertEquals(38, spec.iconContainerSizeDp)
        assertEquals(20, spec.iconGlyphSizeDp)
        assertEquals(0.18f, spec.iconBackgroundAlpha, 0.0001f)
        assertEquals(0f, spec.dividerThicknessDp, 0.0001f)
        assertEquals(0, spec.groupTonalElevationDp)
    }

    @Test
    fun `single miuix style uses denser list row spacing`() {
        val spec = resolveAdaptiveListRowVisualSpec()

        assertEquals(16, spec.insideHorizontalPaddingDp)
        assertEquals(14, spec.insideVerticalPaddingDp)
        assertEquals(14, spec.trailingIconSizeDp)
        assertEquals(6, spec.trailingSpacingDp)
        assertEquals(48, spec.minTouchTargetHeightDp)
    }

    @Test
    fun `single miuix style preserves colorful settings icon tints`() {
        val colorScheme = darkColorScheme()

        assertEquals(
            iOSPurple,
            resolveAdaptiveSemanticIconTint(
                iconTint = iOSPurple,
            ),
        )
        assertEquals(
            iOSRed,
            resolveAdaptiveSemanticIconTint(
                iconTint = iOSRed,
            ),
        )
        assertEquals(
            iOSSystemGray,
            resolveAdaptiveSemanticIconTint(
                iconTint = iOSSystemGray,
            ),
        )
    }

    @Test
    fun `filled settings icons should use opaque containers and contrasting glyphs`() {
        val colorScheme = lightColorScheme(primary = Color(0xFF3366CC))

        assertEquals(
            1f,
            resolveAdaptivePreferenceIconBackgroundAlpha(
                treatment = AppPreferenceIconTreatment.FILLED,
                tonalAlpha = 0.14f,
            ),
        )
        assertEquals(
            colorScheme.onPrimary,
            resolveAdaptivePreferenceIconContentColor(colorScheme.primary, colorScheme),
        )
        assertEquals(
            Color.White,
            resolveAdaptivePreferenceIconContentColor(iOSBlue, colorScheme),
        )
        assertEquals(
            colorScheme.secondary,
            resolveAdaptivePreferenceIconContainerColor(
                iconTint = iOSBlue,
                semanticTint = colorScheme.secondary,
                treatment = AppPreferenceIconTreatment.FILLED,
            ),
        )
    }

    @Test
    fun `single miuix style uses denser shared container tones`() {
        val colorScheme = lightColorScheme(
            surfaceContainer = Color(0xFFF0EBF4),
            surfaceContainerLow = Color(0xFFF4F0F8),
            surfaceContainerHigh = Color(0xFFECE6F0)
        )

        assertEquals(
            colorScheme.surfaceContainer,
            resolveAdaptiveGroupContainerColor(
                colorScheme = colorScheme,
            )
        )
        assertEquals(
            colorScheme.surfaceContainer,
            resolveAdaptiveSearchBarContainerColor(
                colorScheme = colorScheme,
            )
        )
    }

    @Test
    fun `single miuix style routes search to miuix field`() {
        assertTrue(
            shouldUseNativeMiuixSearchBar()
        )
    }

    @Test
    fun `global wallpaper should make default grouped settings translucent`() {
        val colorScheme = lightColorScheme(
            surfaceContainer = Color(0xFFF0EBF4),
            surfaceContainerLow = Color(0xFFF4F0F8),
            surfaceContainerHigh = Color(0xFFECE6F0)
        )

        assertEquals(
            colorScheme.surfaceContainer.copy(alpha = 0.62f),
            resolveAdaptiveGroupContainerColor(
                colorScheme = colorScheme,
                globalWallpaperVisible = true
            )
        )
        assertEquals(
            colorScheme.surfaceContainer.copy(alpha = 0.48f),
            resolveAdaptiveSearchBarContainerColor(
                colorScheme = colorScheme,
                globalWallpaperVisible = true
            )
        )
    }

    @Test
    fun `miuix search bar implementation uses official input field`() {
        val source = java.io.File("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
            .takeIf { it.exists() }
            ?: java.io.File("src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
        val text = source.readText()
        val miuixSearchBarStart = text.indexOf("private fun MiuixAdaptiveSearchBar")
        assertTrue(miuixSearchBarStart >= 0)
        val miuixSearchBarEnd = text.indexOf("\n}", miuixSearchBarStart).let { if (it < 0) text.length else it + 2 }
        val miuixSearchBarBlock = text.substring(miuixSearchBarStart, miuixSearchBarEnd)
        assertTrue(miuixSearchBarBlock.contains("InputField("))
        assertFalse(miuixSearchBarBlock.contains("BasicTextField("))
    }

    @Test
    fun `force expanded search bar uses expanded miuix input field`() {
        val source = listOf(
            java.io.File("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt"),
            java.io.File("src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt"),
        ).first { it.exists() }.readText()
        val forceExpandedStart = source.indexOf("if (forceExpandedInput) {")
        val inputFieldStart = source.indexOf("InputField(", forceExpandedStart)
        assertTrue(forceExpandedStart >= 0)
        assertTrue(inputFieldStart > forceExpandedStart)
        assertTrue(source.substring(forceExpandedStart, inputFieldStart).contains("focusRequester"))
        assertTrue(source.contains("expanded = true"))
    }

    @Test
    fun `settings search screen pins input in scaffold header`() {
        val source = listOf(
            java.io.File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchScreen.kt"),
            java.io.File("../app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchScreen.kt"),
            java.io.File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchScreen.kt"),
        ).first { it.exists() }.readText()
        assertTrue(source.contains("scrollHost = SettingsPageScrollHost.External"))
        assertTrue(source.contains("header = {"))
        assertTrue(source.contains("SettingsSearchBarSection("))
        val headerIndex = source.indexOf("header = {")
        val scrollIndex = source.indexOf(".verticalScroll(", headerIndex)
        assertTrue(headerIndex >= 0)
        assertTrue(scrollIndex > headerIndex)
    }

    @Test
    fun `settings search bar delegates style rendering to neutral field`() {
        val source = listOf(
            java.io.File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchUi.kt"),
            java.io.File("../app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchUi.kt"),
            java.io.File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsSearchUi.kt"),
        ).first { it.exists() }.readText()
        assertTrue(source.contains("fun SettingsSearchBarSection"))
        assertTrue(source.contains("AppSearchField("))
        assertFalse(source.contains("BasicTextField("))
        assertFalse(source.contains("AdaptiveSearchFieldRenderer("))
    }

    @Test
    fun `settings search bar uses expanded miuix input field`() {
        val neutralApiSource = listOf(
            java.io.File("design-system/src/main/java/com/android/purebilibili/core/ui/components/AppPreferenceComponents.kt"),
            java.io.File("src/main/java/com/android/purebilibili/core/ui/components/AppPreferenceComponents.kt"),
        ).first { it.exists() }.readText()
        val rendererSource = listOf(
            java.io.File("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt"),
            java.io.File("src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt"),
        ).first { it.exists() }.readText()
        assertTrue(neutralApiSource.contains("fun AppSearchField("))
        assertTrue(
            neutralApiSource.contains(
                "forceExpandedInput = presentation == AppSearchFieldPresentation.TOP_BAR"
            )
        )
        assertTrue(rendererSource.contains("InputField("))
        assertTrue(rendererSource.contains("expanded = true"))
        assertTrue(rendererSource.contains("forceExpandedInput"))
    }

    @Test
    fun `miuix generic search bar does not auto expand before user interaction`() {
        val source = java.io.File("design-system/src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
            .takeIf { it.exists() }
            ?: java.io.File("src/main/java/com/android/purebilibili/core/ui/components/AdaptivePreferenceComponents.kt")
        val text = source.readText()
        val miuixSearchBarStart = text.indexOf("private fun MiuixAdaptiveSearchBar")
        assertTrue(miuixSearchBarStart >= 0)
        val collapsedPathStart = text.indexOf("var expanded by rememberSaveable(query.isNotBlank())", miuixSearchBarStart)
        assertTrue(collapsedPathStart >= 0)
        val collapsedPathEnd = text.indexOf("InputField(", collapsedPathStart)
        assertTrue(collapsedPathEnd > collapsedPathStart)
        val collapsedPathBlock = text.substring(collapsedPathStart, collapsedPathEnd)

        assertTrue(collapsedPathBlock.contains("var expanded by rememberSaveable(query.isNotBlank())"))
        assertTrue(text.substring(collapsedPathStart).contains("expanded = expanded || query.isNotBlank()"))
        assertTrue(text.contains("forceExpandedInput"))
    }
}
