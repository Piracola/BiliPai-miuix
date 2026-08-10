package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeHeroFlyoutStructureTest {

    @Test
    fun homeScreenNavigatesImmediatelyAndDoesNotRunSourceFlyout() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val clickWrapperSource = source
            .substringAfter("val wrappedOnVideoClick")
            .substringBefore("val onTodayWatchVideoClick")

        assertFalse(source.contains("pendingHeroFlyoutRequest"))
        assertFalse(source.contains("shouldRunHomeHeroFlyoutBeforeNavigation(request)"))
        assertFalse(source.contains("resolveHomeHeroFlyoutNavigationDelayMillis()"))
        assertTrue(clickWrapperSource.contains("hideTopTabsForForwardDetailNav = true"))
        assertTrue(clickWrapperSource.contains("setBottomBarVisible(false)"))
        assertTrue(clickWrapperSource.contains("isVideoNavigating = true"))
        assertTrue(clickWrapperSource.contains("onVideoClick(request)"))
    }

    @Test
    fun homeTopTabsReturnRecoveryFollowsNavigationStateInsteadOfLifecycleStart() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val navigationReturnEffect = source
            .substringAfter("Navigation 返回不一定触发首页 Lifecycle.ON_START")
            .substringBefore("从详情页返回时延后清理")
        val lifecycleObserverSource = source
            .substringAfter("DisposableEffect(lifecycleOwner, useSideNavigation)")
            .substringBefore("lifecycleOwner.lifecycle.addObserver(observer)")

        assertTrue(navigationReturnEffect.contains("LaunchedEffect(isReturningFromVideoDetail"))
        assertTrue(navigationReturnEffect.contains("hideTopTabsForForwardDetailNav = false"))
        assertTrue(navigationReturnEffect.contains("resolveHomeTopTabsRevealDelayMs("))
        assertFalse(lifecycleObserverSource.contains("val returningFromDetail = isReturningFromVideoDetail"))
        assertFalse(lifecycleObserverSource.contains("resolveHomeTopTabsRevealDelayMs("))
    }

    @Test
    fun ordinaryHomeVideoCardDoesNotRunSourceFlyout() {
        val pageSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val cardSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt")

        assertFalse(pageSource.contains("heroFlyoutBvid"))
        assertFalse(pageSource.contains("heroFlyoutActive"))
        assertFalse(cardSource.contains("heroFlyoutActive"))
        assertFalse(cardSource.contains("resolveHomeHeroFlyoutFrame("))
    }

    @Test
    fun ordinaryHomeVideoCardUsesCoverFirstSharedTransitionPolicy() {
        val cardSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt")

        assertTrue(cardSource.contains("resolveVideoCardSharedTransitionMotionSpec("))
        assertTrue(cardSource.contains("resolveVideoSharedTransitionVisualSpec("))
        assertTrue(cardSource.contains("videoCardShellSharedBoundsOrEmpty("))
        assertTrue(cardSource.contains("sharedElementSourceRoute"))
        assertFalse(cardSource.contains("使用 renderInSharedTransitionScopeOverlayOption 控制可见性"))
    }

    @Test
    fun homeHeroCarouselUsesWholeCardShellSharedTransition() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/HomeHeroCarousel.kt")

        assertTrue(source.contains("videoCardShellSharedBoundsOrEmpty("))
        assertTrue(source.contains("resolveVideoCardSharedTransitionMotionSpec("))
        assertTrue(source.contains("LocalSharedTransitionEnabled.current"))
        assertTrue(source.contains("sourceCornerDp = cardCornerDp.value.roundToInt()"))
        assertFalse(source.contains("videoCoverSharedElementKey("))
        assertFalse(source.contains("videoViewsSharedElementKey("))
        assertFalse(source.contains("videoDanmakuSharedElementKey("))
        assertFalse(source.contains("videoDurationSharedElementKey("))
    }

    @Test
    fun homeHeroCarouselStatsTextUsesValidChineseUnits() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/HomeHeroCarousel.kt")
        // 可见统计文案：分隔符 · + 单位「播放」「弹幕」，禁止损坏字面量。
        assertTrue(source.contains("\" · \""))
        assertTrue(source.contains("formatStat(video.stat.view.toLong()) + \"播放\""))
        assertTrue(source.contains("formatStat(video.stat.danmaku.toLong()) + \"弹幕\""))
        assertFalse(source.contains("formatStat(video.stat.view.toLong()) + \"??\""))
        assertFalse(source.contains("formatStat(video.stat.danmaku.toLong()) + \"??\""))
        assertFalse(source.contains("\" \uFFFD \""))
    }

    @Test
    fun inlinePartitionPageKeepsPartitionVideoSourceInsteadOfHomeFeed() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val partitionPageSource = source
            .substringAfter("when (val entry = resolveHomeTopTabEntryOrNull(topTabEntries, page))")
            .substringAfter("HomeTopTabEntry.Partition ->")
            .substringBefore("is HomeTopTabEntry.Category ->")

        assertTrue(partitionPageSource.contains("LocalVideoCardSharedElementSourceRoute provides partitionVideoSourceRoute"))
        assertTrue(partitionPageSource.contains("onVideoClick = onPartitionVideoClick"))
        assertFalse(partitionPageSource.contains("wrappedOnVideoClick("))
    }

    @Test
    fun homeCategoryPageProvidesMatchingSourceRouteForSharedElementsAndNavigation() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val categoryPageSource = source
            .substringAfter("val sourceRoute = remember(category)")
            .substringBefore("// Loading Indicator at bottom")

        assertTrue(categoryPageSource.contains("LocalVideoCardSharedElementSourceRoute provides sourceRoute"))
        assertTrue(categoryPageSource.contains("sourceRoute = sourceRoute"))
        assertTrue(categoryPageSource.contains("HomeHeroCarousel("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
