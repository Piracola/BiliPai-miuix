package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeMotionAndFeedPerformanceStructureTest {
    @Test
    fun bottomTabSwitch_usesUserInputPagerMutation() {
        val source = sourceFile("navigation/MainBottomPagerState.kt")

        assertTrue(source.contains("pagerState.scroll(MutatePriority.UserInput)"))
        assertTrue(source.contains("scrollBy(currentValue - previousValue)"))
        assertTrue(source.contains("easing = EaseInOut"))
        assertTrue(source.contains("resolveBottomPagerNavigationDurationMillis("))
        assertFalse(source.contains("pagerState.animateScrollBy("))
        assertTrue(!source.contains("dispatchRawDelta"))
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
    fun homeFeed_doesNotMountDissolveLayoutTrackingUntilRequested() {
        val source = sourceFile("feature/home/HomeCategoryPage.kt")

        assertTrue(source.contains("MaybeDissolvableVideoCard("))
        assertTrue(source.contains("preserveContentLayerWhenIdle = cardTransitionEnabled"))
        assertFalse(Regex("(?m)^\\s*DissolvableVideoCard\\(").containsMatchIn(source))
    }


    private fun sourceFile(relativePath: String): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/$relativePath"),
            File("src/main/java/com/android/purebilibili/$relativePath"),
        ).first { it.exists() }.readText()
    }
}
