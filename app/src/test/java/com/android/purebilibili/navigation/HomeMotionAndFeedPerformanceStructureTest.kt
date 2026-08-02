package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeMotionAndFeedPerformanceStructureTest {
    @Test
    fun bottomTabSwitch_usesFadeThroughWithoutAnimatingPagerOffset() {
        val source = sourceFile("navigation/MainBottomPagerState.kt")

        assertTrue(source.contains("var contentVisible by mutableStateOf(true)"))
        assertTrue(source.contains("delay(BOTTOM_PAGER_FADE_OUT_DURATION_MILLIS)"))
        assertTrue(source.contains("pagerState.scrollToPage(safeTargetIndex)"))
        assertTrue(source.contains("delay(BOTTOM_PAGER_FADE_IN_DURATION_MILLIS)"))
        assertTrue(!source.contains("animateScrollToPage("))
    }

    @Test
    fun bottomPager_preloadsOnlyAdjacentPage() {
        val source = sourceFile("navigation/AppNavigation.kt")

        assertTrue(
            source.contains(
                ").coerceAtMost(BOTTOM_PAGER_MAX_PRELOAD_DISTANCE)"
            )
        )
    }

    @Test
    fun homeFeed_compactStatsFollowTheAppearanceSettingWithoutInfoBadgeHaze() {
        val source = sourceFile("feature/home/HomeScreen.kt")

        assertTrue(source.contains("compactStatsOnCover = homeSettings.compactVideoStatsOnCover"))
        assertTrue(source.contains("showInfoGlassBadges = false"))
    }

    @Test
    fun homeFeed_mountsVideoSharedBoundsWhenTransitionIsEnabled() {
        val source = sourceFile("feature/home/HomeScreen.kt")

        assertTrue(
            source.contains(
                "val cardTransitionEnabled = homePerformanceConfig.cardTransitionEnabled && !systemReduceMotion",
            ),
        )
    }

    @Test
    fun videoMotionSwitch_noLongerControlsGlobalNavigationTiming() {
        val source = sourceFile("navigation/AppNavigation.kt")

        assertTrue(source.contains("remember { resolveAppNavigationMotionSpec() }"))
        assertFalse(source.contains("cardTransitionEnabled"))
        assertFalse(source.contains("val shouldApplyBackground = cardTransitionEnabled &&"))
    }

    private fun sourceFile(relativePath: String): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/$relativePath"),
            File("src/main/java/com/android/purebilibili/$relativePath"),
        ).first { it.exists() }.readText()
    }
}
