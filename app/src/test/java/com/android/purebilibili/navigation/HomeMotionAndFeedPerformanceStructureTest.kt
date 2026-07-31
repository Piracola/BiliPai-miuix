package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeMotionAndFeedPerformanceStructureTest {
    @Test
    fun bottomTabSwitch_animatesPagerOffset() {
        val source = sourceFile("navigation/MainBottomPagerState.kt")

        assertTrue(source.contains("private suspend fun animatePageChange("))
        assertTrue(source.contains("animateScrollToPage("))
        assertTrue(!source.contains("AnimationState(initialValue = 0f).animateTo("))
        assertTrue(!source.contains("scrollScope.scrollBy(value - previousValue)"))
        assertTrue(source.contains(".coerceAtMost(BOTTOM_PAGER_ANIMATED_SCROLL_MAX_MILLIS)"))
        assertTrue(source.contains("easing = LinearOutSlowInEasing"))
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
    fun homeFeed_externalStatsAvoidPerBadgeHaze() {
        val source = sourceFile("feature/home/HomeScreen.kt")

        assertTrue(source.contains("compactStatsOnCover = false"))
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
    fun videoMotionSwitch_doesNotChangeGlobalNavigationTiming() {
        val source = sourceFile("navigation/AppNavigation.kt")

        assertTrue(source.contains("remember(isTabletLayout, cardTransitionEnabled)"))
        assertTrue(source.contains("cardTransitionEnabled = cardTransitionEnabled"))
        assertFalse(source.contains("val shouldApplyBackground = cardTransitionEnabled &&"))
    }

    private fun sourceFile(relativePath: String): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/$relativePath"),
            File("src/main/java/com/android/purebilibili/$relativePath"),
        ).first { it.exists() }.readText()
    }
}
