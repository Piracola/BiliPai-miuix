package com.android.purebilibili.feature.list

import java.io.File
import com.android.purebilibili.core.store.HomeHeaderBlurMode
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import com.android.purebilibili.core.ui.AppTopChromePolicy
import com.android.purebilibili.core.ui.AppTopTabPresentation
import com.android.purebilibili.core.ui.CompactCapsuleChromeSpec
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonListAppearancePolicyTest {

    @Test
    fun commonListGridWidth_preservesPhoneDensityAndTabletReadability() {
        assertEquals(170.dp, resolveCommonListGridMinColumnWidth(isExpandedScreen = false))
        assertEquals(240.dp, resolveCommonListGridMinColumnWidth(isExpandedScreen = true))
    }

    @Test
    fun favoritePreviewAndProgressBadgeWidths_areOwnedByLayoutPolicy() {
        assertEquals(112.dp, resolveFavoriteSubscribedFolderPreviewWidth())
        assertEquals(
            FavoriteProgressBadgeWidthSpec(minWidth = 104.dp, maxWidth = 150.dp),
            resolveFavoriteProgressBadgeWidthSpec(),
        )
    }

    @Test
    fun followPreset_keepsHeaderBlurForCommonList() {
        val enabled = resolveCommonListHeaderBlurEnabled(
            homeSettings = HomeSettings(
                headerBlurMode = HomeHeaderBlurMode.FOLLOW_PRESET
            ),
        )

        assertTrue(enabled)
    }

    @Test
    fun alwaysOff_disablesHeaderBlurForCommonList() {
        val enabled = resolveCommonListHeaderBlurEnabled(
            homeSettings = HomeSettings(
                headerBlurMode = HomeHeaderBlurMode.ALWAYS_OFF
            ),
        )

        assertFalse(enabled)
    }

    @Test
    fun commonListVideoCardAppearance_followsHomeChromeToggles() {
        val appearance = resolveCommonListVideoCardAppearance(
            homeSettings = HomeSettings(
                headerBlurMode = HomeHeaderBlurMode.FOLLOW_PRESET,
                isBottomBarBlurEnabled = false
            ),
            liquidGlassEnabled = false,
        )

        assertFalse(appearance.glassEnabled)
        assertTrue(appearance.blurEnabled)
        assertFalse(appearance.showCoverGlassBadges)
        assertFalse(appearance.showInfoGlassBadges)
    }

    @Test
    fun commonListHeaderLocalBlur_isDisabledWhenGlobalWallpaperIsVisible() {
        assertFalse(
            shouldUseCommonListHeaderLocalBlur(
                headerBlurEnabled = true,
                globalWallpaperVisible = true
            )
        )
    }

    @Test
    fun commonListHeaderLocalBlur_remainsEnabledWithoutGlobalWallpaper() {
        assertTrue(
            shouldUseCommonListHeaderLocalBlur(
                headerBlurEnabled = true,
                globalWallpaperVisible = false
            )
        )
    }

    @Test
    fun commonListViewportTopPadding_keepsScrollableContentBelowHeader() {
        assertEquals(148.dp, resolveCommonListViewportTopPadding(148.dp))
        assertEquals(0.dp, resolveCommonListViewportTopPadding((-12).dp))
    }

    @Test
    fun commonListTopBar_usesHeaderBlurBudget() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        ).first { it.exists() }.readText()

        assertTrue(source.contains("surfaceType = BlurSurfaceType.HEADER"))
    }

    @Test
    fun historyAndFavoriteHeaderCollapse_usesScrollableInsetAndThemeColors() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        ).first { it.exists() }.readText()

        assertTrue(source.contains("historyViewModel != null || favoriteViewModel != null"))
        assertTrue(source.contains("scrollUnderHeader = commonListHeaderCollapseEnabled"))
        assertTrue(source.contains("selectedContainerColor = MaterialTheme.colorScheme.primaryContainer"))
        assertTrue(source.contains("selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer"))
    }

    @Test
    fun movingCapsuleFavoriteHeaderLayout_prefersCompactSearchAndChips() {
        val layout = resolveCommonListFavoriteHeaderLayout(
            topChromePolicy = testTopChromePolicy(
                presentation = AppTopTabPresentation.MOVING_CAPSULE,
                primaryHeightDp = 44,
                chipHeightDp = 36,
                compactChipHeightDp = 32,
            ),
        )

        assertEquals(44, layout.searchBarHeightDp)
        assertEquals(44, layout.browseToggleHeightDp)
        assertEquals(30, layout.browseToggleIndicatorHeightDp)
        assertEquals(14, layout.browseToggleLabelFontSizeSp)
        assertEquals(32, layout.folderChipMinHeightDp)
        assertTrue(layout.headerBackgroundAlphaMultiplier < 1f)
    }

    @Test
    fun materialUnderlineFavoriteHeaderLayout_staysCompactWithoutBecomingTiny() {
        val layout = resolveCommonListFavoriteHeaderLayout(
            topChromePolicy = testTopChromePolicy(
                presentation = AppTopTabPresentation.MATERIAL_UNDERLINE,
                primaryHeightDp = 56,
                chipHeightDp = 32,
                compactChipHeightDp = 28,
            ),
        )

        assertEquals(56, layout.searchBarHeightDp)
        assertEquals(30, layout.browseToggleIndicatorHeightDp)
        assertEquals(14, layout.browseToggleLabelFontSizeSp)
        assertEquals(32, layout.folderChipMinHeightDp)
        assertTrue(layout.headerBackgroundAlphaMultiplier < 1f)
    }

    private fun testTopChromePolicy(
        presentation: AppTopTabPresentation,
        primaryHeightDp: Int,
        chipHeightDp: Int,
        compactChipHeightDp: Int,
    ) = AppTopChromePolicy(
        tabPresentation = presentation,
        iconFamily = AppSemanticIconFamily.MATERIAL,
        compactChromeSpec = CompactCapsuleChromeSpec(
            primaryHeightDp = primaryHeightDp,
            secondaryButtonSizeDp = 48,
            chipHeightDp = chipHeightDp,
            compactChipHeightDp = compactChipHeightDp,
            primaryCornerRadiusDp = primaryHeightDp / 2,
            secondaryButtonCornerRadiusDp = 24,
            chipCornerRadiusDp = chipHeightDp / 2,
            compactChipCornerRadiusDp = compactChipHeightDp / 2,
            iconSizeDp = 20,
            smallIconSizeDp = 16,
            inputHorizontalPaddingDp = 12,
            chipHorizontalPaddingDp = 12,
            compactChipHorizontalPaddingDp = 10,
            standardGapDp = 8,
        ),
    )

}
