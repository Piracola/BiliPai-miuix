package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeFeedScrollStatePersistenceStructureTest {

    @Test
    fun `home category grid states are saveable per category`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")

        assertTrue(source.contains("rememberSaveable("))
        assertTrue(source.contains("category.name"))
        assertTrue(source.contains("saver = LazyGridState.Saver"))
        assertTrue(source.contains("LazyGridState()"))
        assertFalse(source.contains("gridStates[category] = rememberLazyGridState()"))
    }

    @Test
    fun `video navigation freezes feed anchor before shared transition starts`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val clickSource = source
            .substringAfter("val wrappedOnVideoClick: (HomeVideoClickRequest) -> Unit")
            .substringBefore("val onTodayWatchVideoClick")

        val captureIndex = clickSource.indexOf("pendingFeedScrollAnchor = captureHomeFeedScrollAnchor(")
        val transitionStartIndex = clickSource.indexOf("hideTopTabsForForwardDetailNav = true")
        val navigationIndex = clickSource.indexOf("onVideoClick(request)")

        assertTrue(captureIndex >= 0)
        assertTrue(captureIndex < transitionStartIndex)
        assertTrue(transitionStartIndex < navigationIndex)
        assertTrue(source.contains("gridState != null && pendingFeedScrollAnchor == null"))
    }

    @Test
    fun `avatar preference does not change card bounds during predictive return`() {
        val screenSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val pageSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val standardCardSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt"
        )

        assertTrue(screenSource.contains("var settledShowHomeUpAvatars by rememberSaveable"))
        assertTrue(screenSource.contains("isReturnGestureInProgressProvider()"))
        assertTrue(screenSource.contains("!videoCardReturnGestureInProgress && !isReturningFromVideoDetail"))
        assertTrue(screenSource.contains("showUpAvatars = settledShowHomeUpAvatars"))

        // 关闭后不创建头像节点，也不创建兜底头像节点；Row 的 spacedBy 只作用于实际子项。
        assertTrue(pageSource.contains("if (showUpAvatars && video.owner.face.isNotBlank())"))
        assertTrue(pageSource.contains("} else if (showUpAvatars) {"))
        assertTrue(standardCardSource.contains("leadingContent = if (showUpAvatar && video.owner.face.isNotEmpty())"))
    }

    @Test
    fun `home skin atmosphere is fixed in header instead of pager backdrop`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val headerCallSource = source
            .substringAfter("HomeHeader(")
            .substringBefore("AnimatedVisibility(")

        assertTrue(source.contains("val uiSkinState by rememberUiSkinState(context)"))
        assertTrue(source.contains("val homeUiSkinDecoration = rememberHomeUiSkinDecoration(uiSkinState)"))
        assertTrue(headerCallSource.contains("uiSkinDecoration = homeUiSkinDecoration"))
        assertFalse(source.contains("import com.android.purebilibili.feature.home.components.HomeSkinAtmosphere"))
        assertFalse(source.contains("HomeSkinAtmosphere(\n                        decoration = homeUiSkinDecoration"))
    }

    @Test
    fun `home skin feed atmosphere is drawn once behind grid container`() {
        val screenSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val pageSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val pageCallSource = screenSource
            .substringAfter("HomeCategoryPageContent(")
            .substringBefore("firstGridItemModifier = Modifier")
        val pageFunctionSource = pageSource
            .substringAfter("internal fun HomeCategoryPageContent(")
            .substringBefore("@Composable\nprivate fun PopularSubCategorySegmentedControl")
        val gridContainerSource = pageFunctionSource
            .substringAfter("val feedAtmosphereImagePath = resolveHomeFeedSkinAtmosphereImagePath(uiSkinDecoration)")
            .substringBefore("LazyVerticalGrid(")
        val videoItemSource = pageFunctionSource
            .substringAfter("categoryState.videos.forEachIndexed")

        assertTrue(pageCallSource.contains("uiSkinDecoration = homeUiSkinDecoration"))
        assertTrue(pageFunctionSource.contains("uiSkinDecoration: HomeUiSkinDecoration? = null"))
        assertTrue(pageFunctionSource.contains("val feedAtmosphereImagePath = resolveHomeFeedSkinAtmosphereImagePath(uiSkinDecoration)"))
        assertTrue(gridContainerSource.contains("AsyncImage("))
        assertTrue(gridContainerSource.contains("model = File(feedAtmosphereImagePath)"))
        assertFalse(videoItemSource.contains("resolveHomeFeedSkinAtmosphereImagePath(uiSkinDecoration)"))
        assertFalse(videoItemSource.contains("model = File(feedAtmosphereImagePath)"))
    }

    @Test
    fun `home pager page refresh uses page category instead of stale current state`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val pagerPageSource = source
            .substringAfter("HorizontalPager(")
            .substringBefore("// Close HorizontalPager lambda")
        val onRefreshSource = pagerPageSource
            .substringAfter("onRefresh = {")
            .substringBefore("},")

        assertTrue(onRefreshSource.contains("viewModel.refresh(category)"))
        assertFalse(onRefreshSource.contains("viewModel.refresh()"))
        assertFalse(onRefreshSource.contains("if (category == HomeCategory.FOLLOW)"))
    }

    @Test
    fun `home feed samples scroll state without observing every scroll transition`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val pageFunctionSource = source
            .substringAfter("internal fun HomeCategoryPageContent(")
            .substringBefore("@Composable\nprivate fun PopularSubCategorySegmentedControl")

        assertTrue(pageFunctionSource.contains("Snapshot.withoutReadObservation"))
        assertTrue(pageFunctionSource.contains("gridState.isScrollInProgress"))
        assertFalse(pageFunctionSource.contains("derivedStateOf { gridState.isScrollInProgress }"))
    }

    @Test
    fun `home pager frame state is read inside isolated motion layer`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt")
        val tabsSource = source
            .substringAfter("private fun LightweightHomeTopTabs(")
            .substringBefore("internal enum class TopTabLiquidColorMode")
        val motionSource = tabsSource.substringAfter("HomeTopTabMotionLayer {")

        assertTrue(tabsSource.contains("val currentPositionProvider = remember(pagerState, selectedIndex)"))
        assertTrue(tabsSource.contains("val pagerScrollingProvider = remember(pagerState)"))
        assertTrue(motionSource.contains("val currentPosition = currentPositionProvider()"))
        assertTrue(motionSource.contains("val pagerIsScrolling = pagerScrollingProvider()"))
    }

    @Test
    fun `category initial load is owned and cancelled by home view model`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeViewModel.kt")
        val switchSource = source
            .substringAfter("fun switchCategory(category: HomeCategory)")
            .substringBefore("fun updateDisplayedTabIndex")

        assertTrue(source.contains("private var categoryInitialLoadJob: Job? = null"))
        assertTrue(switchSource.contains("categoryInitialLoadJob?.cancel()"))
        assertTrue(switchSource.contains("fetchData(isLoadMore = false, category = category)"))
        assertTrue(switchSource.contains("catch (error: CancellationException)"))
        assertTrue(switchSource.contains("throw error"))
        assertFalse(source.contains("sessionSeenBvids"))
    }

    @Test
    fun `home follow refresh preserves dynamic update baseline for incremental content`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeViewModel.kt")
        val followFeedSource = source
            .substringAfter("private suspend fun fetchFollowFeed")
            .substringBefore("private fun videoItemKey")

        assertFalse(followFeedSource.contains("DynamicRepository.resetPagination"))
    }

    @Test
    fun `home follow feed requests video dynamics instead of filtering all dynamics after baseline`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeViewModel.kt")
        val followFeedSource = source
            .substringAfter("private suspend fun fetchFollowFeed")
            .substringBefore("private fun videoItemKey")

        assertTrue(followFeedSource.contains("val followType = \"video\""))
        assertTrue(followFeedSource.contains("type = followType"))
    }

    @Test
    fun `home follow manual refresh reports api update_num via baseline probe`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeViewModel.kt")
        val fetchDataSource = source
            .substringAfter("if (currentCategory == HomeCategory.FOLLOW)")
            .substringBefore("val currentCategoryState")
        val followFeedSource = source
            .substringAfter("private suspend fun fetchFollowFeed")
            .substringBefore("private fun videoItemKey")

        assertTrue(fetchDataSource.contains("val result = fetchFollowFeed("))
        assertTrue(fetchDataSource.contains("return result"))
        assertTrue(followFeedSource.contains("probeWithBaseline"))
        assertTrue(followFeedSource.contains("resolveHomeFollowRefreshNewItemsCount("))
        assertTrue(followFeedSource.contains("currentUpdateBaseline("))
        assertTrue(followFeedSource.contains("return tipCount"))
    }

    @Test
    fun `home feed refilters existing content after ad filter configuration is ready`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeViewModel.kt")
        val initSource = source
            .substringAfter("syncTodayWatchFeedbackFromStore()")
            .substringBefore("PluginManager.pluginsFlow.collect")
        val refilterSource = source
            .substringAfter("private fun reFilterAllContent()")
            .substringBefore("private fun resolveTodayWatchRuntimeConfig")

        assertTrue(initSource.contains("PluginManager.awaitPluginReady(ADFILTER_PLUGIN_ID)"))
        assertTrue(initSource.contains("reFilterAllContent()"))
        assertTrue(refilterSource.contains("PluginManager.filterFeedItems("))
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
