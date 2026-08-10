package com.android.purebilibili.feature.list
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.components.AppAssistChip
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppCard
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSmallFloatingActionButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.core.ui.components.AppTextField
import com.android.purebilibili.core.ui.components.AppSwitch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animate
import dev.chrisbanes.haze.HazeState
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.unifiedBlur
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Job
import androidx.compose.ui.platform.LocalContext // [New]
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity // [New]
import androidx.compose.ui.zIndex // [New]
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned // [New]
import com.android.purebilibili.core.store.SettingsManager // [New]
import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeDurationStyle
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.ui.blur.BlurStyles // [New]
import com.android.purebilibili.core.ui.blur.BlurSurfaceType
import com.android.purebilibili.core.ui.blur.currentUnifiedBlurIntensity
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.adaptive.resolveDeviceUiProfile
import com.android.purebilibili.core.ui.adaptive.resolveEffectiveMotionTier
import com.android.purebilibili.core.ui.LocalBottomBarContentPadding
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.core.util.responsiveContentWidth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.DisposableEffect // [Fix] Missing import
import kotlinx.coroutines.launch // [Fix] Import
//  Material Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.LocalGlobalWallpaperBackdropVisible
import com.android.purebilibili.feature.home.LocalHomeScrollOffset
import com.android.purebilibili.feature.home.policy.resolveBottomBarChromeScrollOffset
import com.android.purebilibili.core.ui.rememberAppChevronUpIcon
import com.android.purebilibili.core.ui.rememberAppChevronDownIcon
import com.android.purebilibili.core.ui.resolveGlobalWallpaperChromeColor
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.core.ui.rememberAppChromeLiquidGlassEnabled
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.ui.components.AppSearchField
import com.android.purebilibili.core.ui.animation.DissolveAnimationPreset
import com.android.purebilibili.core.ui.animation.MaybeDissolvableVideoCard
import com.android.purebilibili.core.ui.animation.jiggleOnDissolve
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.LocalSharedTransitionEnabled
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.ui.rememberAppFolderIcon
import com.android.purebilibili.core.ui.rememberAppHeadphonesIcon
import com.android.purebilibili.core.ui.rememberAppPlayIcon
import com.android.purebilibili.core.ui.transition.BiliPaiSharedElementKey
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.VideoGridItemSkeleton
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.feature.home.components.cards.ElegantVideoCard
import com.android.purebilibili.feature.personal.resolvePersonalListColumnCount
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.rememberAdaptiveGridColumns
import com.android.purebilibili.core.util.rememberResponsiveSpacing
import com.android.purebilibili.data.model.response.HistoryBusiness
import com.android.purebilibili.data.model.response.HistoryItem
import com.android.purebilibili.data.model.response.FavoriteSection
import com.android.purebilibili.data.model.response.FavoriteSearchScope
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.article.ArticleSharedElementSlot
import com.android.purebilibili.feature.article.resolveHistoryArticleCoverAspectRatio
import com.android.purebilibili.feature.article.resolveArticleSharedTransitionKey
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.space.SeasonSeriesDetailViewModel
import com.android.purebilibili.feature.video.player.ExternalPlaylistSource
import com.android.purebilibili.feature.video.player.PlayMode
import com.android.purebilibili.feature.video.player.PlaylistManager
import com.android.purebilibili.feature.video.player.PlaylistSession
import com.android.purebilibili.core.util.resolveScrollToTopPlan
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

internal enum class FavoriteContentMode {
    BASE_LIST,
    SINGLE_FOLDER,
    PAGER
}

private enum class FavoriteBrowseSection {
    OWNED,
    SUBSCRIBED
}

internal fun resolveFavoriteContentMode(
    isFavoritePage: Boolean,
    folderCount: Int
): FavoriteContentMode {
    if (!isFavoritePage) return FavoriteContentMode.BASE_LIST
    return when {
        folderCount > 1 -> FavoriteContentMode.PAGER
        folderCount == 1 -> FavoriteContentMode.SINGLE_FOLDER
        else -> FavoriteContentMode.BASE_LIST
    }
}

internal fun resolveFavoritePlayAllItems(
    mode: FavoriteContentMode,
    baseItems: List<VideoItem>,
    selectedFolderItems: List<VideoItem>,
    singleFolderItems: List<VideoItem>
): List<VideoItem> {
    val candidateItems = when (mode) {
        FavoriteContentMode.PAGER -> selectedFolderItems.ifEmpty { baseItems }
        FavoriteContentMode.SINGLE_FOLDER -> singleFolderItems.ifEmpty { baseItems }
        FavoriteContentMode.BASE_LIST -> baseItems
    }
    return candidateItems.filter { !it.isCollectionResource && it.bvid.isNotBlank() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonListScreen(
    viewModel: BaseListViewModel,
    onBack: () -> Unit,
    onVideoClick: (String, Long, String, Boolean) -> Unit,
    onUpClick: ((Long) -> Unit)? = null,
    onCollectionClick: ((FavoriteCollectionRoute) -> Unit)? = null,
    onFavoriteFolderClick: ((Long, Long, String, String) -> Unit)? = null,
    onFavoriteBangumiClick: (Long) -> Unit = {},
    onFavoriteArticleClick: (Long, String) -> Unit = { _, _ -> },
    onFavoriteTopicClick: (Long) -> Unit = {},
    onFavoriteWebClick: (String, String) -> Unit = { _, _ -> },
    initialSearchQuery: String = "",
    initialFavoriteSearchScope: FavoriteSearchScope = FavoriteSearchScope.CURRENT_FOLDER,
    isSearchDestination: Boolean = false,
    onOpenSearchDestination: ((String) -> Unit)? = null,
    onPlayAllAudioClick: ((String, Long) -> Unit)? = null,
    globalHazeState: HazeState? = null, // [新增] 接收全局 HazeState
    scrollToTopChannel: Channel<Unit>? = null,
    favoriteCollectionSharedElementRoute: FavoriteCollectionRoute? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val primaryGridState = rememberLazyGridState()
    val subscribedFolderListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val favoritePagerGridStates = remember { mutableStateMapOf<Int, androidx.compose.foundation.lazy.grid.LazyGridState>() }

    // 📱 响应式布局参数
    // Fix: 手机端(Compact)使用较小的最小宽度以保证2列显示 (360dp / 170dp = 2.1 -> 2列)
    // 平板端(Expanded)使用较大的最小宽度以避免卡片过小
    val context = LocalContext.current
    val showOnlineCount by SettingsManager.getShowOnlineCount(context).collectAsStateWithLifecycle(initialValue = false
        )
    val homeSettings by SettingsManager.getHomeSettings(context).collectAsStateWithLifecycle(initialValue = com.android.purebilibili.core.store.HomeSettings(),
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val topChromePolicy = rememberAppTopChromePolicy()
    val liquidGlassEnabled = rememberAppChromeLiquidGlassEnabled(
        individualEnabled = homeSettings.isLiquidGlassEnabled,
        androidNativeEnabled = homeSettings.androidNativeLiquidGlassEnabled,
    )
    val windowSizeClass = LocalWindowSizeClass.current
    val deviceUiProfile = remember(windowSizeClass.widthSizeClass) {
        resolveDeviceUiProfile(
            widthSizeClass = windowSizeClass.widthSizeClass
        )
    }
    val cardMotionTier = resolveEffectiveMotionTier(
        baseTier = deviceUiProfile.motionTier,
        animationEnabled = homeSettings.cardAnimationEnabled
    )
    val favoriteCollectionSharedTransitionEnabled =
        homeSettings.cardTransitionEnabled && LocalSharedTransitionEnabled.current

    val minColWidth = remember(windowSizeClass.isExpandedScreen) {
        resolveCommonListGridMinColumnWidth(windowSizeClass.isExpandedScreen)
    }
    val adaptiveColumns = rememberAdaptiveGridColumns(minColumnWidth = minColWidth)

    // [新增] 优先使用用户设置的列数
    val columns = if (homeSettings.gridColumnCount > 0) homeSettings.gridColumnCount else adaptiveColumns
    val configuration = LocalConfiguration.current
    val personalListColumns = remember(configuration.screenWidthDp) {
        resolvePersonalListColumnCount(configuration.screenWidthDp.toFloat())
    }
    val spacing = rememberResponsiveSpacing()

    //  [修复] 分页支持：收藏 + 历史记录
    val favoriteViewModel = viewModel as? FavoriteViewModel
    val historyViewModel = viewModel as? HistoryViewModel
    val seasonSeriesDetailViewModel = viewModel as? SeasonSeriesDetailViewModel
    val historyDeleteSession by historyViewModel?.deleteSession?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<HistoryDeleteSession?>(null) }
    val isHistoryPaused by historyViewModel?.isHistoryPausedState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isHistoryManagementBusy by historyViewModel?.isHistoryManagementBusyState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val historyHasMore by historyViewModel?.hasMoreState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val historyIsLoadingMore by historyViewModel?.isLoadingMoreState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var historyContentFilter by rememberSaveable { androidx.compose.runtime.mutableStateOf(HistoryContentFilter.ALL) }
    var isHistoryBatchMode by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var selectedHistoryKeys by rememberSaveable { androidx.compose.runtime.mutableStateOf(setOf<String>()) }
    var showHistoryBatchDeleteConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var showHistoryManagementMenu by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var showHistoryClearConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var pendingHistorySingleDeleteKey by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val supportsCollapsibleCommonListHeader = historyViewModel != null || favoriteViewModel != null
    val visibleHistoryItems = remember(state.items, historyContentFilter, historyViewModel) {
        if (historyViewModel == null) {
            state.items
        } else {
            filterHistoryItemsByContent(
                items = state.items,
                filter = historyContentFilter,
                resolveHistoryItem = { video ->
                    historyViewModel.getHistoryItem(historyViewModel.resolveHistoryLookupKey(video))
                }
            )
        }
    }

    LaunchedEffect(
        historyViewModel,
        historyContentFilter,
        state.isLoading,
        state.items.size,
        visibleHistoryItems.size,
        historyHasMore,
        historyIsLoadingMore
    ) {
        if (
            historyViewModel != null &&
            !state.isLoading &&
            shouldLoadMoreHistoryFilterResults(
                filter = historyContentFilter,
                filteredItemCount = visibleHistoryItems.size,
                hasMore = historyHasMore,
                isLoading = historyIsLoadingMore
            )
        ) {
            historyViewModel.loadMore()
        }
    }

    LaunchedEffect(state.items, historyViewModel, isHistoryBatchMode) {
        if (historyViewModel == null) return@LaunchedEffect
        val validKeys = state.items
            .map(historyViewModel::resolveHistoryRenderKey)
            .filter { it.isNotBlank() }
            .toSet()
        selectedHistoryKeys = selectedHistoryKeys.filter { it in validKeys }.toSet()
        if (isHistoryBatchMode && state.items.isEmpty()) {
            isHistoryBatchMode = false
            selectedHistoryKeys = emptySet()
        }
        if (state.items.isEmpty()) {
            showHistoryClearConfirm = false
        }
    }

    // [Feature] BottomBar Scroll Hiding for CommonListScreen (History/Favorite)
    val setBottomBarVisible = com.android.purebilibili.core.ui.LocalSetBottomBarVisible.current
    val bottomBarChromeScrollOffset = LocalHomeScrollOffset.current

    // 监听列表滚动实现底栏自动隐藏/显示
    var lastFirstVisibleItem by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var lastScrollOffset by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }

    // 离开页面时恢复底栏显示
    DisposableEffect(Unit) {
        onDispose {
            setBottomBarVisible(true)
            bottomBarChromeScrollOffset.value = 0f
        }
    }

    // [Fix] Import for launch
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val headerSettleMotionSpec = AppMotionTokens.standardSpec<Float>()

    // 📁 [新增] 收藏夹切换 Tab
    val foldersState by favoriteViewModel?.folders?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    val subscribedFoldersState by favoriteViewModel?.subscribedFolders?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(emptyList()) }
    val subscribedFolderProgressState by favoriteViewModel?.subscribedFolderProgressState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf(FavoriteViewModel.SubscribedFolderProgressState())
        }
    val selectedFolderIndex by favoriteViewModel?.selectedFolderIndex?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val favoriteOrder by favoriteViewModel?.favoriteOrderState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(FavoriteResourceOrder.FAVORITE_TIME) }
    val isFavoriteManaging by favoriteViewModel?.isFavoriteManagingState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val favoriteSearchUiState by favoriteViewModel?.searchUiState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ListUiState()) }
    val favoriteDetailProgressState by seasonSeriesDetailViewModel?.favoriteDetailProgressState?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember {
            androidx.compose.runtime.mutableStateOf(SeasonSeriesDetailViewModel.FavoriteDetailProgressState())
        }
    var favoriteBrowseSection by rememberSaveable { androidx.compose.runtime.mutableStateOf(FavoriteBrowseSection.OWNED) }
    var showFavoriteManagementMenu by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var showFavoriteCleanInvalidConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var showFavoriteDynamicShareConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var favoriteSection by rememberSaveable { androidx.compose.runtime.mutableStateOf(FavoriteSection.VIDEO) }
    var isFavoriteBatchMode by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var selectedFavoriteResourceIds by rememberSaveable { androidx.compose.runtime.mutableStateOf(setOf<Long>()) }
    var showFavoriteBatchDeleteConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var pendingFavoriteTransferCopy by rememberSaveable { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
    var showFavoriteBatchMenu by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var favoriteFolderEditorMode by rememberSaveable { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var favoriteFolderEditorTitle by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var favoriteFolderEditorIntro by rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    var favoriteFolderEditorPrivate by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    var showFavoriteFolderDeleteConfirm by rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    LaunchedEffect(foldersState.size, subscribedFoldersState.size) {
        favoriteBrowseSection = when {
            favoriteBrowseSection == FavoriteBrowseSection.SUBSCRIBED && subscribedFoldersState.isNotEmpty() -> FavoriteBrowseSection.SUBSCRIBED
            foldersState.isNotEmpty() -> FavoriteBrowseSection.OWNED
            subscribedFoldersState.isNotEmpty() -> FavoriteBrowseSection.SUBSCRIBED
            else -> FavoriteBrowseSection.OWNED
        }
    }
    val isSubscribedBrowse = favoriteViewModel != null &&
        favoriteSection == FavoriteSection.VIDEO &&
        favoriteBrowseSection == FavoriteBrowseSection.SUBSCRIBED
    val loadMoreOwner = resolveCommonListLoadMoreOwner(
        isSubscribedBrowse = isSubscribedBrowse,
        hasFavoriteViewModel = favoriteViewModel != null,
        hasHistoryViewModel = historyViewModel != null,
        hasSeasonSeriesDetailViewModel = seasonSeriesDetailViewModel != null
    )
    val shouldUseFavoritePlaybackQueue = shouldUseFavoriteExternalPlaylist(
        hasFavoriteViewModel = favoriteViewModel != null,
        isFavoriteDetail = seasonSeriesDetailViewModel?.isFavoriteDetail == true
    )
    val favoriteContentMode = resolveFavoriteContentMode(
        isFavoritePage = favoriteViewModel != null &&
            favoriteSection == FavoriteSection.VIDEO &&
            !isSubscribedBrowse,
        folderCount = foldersState.size
    )
    val selectedFolderUiState by favoriteViewModel
        ?.getFolderUiState(selectedFolderIndex)
        ?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ListUiState()) }
    val singleFolderUiState by favoriteViewModel
        ?.getFolderUiState(0)
        ?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ListUiState()) }
    val activeFavoriteItems = resolveFavoritePlayAllItems(
        mode = favoriteContentMode,
        baseItems = state.items,
        selectedFolderItems = selectedFolderUiState.items,
        singleFolderItems = singleFolderUiState.items
    ).takeUnless { isSubscribedBrowse }.orEmpty()
    val selectedFavoriteFolder = foldersState.getOrNull(selectedFolderIndex)
    LaunchedEffect(selectedFolderIndex, favoriteBrowseSection, favoriteSection) {
        isFavoriteBatchMode = false
        selectedFavoriteResourceIds = emptySet()
        pendingFavoriteTransferCopy = null
    }
    val toggleFavoriteResourceSelection: (Long) -> Unit = { resourceId ->
        if (resourceId > 0L) {
            selectedFavoriteResourceIds = if (resourceId in selectedFavoriteResourceIds) {
                selectedFavoriteResourceIds - resourceId
            } else {
                selectedFavoriteResourceIds + resourceId
            }
        }
    }
    val enterFavoriteBatchMode: (Long) -> Unit = { resourceId ->
        if (resourceId > 0L) {
            isFavoriteBatchMode = true
            selectedFavoriteResourceIds = selectedFavoriteResourceIds + resourceId
        }
    }
    val progressBadge = remember(
        favoriteDetailProgressState,
        seasonSeriesDetailViewModel
    ) {
        if (
            seasonSeriesDetailViewModel != null &&
            (favoriteDetailProgressState.expectedCount > 0 || favoriteDetailProgressState.loadedCount > 0)
        ) {
            resolveFavoriteDetailProgressBadge(
                loadedCount = favoriteDetailProgressState.loadedCount,
                expectedCount = favoriteDetailProgressState.expectedCount,
                currentPage = favoriteDetailProgressState.currentPage,
                lastAddedCount = favoriteDetailProgressState.lastAddedCount,
                invalidCount = favoriteDetailProgressState.invalidCount,
                hasMore = favoriteDetailProgressState.hasMore
            )
        } else {
            null
        }
    }

    // [新增] Pager State (仅当有多个文件夹时使用)
    // 尽管 compose 会自动处理 rememberKey，但这里用 foldersState.size 作为 key 确保变化时重置
    val pagerState = rememberPagerState(initialPage = 0) {
        if (favoriteViewModel != null && foldersState.size > 1) foldersState.size else 0
    }

    val commonListBottomPadding = LocalBottomBarContentPadding.current
    val activeCommonListScrollState = remember(
        favoriteViewModel,
        favoriteContentMode,
        isSubscribedBrowse,
        pagerState.currentPage,
        primaryGridState,
        subscribedFolderListState,
        favoritePagerGridStates.size
    ) {
        {
            when {
                isSubscribedBrowse -> CommonListScrollState.List(subscribedFolderListState)
                favoriteViewModel != null && favoriteContentMode == FavoriteContentMode.PAGER -> {
                    favoritePagerGridStates[pagerState.currentPage]?.let(CommonListScrollState::Grid)
                        ?: CommonListScrollState.Grid(primaryGridState)
                }
                else -> CommonListScrollState.Grid(primaryGridState)
            }
        }
    }
    LaunchedEffect(activeCommonListScrollState) {
        snapshotFlow {
            when (val scrollState = activeCommonListScrollState()) {
                is CommonListScrollState.Grid -> Pair(
                    scrollState.state.firstVisibleItemIndex,
                    scrollState.state.firstVisibleItemScrollOffset
                )
                is CommonListScrollState.List -> Pair(
                    scrollState.state.firstVisibleItemIndex,
                    scrollState.state.firstVisibleItemScrollOffset
                )
            }
        }
            .distinctUntilChanged()
            .collect { (firstVisibleItem, scrollOffset) ->
                if (firstVisibleItem == 0 && scrollOffset < 100) {
                    setBottomBarVisible(true)
                } else {
                    val isScrollingDown = when {
                        firstVisibleItem > lastFirstVisibleItem -> true
                        firstVisibleItem < lastFirstVisibleItem -> false
                        else -> scrollOffset > lastScrollOffset + 50
                    }
                    val isScrollingUp = when {
                        firstVisibleItem < lastFirstVisibleItem -> true
                        firstVisibleItem > lastFirstVisibleItem -> false
                        else -> scrollOffset < lastScrollOffset - 50
                    }

                    if (isScrollingDown) setBottomBarVisible(false)
                    if (isScrollingUp) setBottomBarVisible(true)
                }
                lastFirstVisibleItem = firstVisibleItem
                lastScrollOffset = scrollOffset
                bottomBarChromeScrollOffset.value = resolveBottomBarChromeScrollOffset(
                    firstVisibleItem = firstVisibleItem,
                    scrollOffset = scrollOffset
                )
            }
    }
    val shouldShowBackToTop by remember(activeCommonListScrollState) {
        derivedStateOf {
            when (val scrollState = activeCommonListScrollState()) {
                is CommonListScrollState.Grid -> shouldShowCommonListBackToTop(
                    firstVisibleItemIndex = scrollState.state.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = scrollState.state.firstVisibleItemScrollOffset
                )
                is CommonListScrollState.List -> shouldShowCommonListBackToTop(
                    firstVisibleItemIndex = scrollState.state.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = scrollState.state.firstVisibleItemScrollOffset
                )
            }
        }
    }

    suspend fun scrollCommonListToTop() {
        when (val scrollState = activeCommonListScrollState()) {
            is CommonListScrollState.Grid -> {
                val currentIndex = scrollState.state.firstVisibleItemIndex
                val plan = resolveScrollToTopPlan(currentIndex)
                plan.preJumpIndex?.let { preJump ->
                    if (currentIndex > preJump) {
                        scrollState.state.scrollToItem(preJump)
                    }
                }
                scrollState.state.animateScrollToItem(plan.animateTargetIndex)
            }
            is CommonListScrollState.List -> {
                val currentIndex = scrollState.state.firstVisibleItemIndex
                val plan = resolveScrollToTopPlan(currentIndex)
                plan.preJumpIndex?.let { preJump ->
                    if (currentIndex > preJump) {
                        scrollState.state.scrollToItem(preJump)
                    }
                }
                scrollState.state.animateScrollToItem(plan.animateTargetIndex)
            }
        }
    }

    LaunchedEffect(scrollToTopChannel) {
        scrollToTopChannel?.receiveAsFlow()?.collect {
            scrollCommonListToTop()
        }
    }

    // [Fix] 协程作用域 (用于 UI 事件触发的滚动)
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // [Fix] 这里的模糊冲突核心：顶栏需要自己的独立 HazeState
    val localHazeState = com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState()
    val commonListChromeBackdrop = rememberLayerBackdrop()

    // 🔍 搜索状态
    var searchQuery by rememberSaveable { androidx.compose.runtime.mutableStateOf(initialSearchQuery) }
    var favoriteSearchScope by rememberSaveable {
        androidx.compose.runtime.mutableStateOf(initialFavoriteSearchScope)
    }
    LaunchedEffect(
        searchQuery,
        favoriteSearchScope,
        isSearchDestination,
        favoriteViewModel,
        historyViewModel,
    ) {
        if (isSearchDestination && favoriteViewModel != null) {
            kotlinx.coroutines.delay(350)
            favoriteViewModel.searchVideos(searchQuery, favoriteSearchScope)
        } else if (isSearchDestination && historyViewModel != null) {
            kotlinx.coroutines.delay(350)
            historyViewModel.searchHistory(searchQuery)
        }
    }
    // [New] 动态顶栏高度测量 (最准确的方式)
    var headerHeightPx by androidx.compose.runtime.remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val headerHeightDp = with(LocalDensity.current) { headerHeightPx.toDp() }
    var commonListHeaderOffsetPx by remember { mutableFloatStateOf(0f) }
    var commonListHeaderSettleJob by remember { androidx.compose.runtime.mutableStateOf<Job?>(null) }
    val commonListHeaderCollapseMode = resolveCommonListHeaderCollapseModeForScreen(
        configuredMode = homeSettings.commonListHeaderCollapseMode,
        isFavoritePage = favoriteViewModel != null
    )
    val commonListHeaderCollapseEnabled = supportsCollapsibleCommonListHeader &&
        commonListHeaderCollapseMode != CommonListHeaderCollapseMode.ALWAYS_VISIBLE
    fun animateCommonListHeaderOffsetTo(targetOffsetPx: Float) {
        if (kotlin.math.abs(commonListHeaderOffsetPx - targetOffsetPx) <= 0.5f) {
            commonListHeaderOffsetPx = targetOffsetPx
            return
        }
        commonListHeaderSettleJob?.cancel()
        commonListHeaderSettleJob = scope.launch {
            animate(
                initialValue = commonListHeaderOffsetPx,
                targetValue = targetOffsetPx,
                animationSpec = headerSettleMotionSpec
            ) { value, _ ->
                commonListHeaderOffsetPx = value
            }
        }.also { job ->
            job.invokeOnCompletion {
                if (commonListHeaderSettleJob === job) {
                    commonListHeaderSettleJob = null
                }
            }
        }
    }
    val isCommonListAtTop by remember(activeCommonListScrollState) {
        derivedStateOf {
            when (val scrollState = activeCommonListScrollState()) {
                is CommonListScrollState.Grid ->
                    scrollState.state.firstVisibleItemIndex == 0 &&
                        scrollState.state.firstVisibleItemScrollOffset == 0
                is CommonListScrollState.List ->
                    scrollState.state.firstVisibleItemIndex == 0 &&
                        scrollState.state.firstVisibleItemScrollOffset == 0
            }
        }
    }
    LaunchedEffect(
        commonListHeaderCollapseMode,
        isCommonListAtTop,
        headerHeightPx,
        supportsCollapsibleCommonListHeader,
        favoriteContentMode,
        pagerState.isScrollInProgress
    ) {
        if (favoriteContentMode == FavoriteContentMode.PAGER && pagerState.isScrollInProgress) {
            return@LaunchedEffect
        }
        if (
            !supportsCollapsibleCommonListHeader ||
            commonListHeaderCollapseMode == CommonListHeaderCollapseMode.ALWAYS_VISIBLE ||
            isCommonListAtTop
        ) {
            animateCommonListHeaderOffsetTo(0f)
        }
    }
    LaunchedEffect(
        commonListHeaderCollapseMode,
        headerHeightPx,
        supportsCollapsibleCommonListHeader,
        isSubscribedBrowse,
        favoriteContentMode,
        pagerState.settledPage,
        favoritePagerGridStates.size
    ) {
        val (firstVisibleItemIndex, firstVisibleItemScrollOffset) =
            when (val scrollState = activeCommonListScrollState()) {
                is CommonListScrollState.Grid -> Pair(
                    scrollState.state.firstVisibleItemIndex,
                    scrollState.state.firstVisibleItemScrollOffset
                )
                is CommonListScrollState.List -> Pair(
                    scrollState.state.firstVisibleItemIndex,
                    scrollState.state.firstVisibleItemScrollOffset
                )
            }
        val targetOffsetPx = resolveCommonListHeaderOffsetForSettledContent(
            firstVisibleItemIndex = firstVisibleItemIndex,
            firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
            maxCollapsePx = headerHeightPx.toFloat(),
            mode = if (supportsCollapsibleCommonListHeader) {
                commonListHeaderCollapseMode
            } else {
                CommonListHeaderCollapseMode.ALWAYS_VISIBLE
            }
        )
        animateCommonListHeaderOffsetTo(targetOffsetPx)
    }
    val commonListHeaderScrollConnection = remember(
        commonListHeaderCollapseMode,
        headerHeightPx,
        isCommonListAtTop,
        supportsCollapsibleCommonListHeader
    ) {
        object : NestedScrollConnection {
            // 仅跟随内容实际消费的位移，避免横向标签行的纵向手势让顶部栏与列表占位失步。
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (
                    !supportsCollapsibleCommonListHeader ||
                    kotlin.math.abs(consumed.y) < 0.5f ||
                    kotlin.math.abs(consumed.y) < kotlin.math.abs(consumed.x)
                ) {
                    return Offset.Zero
                }
                commonListHeaderSettleJob?.cancel()
                commonListHeaderSettleJob = null
                commonListHeaderOffsetPx = resolveCommonListHeaderOffsetAfterContentScroll(
                    currentOffsetPx = commonListHeaderOffsetPx,
                    contentConsumedDeltaYPx = consumed.y,
                    maxCollapsePx = headerHeightPx.toFloat(),
                    isAtTop = isCommonListAtTop,
                    mode = commonListHeaderCollapseMode
                )
                return Offset.Zero
            }
        }
    }

    // [Feature] Header Blur Optimization
    val isHeaderBlurEnabled = remember(homeSettings) {
        resolveCommonListHeaderBlurEnabled(
            homeSettings = homeSettings,
        )
    }
    val videoCardAppearance = remember(homeSettings, liquidGlassEnabled) {
        resolveCommonListVideoCardAppearance(
            homeSettings = homeSettings,
            liquidGlassEnabled = liquidGlassEnabled,
        )
    }
    val favoriteHeaderLayout = remember(topChromePolicy) {
        resolveCommonListFavoriteHeaderLayout(
            topChromePolicy = topChromePolicy,
        )
    }
    val historyFilterChrome = remember(homeSettings, topChromePolicy) {
        resolveHistoryFilterTabChromeSpec(
            homeSettings = homeSettings,
            topChromePolicy = topChromePolicy,
        )
    }
    val blurIntensity = currentUnifiedBlurIntensity()
    val backgroundAlpha = BlurStyles.getBackgroundAlpha(blurIntensity)
    val headerBackgroundAlpha = if (favoriteViewModel != null) {
        (backgroundAlpha * favoriteHeaderLayout.headerBackgroundAlphaMultiplier).coerceIn(0f, 1f)
    } else {
        backgroundAlpha
    }
    val globalWallpaperVisible = LocalGlobalWallpaperBackdropVisible.current
    val shouldUseHeaderLocalBlur = shouldUseCommonListHeaderLocalBlur(
        headerBlurEnabled = isHeaderBlurEnabled,
        globalWallpaperVisible = globalWallpaperVisible
    )
    val headerBackgroundColor = resolveGlobalWallpaperChromeColor(
        requestedColor = AppSurfaceTokens.surface().copy(
            alpha = if (isHeaderBlurEnabled) headerBackgroundAlpha else 1f
        ),
        defaultBackgroundColor = AppSurfaceTokens.background(),
        defaultSurfaceColor = AppSurfaceTokens.surface(),
        globalWallpaperVisible = globalWallpaperVisible
    )

    // 决定顶栏背景 (使用私有的 localHazeState)
    val topBarBackgroundModifier = if (shouldUseHeaderLocalBlur) {
        Modifier
            .fillMaxWidth()
            .unifiedBlur(
                hazeState = localHazeState,
                surfaceType = BlurSurfaceType.HEADER
            )
            .background(headerBackgroundColor)
    } else {
        Modifier
            .fillMaxWidth()
            .background(headerBackgroundColor)
    }

    val playFavoriteVideo: (List<VideoItem>, String, Long, String, Int?, Boolean) -> Unit =
        { items, bvid, cid, coverUrl, folderIndex, playAllAudio ->
            fun startPlayback(playlistItems: List<VideoItem>): PlaylistSession? {
                val externalPlaylist = buildExternalPlaylistFromFavorite(
                    items = playlistItems,
                    clickedBvid = bvid
                )
                val playlistSession = externalPlaylist?.let { playlist ->
                    PlaylistManager.setExternalPlaylist(
                        playlist.playlistItems,
                        playlist.startIndex,
                        source = ExternalPlaylistSource.FAVORITE
                    ).also { PlaylistManager.setPlayMode(PlayMode.SEQUENTIAL) }
                }
                val isVertical = playlistItems.firstOrNull { it.bvid == bvid }?.isVertical ?: false
                if (playAllAudio) {
                    onPlayAllAudioClick?.invoke(bvid, cid)
                        ?: onVideoClick(bvid, cid, coverUrl, isVertical)
                } else {
                    onVideoClick(bvid, cid, coverUrl, isVertical)
                }
                return playlistSession
            }
            if (favoriteViewModel != null && folderIndex != null) {
                val playlistSession = startPlayback(items)
                if (playlistSession != null) {
                    favoriteViewModel.loadAllForPlayback(folderIndex) { allItems ->
                        buildExternalPlaylistFromFavorite(allItems)?.let { playlist ->
                            PlaylistManager.addAllToPlaylistIfCurrent(
                                items = playlist.playlistItems,
                                session = playlistSession,
                            )
                        }
                    }
                }
            } else {
                startPlayback(items)
            }
        }

    AppScaffold(
        modifier = Modifier
            .nestedScroll(commonListHeaderScrollConnection)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = AppSurfaceTokens.groupedListContainer()
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. 底层：内容区域
            // [Haze Audit] 全局源已在 AppNavigation 根层提供，这里仅保留本地源
            val contentModifier = Modifier
                .fillMaxSize()
                .layerBackdrop(commonListChromeBackdrop)
                .hazeSourceCompat(state = localHazeState)

            Box(modifier = contentModifier) {
                if (
                    favoriteViewModel != null &&
                    isSearchDestination &&
                    favoriteSection == FavoriteSection.VIDEO &&
                    searchQuery.isNotBlank()
                ) {
                    CommonListContent(
                        items = favoriteSearchUiState.items,
                        isLoading = favoriteSearchUiState.isLoading,
                        error = favoriteSearchUiState.error,
                        searchQuery = "",
                        columns = personalListColumns,
                        isFavoritePersonalList = true,
                        spacing = spacing.medium,
                        padding = PaddingValues(top = headerHeightDp, bottom = commonListBottomPadding),
                        scrollUnderHeader = commonListHeaderCollapseEnabled,
                        cardAnimationEnabled = homeSettings.cardAnimationEnabled,
                        cardTransitionEnabled = homeSettings.cardTransitionEnabled,
                        cardMotionTier = cardMotionTier,
                        showOnlineCount = showOnlineCount,
                        videoCardAppearance = videoCardAppearance,
                        onVideoClick = { bvid, cid, coverUrl, isVertical ->
                            onVideoClick(bvid, cid, coverUrl, isVertical)
                        },
                        onCollectionClick = onCollectionClick,
                        onRetry = { favoriteViewModel.searchVideos(searchQuery, favoriteSearchScope) },
                        onLoadMore = {},
                        onUnfavorite = { favoriteViewModel.removeVideo(it) },
                        onUpClick = onUpClick,
                        gridState = primaryGridState,
                    )
                } else if (favoriteViewModel != null && favoriteSection != FavoriteSection.VIDEO) {
                    FavoriteCategoryRoute(
                        section = favoriteSection,
                        query = searchQuery,
                        contentPadding = PaddingValues(
                            top = headerHeightDp,
                            bottom = commonListBottomPadding,
                        ),
                        onBangumiClick = onFavoriteBangumiClick,
                        onArticleClick = onFavoriteArticleClick,
                        onTopicClick = onFavoriteTopicClick,
                        onWebClick = onFavoriteWebClick,
                    )
                } else if (isSubscribedBrowse) {
                    val favoriteVm = requireNotNull(favoriteViewModel)
                    FavoriteSubscribedFolderList(
                        folders = filterFavoriteFoldersByQuery(subscribedFoldersState, searchQuery),
                        searchQuery = searchQuery,
                        padding = PaddingValues(
                            top = headerHeightDp,
                            bottom = commonListBottomPadding
                        ),
                        listState = subscribedFolderListState,
                        spacing = spacing.medium,
                        hasMore = subscribedFolderProgressState.hasMore,
                        isLoadingMore = subscribedFolderProgressState.isLoadingMore,
                        transitionEnabled = favoriteCollectionSharedTransitionEnabled,
                        onLoadMore = { favoriteVm.loadMoreSubscribedFolders() },
                        onFolderClick = { folder ->
                            val collectionRoute = resolveSubscribedFavoriteFolderRoute(folder)
                            if (collectionRoute != null) {
                                onCollectionClick?.invoke(collectionRoute)
                            } else {
                                onFavoriteFolderClick?.invoke(
                                    resolveFavoriteFolderMediaId(folder),
                                    folder.mid,
                                    folder.title,
                                    folder.upper?.name.orEmpty()
                                )
                            }
                        }
                    )
                } else when (favoriteContentMode) {
                    FavoriteContentMode.PAGER -> {
                        val favoriteVm = requireNotNull(favoriteViewModel)
                        // Personal-list pages use explicit controls for horizontal navigation.
                        // Keeping this pager programmatic avoids competing with predictive back
                        // and with filter/folder controls in the collapsing header.
                        LaunchedEffect(selectedFolderIndex) {
                            if (pagerState.currentPage != selectedFolderIndex) {
                                pagerState.animateScrollToPage(selectedFolderIndex)
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = false,
                            modifier = Modifier.fillMaxSize(),
                            beyondViewportPageCount = 1 // 预加载
                        ) { page ->
                            // 获取当前页面的状态
                            val folderUiState by favoriteVm.getFolderUiState(page).collectAsStateWithLifecycle()

                            // 确保数据加载
                            LaunchedEffect(page) {
                                favoriteVm.loadFolder(page)
                            }

                            // 渲染通用列表内容 (复用下方逻辑，提取为组件)
                            CommonListContent(
                                items = folderUiState.items,
                                isLoading = folderUiState.isLoading,
                                error = folderUiState.error,
                                searchQuery = searchQuery,
                                columns = personalListColumns,
                                isFavoritePersonalList = true,
                                favoriteBatchMode = isFavoriteBatchMode && page == selectedFolderIndex,
                                favoriteSelectedResourceIds = selectedFavoriteResourceIds,
                                onFavoriteToggleSelect = toggleFavoriteResourceSelection,
                                onFavoriteLongPress = enterFavoriteBatchMode,
                                spacing = spacing.medium,
                                padding = PaddingValues(top = headerHeightDp, bottom = commonListBottomPadding),
                                scrollUnderHeader = commonListHeaderCollapseEnabled,
                                cardAnimationEnabled = homeSettings.cardAnimationEnabled,
                                cardTransitionEnabled = homeSettings.cardTransitionEnabled,
                                cardMotionTier = cardMotionTier,
                                showOnlineCount = showOnlineCount,
                                videoCardAppearance = videoCardAppearance,
                                onVideoClick = { bvid, cid, coverUrl, isVertical ->
                                    playFavoriteVideo(folderUiState.items, bvid, cid, coverUrl, page, false)
                                },
                                onCollectionClick = onCollectionClick,
                                onRetry = { favoriteVm.retryFolder(page) },
                                onLoadMore = { favoriteVm.loadMoreForFolder(page) },
                                onUnfavorite = if (folderUiState.canRemoveItems) {
                                    { video -> favoriteVm.removeVideo(video) }
                                } else {
                                    null
                                },
                                onUpClick = onUpClick,
                                gridState = favoritePagerGridStates.getOrPut(page) {
                                    androidx.compose.foundation.lazy.grid.LazyGridState()
                                }
                            )
                        }
                    }

                    FavoriteContentMode.SINGLE_FOLDER -> {
                        val favoriteVm = requireNotNull(favoriteViewModel)
                        val folderUiState by favoriteVm.getFolderUiState(0).collectAsStateWithLifecycle()
                        LaunchedEffect(favoriteVm) {
                            favoriteVm.loadFolder(0)
                        }
                        CommonListContent(
                            items = folderUiState.items,
                            isLoading = folderUiState.isLoading,
                            error = folderUiState.error,
                            searchQuery = searchQuery,
                            columns = personalListColumns,
                            isFavoritePersonalList = true,
                            favoriteBatchMode = isFavoriteBatchMode,
                            favoriteSelectedResourceIds = selectedFavoriteResourceIds,
                            onFavoriteToggleSelect = toggleFavoriteResourceSelection,
                            onFavoriteLongPress = enterFavoriteBatchMode,
                            spacing = spacing.medium,
                            padding = PaddingValues(top = headerHeightDp, bottom = commonListBottomPadding),
                            scrollUnderHeader = commonListHeaderCollapseEnabled,
                            cardAnimationEnabled = homeSettings.cardAnimationEnabled,
                            cardTransitionEnabled = homeSettings.cardTransitionEnabled,
                            cardMotionTier = cardMotionTier,
                            showOnlineCount = showOnlineCount,
                            videoCardAppearance = videoCardAppearance,
                            onVideoClick = { bvid, cid, coverUrl, _ ->
                                playFavoriteVideo(folderUiState.items, bvid, cid, coverUrl, 0, false)
                            },
                            onCollectionClick = onCollectionClick,
                            onRetry = { favoriteVm.retryFolder(0) },
                            onLoadMore = { favoriteVm.loadMoreForFolder(0) },
                            onUnfavorite = if (folderUiState.canRemoveItems) {
                                { video -> favoriteVm.removeVideo(video) }
                            } else {
                                null
                            },
                            onUpClick = onUpClick,
                            gridState = primaryGridState
                        )
                    }

                    FavoriteContentMode.BASE_LIST -> CommonListContent(
                        items = if (historyViewModel != null) visibleHistoryItems else state.items,
                        isLoading = state.isLoading,
                        error = state.error,
                        searchQuery = searchQuery,
                        columns = if (historyViewModel != null || favoriteViewModel != null) {
                            personalListColumns
                        } else {
                            columns
                        },
                        isFavoritePersonalList = favoriteViewModel != null,
                        favoriteBatchMode = favoriteViewModel != null && isFavoriteBatchMode,
                        favoriteSelectedResourceIds = selectedFavoriteResourceIds,
                        onFavoriteToggleSelect = if (favoriteViewModel != null) toggleFavoriteResourceSelection else null,
                        onFavoriteLongPress = if (favoriteViewModel != null) enterFavoriteBatchMode else null,
                        spacing = spacing.medium,
                        padding = PaddingValues(top = headerHeightDp, bottom = commonListBottomPadding),
                        scrollUnderHeader = commonListHeaderCollapseEnabled,
                        cardAnimationEnabled = homeSettings.cardAnimationEnabled,
                        cardTransitionEnabled = homeSettings.cardTransitionEnabled,
                        cardMotionTier = cardMotionTier,
                        showOnlineCount = showOnlineCount,
                        videoCardAppearance = videoCardAppearance,
                        homeDurationStyle = homeSettings.homeDurationStyle,
                        onVideoClick = { bvid, cid, coverUrl, isVertical ->
                            if (shouldUseFavoritePlaybackQueue) {
                                playFavoriteVideo(state.items, bvid, cid, coverUrl, null, false)
                            } else {
                                onVideoClick(bvid, cid, coverUrl, isVertical)
                            }
                        },
                        onCollectionClick = onCollectionClick,
                        onRetry = favoriteViewModel?.let { favoriteVm ->
                            { favoriteVm.loadData() }
                        },
                        onLoadMore = {
                            when (loadMoreOwner) {
                                CommonListLoadMoreOwner.FAVORITE -> favoriteViewModel?.loadMore()
                                CommonListLoadMoreOwner.HISTORY -> historyViewModel?.loadMore()
                                CommonListLoadMoreOwner.SEASON_SERIES_DETAIL -> seasonSeriesDetailViewModel?.loadMore()
                                CommonListLoadMoreOwner.NONE -> Unit
                            }
                        },
                        onUnfavorite = if (favoriteViewModel != null) {
                            { favoriteViewModel.removeVideo(it) }
                        } else null,
                        onUpClick = if (!isHistoryBatchMode) {
                            onUpClick
                        } else {
                            null
                        },
                        searchPaginationFallbackEnabled = historyViewModel != null,
                        hasMoreSearchResults = historyHasMore,
                        isLoadingMoreSearchResults = historyIsLoadingMore,
                        historyDeleteSession = historyDeleteSession,
                        historyBatchMode = historyViewModel != null && isHistoryBatchMode,
                        historySelectedKeys = selectedHistoryKeys,
                        resolveHistoryItemKey = if (historyViewModel != null) {
                            { video -> historyViewModel.resolveHistoryRenderKey(video) }
                        } else {
                            { video -> video.bvid.ifBlank { video.id.toString() } }
                        },
                        resolveHistoryLookupKey = historyViewModel?.let { vm ->
                            { video -> vm.resolveHistoryLookupKey(video) }
                        },
                        resolveHistoryItem = historyViewModel?.let { vm ->
                            { video -> vm.getHistoryItem(vm.resolveHistoryLookupKey(video)) }
                        },
                        onHistoryLongDelete = if (historyViewModel != null) {
                            { key ->
                                if (!isHistoryBatchMode) {
                                    isHistoryBatchMode = true
                                    selectedHistoryKeys = key.takeIf { it.isNotBlank() }
                                        ?.let(::setOf)
                                        .orEmpty()
                                }
                            }
                        } else null,
                        onHistoryDelete = if (historyViewModel != null) {
                            { key -> pendingHistorySingleDeleteKey = key.takeIf { it.isNotBlank() } }
                        } else null,
                        onHistoryAddToWatchLater = historyViewModel?.let { vm ->
                            { item -> vm.addToWatchLater(item) }
                        },
                        onHistoryDissolveComplete = if (historyViewModel != null) {
                            { key -> historyViewModel.completeVideoDissolve(key) }
                        } else null,
                        onHistoryToggleSelect = if (historyViewModel != null) {
                            { key ->
                                if (key.isNotBlank()) {
                                    selectedHistoryKeys = if (key in selectedHistoryKeys) {
                                        selectedHistoryKeys - key
                                    } else {
                                        selectedHistoryKeys + key
                                    }
                                }
                            }
                        } else null,
                        gridState = primaryGridState
                    )
                }
            }

            progressBadge?.let { badge ->
                FavoriteProgressBadgeCapsule(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = AppSpacingTokens.Medium)
                        .zIndex(2f),
                    title = "进度",
                    badge = badge
                )
            }

            // 2. 顶层：悬浮顶栏 (使用 onGloballyPositioned 测量高度)
            Box(
                modifier = Modifier
                    .zIndex(1f)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        translationY = commonListHeaderOffsetPx
                    }
                    .then(topBarBackgroundModifier)
                    .onGloballyPositioned { coordinates ->
                        headerHeightPx = coordinates.size.height
                    }
            ) {
                Column {
                    AppTopBar(
                        title = state.title,
                        modifier = Modifier.favoriteCollectionSharedBounds(
                            route = favoriteCollectionSharedElementRoute,
                            transitionEnabled = favoriteCollectionSharedTransitionEnabled
                        ),
                        navigationIcon = {
                            AppIconButton(onClick = onBack) {
                                AppIcon(rememberAppBackIcon(), contentDescription = "Back")
                            }
                        },
                        actions = {
                            onOpenSearchDestination?.let { openSearch ->
                                AppIconButton(onClick = { openSearch(searchQuery) }) {
                                    AppIcon(Icons.Rounded.Search, contentDescription = "搜索")
                                }
                            }
                            if (favoriteViewModel != null && favoriteSection == FavoriteSection.VIDEO) {
                                if (isFavoriteBatchMode) {
                                    val selectableIds = activeFavoriteItems
                                        .filterNot { it.isCollectionResource }
                                        .map { it.id }
                                        .filter { it > 0L }
                                        .toSet()
                                    val allSelected = selectableIds.isNotEmpty() &&
                                        selectedFavoriteResourceIds.containsAll(selectableIds)
                                    AppTextButton(
                                        onClick = {
                                            selectedFavoriteResourceIds = if (allSelected) emptySet() else selectableIds
                                        }
                                    ) {
                                        AppText(if (allSelected) "取消全选" else "全选")
                                    }
                                    Box {
                                        AppIconButton(
                                            enabled = selectedFavoriteResourceIds.isNotEmpty() && !isFavoriteManaging,
                                            onClick = { showFavoriteBatchMenu = true },
                                        ) {
                                            AppIcon(Icons.Filled.MoreVert, contentDescription = "批量操作")
                                        }
                                        AppDropdownMenu(
                                            expanded = showFavoriteBatchMenu,
                                            onDismissRequest = { showFavoriteBatchMenu = false },
                                        ) {
                                            AppDropdownMenuItem(
                                                text = { AppText("复制到收藏夹") },
                                                onClick = {
                                                    showFavoriteBatchMenu = false
                                                    pendingFavoriteTransferCopy = true
                                                },
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("移动到收藏夹") },
                                                onClick = {
                                                    showFavoriteBatchMenu = false
                                                    pendingFavoriteTransferCopy = false
                                                },
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("删除", color = MaterialTheme.colorScheme.error) },
                                                onClick = {
                                                    showFavoriteBatchMenu = false
                                                    showFavoriteBatchDeleteConfirm = true
                                                },
                                            )
                                        }
                                    }
                                    AppTextButton(
                                        onClick = {
                                            isFavoriteBatchMode = false
                                            selectedFavoriteResourceIds = emptySet()
                                        }
                                    ) {
                                        AppText("完成")
                                    }
                                } else {
                                AppIconButton(
                                    enabled = activeFavoriteItems.any { it.bvid.isNotBlank() } && !isSubscribedBrowse,
                                    onClick = {
                                        activeFavoriteItems.firstOrNull { it.bvid.isNotBlank() }?.let { first ->
                                            playFavoriteVideo(
                                                activeFavoriteItems,
                                                first.bvid,
                                                first.cid,
                                                first.pic,
                                                selectedFolderIndex,
                                                false,
                                            )
                                        }
                                    }
                                ) {
                                    AppIcon(
                                        imageVector = rememberAppPlayIcon(),
                                        contentDescription = "播放全部",
                                    )
                                }
                                AppIconButton(
                                    enabled = activeFavoriteItems.isNotEmpty() && !isSubscribedBrowse,
                                    onClick = {
                                        playFavoriteVideo(
                                            activeFavoriteItems,
                                            activeFavoriteItems.firstOrNull()?.bvid.orEmpty(),
                                            activeFavoriteItems.firstOrNull()?.cid ?: 0L,
                                            activeFavoriteItems.firstOrNull()?.pic.orEmpty(),
                                            if (!isSubscribedBrowse) {
                                                selectedFolderIndex
                                            } else {
                                                null
                                            },
                                            true
                                        )
                                    }
                                ) {
                                    AppIcon(
                                        imageVector = rememberAppHeadphonesIcon(),
                                        contentDescription = "全部听"
                                    )
                                }

                                if (!isSubscribedBrowse) {
                                    Box {
                                        AppIconButton(
                                            enabled = !isFavoriteManaging,
                                            onClick = { showFavoriteManagementMenu = true }
                                        ) {
                                            AppIcon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "更多管理"
                                            )
                                        }
                                        AppDropdownMenu(
                                            expanded = showFavoriteManagementMenu,
                                            onDismissRequest = { showFavoriteManagementMenu = false }
                                        ) {
                                            AppDropdownMenuItem(
                                                text = { AppText("新建收藏夹") },
                                                enabled = !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    favoriteFolderEditorMode = "create"
                                                    favoriteFolderEditorTitle = ""
                                                    favoriteFolderEditorIntro = ""
                                                    favoriteFolderEditorPrivate = false
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("编辑收藏夹") },
                                                enabled = selectedFavoriteFolder != null && !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    selectedFavoriteFolder?.let { folder ->
                                                        favoriteFolderEditorMode = "edit"
                                                        favoriteFolderEditorTitle = folder.title
                                                        favoriteFolderEditorIntro = folder.intro
                                                        favoriteFolderEditorPrivate = folder.attr != 0
                                                    }
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("删除收藏夹", color = MaterialTheme.colorScheme.error) },
                                                enabled = selectedFolderIndex > 0 && !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    showFavoriteFolderDeleteConfirm = true
                                                }
                                            )
                                            AppHorizontalDivider()
                                            FavoriteResourceOrder.entries.forEach { order ->
                                                AppDropdownMenuItem(
                                                    text = {
                                                        AppText(
                                                            if (order == favoriteOrder) {
                                                                "排序：${order.label}"
                                                            } else {
                                                                order.label
                                                            }
                                                        )
                                                    },
                                                    enabled = !isFavoriteManaging,
                                                    onClick = {
                                                        showFavoriteManagementMenu = false
                                                        favoriteViewModel.changeFavoriteOrder(order)
                                                    }
                                                )
                                            }
                                            AppHorizontalDivider()
                                            AppDropdownMenuItem(
                                                text = { AppText("分享收藏夹") },
                                                enabled = selectedFavoriteFolder?.attr == 0 && !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    selectedFavoriteFolder?.let { folder ->
                                                        com.android.purebilibili.core.util.ShareUtils.shareText(
                                                            context = context,
                                                            subject = folder.title,
                                                            text = "https://www.bilibili.com/medialist/detail/ml${resolveFavoriteFolderMediaId(folder)}",
                                                            chooserTitle = "分享收藏夹",
                                                        )
                                                    }
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("分享至动态") },
                                                enabled = selectedFavoriteFolder?.attr == 0 && !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    showFavoriteDynamicShareConfirm = true
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("清理失效内容") },
                                                enabled = canCleanInvalidFavoriteResources(selectedFavoriteFolder) && !isFavoriteManaging,
                                                onClick = {
                                                    showFavoriteManagementMenu = false
                                                    showFavoriteCleanInvalidConfirm = true
                                                }
                                            )
                                        }
                                    }
                                }
                                }
                            }

                            if (historyViewModel != null) {
                                if (isHistoryBatchMode && visibleHistoryItems.isNotEmpty()) {
                                    val visibleHistoryKeys = visibleHistoryItems
                                        .map(historyViewModel::resolveHistoryRenderKey)
                                        .toSet()
                                    val allSelected = visibleHistoryKeys.isNotEmpty() &&
                                        selectedHistoryKeys.containsAll(visibleHistoryKeys)
                                    AppTextButton(
                                        onClick = {
                                            selectedHistoryKeys = if (allSelected) {
                                                emptySet()
                                            } else {
                                                visibleHistoryKeys
                                            }
                                        }
                                    ) {
                                        AppText(if (allSelected) "取消全选" else "全选")
                                    }
                                    AppTextButton(
                                        enabled = selectedHistoryKeys.isNotEmpty(),
                                        onClick = { showHistoryBatchDeleteConfirm = true }
                                    ) {
                                        AppText("删除(${selectedHistoryKeys.size})")
                                    }
                                    AppTextButton(
                                        onClick = {
                                            isHistoryBatchMode = false
                                            selectedHistoryKeys = emptySet()
                                        }
                                    ) {
                                        AppText("完成")
                                    }
                                } else {
                                    if (state.items.isNotEmpty()) {
                                        AppTextButton(
                                            enabled = !isHistoryManagementBusy,
                                            onClick = {
                                                isHistoryBatchMode = true
                                                selectedHistoryKeys = emptySet()
                                            }
                                        ) {
                                            AppText("批量删除")
                                        }
                                    }

                                    Box {
                                        AppIconButton(
                                            enabled = !isHistoryManagementBusy,
                                            onClick = { showHistoryManagementMenu = true }
                                        ) {
                                            AppIcon(
                                                imageVector = Icons.Filled.MoreVert,
                                                contentDescription = "更多管理"
                                            )
                                        }
                                        AppDropdownMenu(
                                            expanded = showHistoryManagementMenu,
                                            onDismissRequest = { showHistoryManagementMenu = false }
                                        ) {
                                            AppDropdownMenuItem(
                                                text = { AppText(resolveHistoryPauseActionLabel(isHistoryPaused)) },
                                                enabled = !isHistoryManagementBusy,
                                                onClick = {
                                                    showHistoryManagementMenu = false
                                                    historyViewModel.toggleHistoryPause()
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("删除已看记录") },
                                                enabled = state.items.isNotEmpty() && !isHistoryManagementBusy,
                                                onClick = {
                                                    showHistoryManagementMenu = false
                                                    historyViewModel.deleteViewedHistory()
                                                }
                                            )
                                            AppDropdownMenuItem(
                                                text = { AppText("清空历史") },
                                                enabled = state.items.isNotEmpty() && !isHistoryManagementBusy,
                                                onClick = {
                                                    showHistoryManagementMenu = false
                                                    showHistoryClearConfirm = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent
                        ),
                        scrollBehavior = scrollBehavior
                    )

                    // 🔍 搜索栏
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = favoriteHeaderLayout.searchBarHorizontalPaddingDp.dp,
                                vertical = favoriteHeaderLayout.searchBarVerticalPaddingDp.dp
                            )
                    ) {
                        AppSearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = when {
                                isSubscribedBrowse -> "搜索追更"
                                historyViewModel != null -> "搜索历史"
                                favoriteViewModel != null && favoriteSection != FavoriteSection.VIDEO ->
                                    "搜索${favoriteSection.label}收藏"
                                else -> "搜索视频"
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                            heightOverride = favoriteHeaderLayout.searchBarHeightDp.dp
                        )
                    }

                    if (favoriteViewModel != null) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacingTokens.Medium),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                        ) {
                            FavoriteSection.entries.forEach { section ->
                                AppFilterChip(
                                    selected = favoriteSection == section,
                                    onClick = {
                                        if (favoriteSection != section) {
                                            favoriteSection = section
                                            favoriteBrowseSection = FavoriteBrowseSection.OWNED
                                            searchQuery = ""
                                            isFavoriteBatchMode = false
                                            selectedFavoriteResourceIds = emptySet()
                                        }
                                    },
                                    label = { AppText(section.label) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                    }

                    if (
                        favoriteViewModel != null &&
                        isSearchDestination &&
                        favoriteSection == FavoriteSection.VIDEO
                    ) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacingTokens.Medium),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                        ) {
                            FavoriteSearchScope.entries.forEach { scopeOption ->
                                AppFilterChip(
                                    selected = favoriteSearchScope == scopeOption,
                                    onClick = { favoriteSearchScope = scopeOption },
                                    label = { AppText(scopeOption.label) },
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                    }

                    if (
                        favoriteViewModel != null &&
                        favoriteSection == FavoriteSection.VIDEO &&
                        !isSubscribedBrowse &&
                        foldersState.isNotEmpty()
                    ) {
                        FavoriteFolderSelector(
                            folders = foldersState,
                            selectedFolderIndex = selectedFolderIndex,
                            selectedFolderItems = selectedFolderUiState.items,
                            layout = favoriteHeaderLayout,
                            onFolderSelected = { index ->
                                favoriteViewModel.switchFolder(index)
                                searchQuery = ""
                            },
                        )
                    }

                    if (historyViewModel != null) {
                        if (isHistoryPaused) {
                            AppSurface(
                                onClick = historyViewModel::toggleHistoryPause,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = AppSpacingTokens.Medium),
                                shape = AppShapes.container(ContainerLevel.Pill),
                                color = MaterialTheme.colorScheme.errorContainer,
                            ) {
                                AppText(
                                    text = "历史记录功能已关闭 · 点击开启",
                                    modifier = Modifier.padding(AppSpacingTokens.Medium),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                        }
                        val historyFilterLabels = remember {
                            HistoryContentFilter.entries.map { it.label }
                        }
                        val selectedHistoryFilterIndex = remember(historyContentFilter) {
                            HistoryContentFilter.entries.indexOf(historyContentFilter).coerceAtLeast(0)
                        }
                        val onHistoryFilterSelected: (HistoryContentFilter) -> Unit = { filter ->
                            if (filter != historyContentFilter) {
                                historyContentFilter = filter
                                selectedHistoryKeys = emptySet()
                                scope.launch {
                                    primaryGridState.scrollToItem(0)
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = historyFilterChrome.horizontalPaddingDp.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (historyFilterChrome.useLiquidDock) {
                                BottomBarLiquidSegmentedControl(
                                    items = historyFilterLabels,
                                    selectedIndex = selectedHistoryFilterIndex,
                                    onSelected = { index ->
                                        HistoryContentFilter.entries.getOrNull(index)?.let(onHistoryFilterSelected)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isHistoryBatchMode,
                                    itemWidth = historyFilterChrome.itemWidthDp?.dp,
                                    height = historyFilterChrome.heightDp.dp,
                                    indicatorHeight = historyFilterChrome.indicatorHeightDp.dp,
                                    labelFontSize = historyFilterChrome.labelFontSizeSp.sp,
                                    backdrop = commonListChromeBackdrop,
                                    forceLiquidChrome = homeSettings.androidNativeLiquidGlassEnabled,
                                    liquidGlassEffectsEnabled = true,
                                    dragSelectionEnabled = historyFilterChrome.dragSelectionEnabled,
                                    tapPressRefractionEnabled = true,
                                    isScrollInProgressProvider = {
                                        primaryGridState.isScrollInProgress
                                    }
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(
                                        space = AppSpacingTokens.Small,
                                        alignment = Alignment.CenterHorizontally
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                                ) {
                                    HistoryContentFilter.entries.forEach { filter ->
                                        AppFilterChip(
                                            selected = historyContentFilter == filter,
                                            enabled = !isHistoryBatchMode,
                                            onClick = { onHistoryFilterSelected(filter) },
                                            label = {
                                                AppText(
                                                    text = filter.label,
                                                    style = MaterialTheme.typography.labelLarge.copy(
                                                        fontSize = historyFilterChrome.labelFontSizeSp.sp
                                                    )
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = AppSurfaceTokens.cardContainer(),
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                    }

                    if (favoriteViewModel != null) {
                        Spacer(modifier = Modifier.height(favoriteHeaderLayout.headerBottomPaddingDp.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = shouldShowBackToTop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall, bottom = commonListBottomPadding + AppSpacingTokens.Medium),
                enter = androidx.compose.animation.fadeIn(animationSpec = AppMotionTokens.standardSpec()) +
                    androidx.compose.animation.scaleIn(initialScale = 0.92f),
                exit = androidx.compose.animation.fadeOut(animationSpec = AppMotionTokens.standardSpec()) +
                    androidx.compose.animation.scaleOut(targetScale = 0.92f)
            ) {
                AppSmallFloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            scrollCommonListToTop()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    AppIcon(
                        imageVector = rememberAppChevronUpIcon(),
                        contentDescription = "回到顶部"
                    )
                }
            }
        }
    }

    if (showHistoryBatchDeleteConfirm && historyViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showHistoryBatchDeleteConfirm = false },
            title = { AppText("批量删除历史") },
            text = { AppText("确认删除已选择的 ${selectedHistoryKeys.size} 条历史记录吗？") },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        val targetKeys = selectedHistoryKeys
                        when (resolveHistoryDeleteAnimationMode(targetKeys.size)) {
                            HistoryDeleteAnimationMode.SINGLE_DISSOLVE -> {
                                targetKeys.firstOrNull()?.let(historyViewModel::startVideoDissolve)
                            }
                            HistoryDeleteAnimationMode.DIRECT_DELETE -> {
                                // Batch selection may include off-screen items that never report animation completion.
                                historyViewModel.deleteHistoryItems(targetKeys)
                            }
                        }
                        selectedHistoryKeys = emptySet()
                        isHistoryBatchMode = false
                        showHistoryBatchDeleteConfirm = false
                    }
                ) {
                    AppText("删除")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showHistoryBatchDeleteConfirm = false }) {
                    AppText("取消")
                }
            }
        )
    }

    if (showFavoriteCleanInvalidConfirm && favoriteViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showFavoriteCleanInvalidConfirm = false },
            title = { AppText("清理失效内容") },
            text = {
                AppText(
                    resolveFavoriteCleanInvalidConfirmText(
                        selectedFavoriteFolder?.title.orEmpty()
                    )
                )
            },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        favoriteViewModel.cleanInvalidResourcesInSelectedFolder()
                        showFavoriteCleanInvalidConfirm = false
                    }
                ) {
                    AppText("清理")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showFavoriteCleanInvalidConfirm = false }) {
                    AppText("取消")
                }
            }
        )
    }

    favoriteFolderEditorMode?.let { mode ->
        AppAlertDialog(
            onDismissRequest = { favoriteFolderEditorMode = null },
            title = { AppText(if (mode == "create") "新建收藏夹" else "编辑收藏夹") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)) {
                    AppTextField(
                        value = favoriteFolderEditorTitle,
                        onValueChange = { favoriteFolderEditorTitle = it },
                        label = "名称",
                        placeholder = "收藏夹名称",
                    )
                    AppTextField(
                        value = favoriteFolderEditorIntro,
                        onValueChange = { favoriteFolderEditorIntro = it },
                        label = "简介",
                        placeholder = "可选",
                        singleLine = false,
                        minLines = 2,
                        maxLines = 4,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        AppText("设为私密")
                        AppSwitch(
                            checked = favoriteFolderEditorPrivate,
                            onCheckedChange = { favoriteFolderEditorPrivate = it },
                        )
                    }
                }
            },
            confirmButton = {
                AppTextButton(
                    enabled = favoriteFolderEditorTitle.isNotBlank() && isFavoriteManaging.not(),
                    onClick = {
                        if (mode == "create") {
                            favoriteViewModel?.createFavoriteFolder(
                                title = favoriteFolderEditorTitle,
                                intro = favoriteFolderEditorIntro,
                                isPrivate = favoriteFolderEditorPrivate,
                            )
                        } else {
                            favoriteViewModel?.editSelectedFavoriteFolder(
                                title = favoriteFolderEditorTitle,
                                intro = favoriteFolderEditorIntro,
                                isPrivate = favoriteFolderEditorPrivate,
                            )
                        }
                        favoriteFolderEditorMode = null
                    },
                ) {
                    AppText("保存")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { favoriteFolderEditorMode = null }) {
                    AppText("取消")
                }
            },
        )
    }

    if (showFavoriteFolderDeleteConfirm && favoriteViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showFavoriteFolderDeleteConfirm = false },
            title = { AppText("删除收藏夹") },
            text = { AppText("确认删除“${selectedFavoriteFolder?.title.orEmpty()}”吗？收藏内容不会从站点删除。") },
            confirmButton = {
                AppTextButton(
                    enabled = selectedFolderIndex > 0 && !isFavoriteManaging,
                    onClick = {
                        favoriteViewModel.deleteSelectedFavoriteFolder()
                        showFavoriteFolderDeleteConfirm = false
                    },
                ) {
                    AppText("删除")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showFavoriteFolderDeleteConfirm = false }) {
                    AppText("取消")
                }
            },
        )
    }

    if (showFavoriteDynamicShareConfirm && favoriteViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showFavoriteDynamicShareConfirm = false },
            title = { AppText("分享至动态") },
            text = {
                AppText("将“${selectedFavoriteFolder?.title.orEmpty()}”作为收藏夹卡片发布到动态？")
            },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        favoriteViewModel.shareSelectedFolderToDynamic(
                            content = "分享收藏夹：${selectedFavoriteFolder?.title.orEmpty()}",
                        )
                        showFavoriteDynamicShareConfirm = false
                    }
                ) {
                    AppText("发布")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showFavoriteDynamicShareConfirm = false }) {
                    AppText("取消")
                }
            },
        )
    }

    if (showFavoriteBatchDeleteConfirm && favoriteViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showFavoriteBatchDeleteConfirm = false },
            title = { AppText("批量删除收藏内容") },
            text = { AppText("确认移除已选择的 ${selectedFavoriteResourceIds.size} 个内容吗？") },
            confirmButton = {
                AppTextButton(
                    enabled = selectedFavoriteResourceIds.isNotEmpty(),
                    onClick = {
                        favoriteViewModel.deleteSelectedFavoriteResources(selectedFavoriteResourceIds)
                        selectedFavoriteResourceIds = emptySet()
                        isFavoriteBatchMode = false
                        showFavoriteBatchDeleteConfirm = false
                    },
                ) {
                    AppText("删除")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showFavoriteBatchDeleteConfirm = false }) {
                    AppText("取消")
                }
            },
        )
    }

    pendingFavoriteTransferCopy?.let { copy ->
        AppAlertDialog(
            onDismissRequest = { pendingFavoriteTransferCopy = null },
            title = { AppText(if (copy) "复制到收藏夹" else "移动到收藏夹") },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                ) {
                    items(
                        items = foldersState.filterIndexed { index, _ -> index != selectedFolderIndex },
                        key = { folder -> resolveFavoriteFolderMediaId(folder) },
                    ) { folder ->
                        AppSurface(
                            onClick = {
                                favoriteViewModel?.copyOrMoveSelectedFavoriteResources(
                                    resourceIds = selectedFavoriteResourceIds,
                                    targetMediaId = resolveFavoriteFolderMediaId(folder),
                                    copy = copy,
                                )
                                selectedFavoriteResourceIds = emptySet()
                                isFavoriteBatchMode = false
                                pendingFavoriteTransferCopy = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = AppShapes.container(ContainerLevel.Card),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
                        ) {
                            AppText(
                                text = folder.title,
                                modifier = Modifier.padding(AppSpacingTokens.Medium),
                            )
                        }
                    }
                }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingFavoriteTransferCopy = null }) {
                    AppText("取消")
                }
            },
        )
    }

    if (showHistoryClearConfirm && historyViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { showHistoryClearConfirm = false },
            title = { AppText("清空历史") },
            text = { AppText(resolveHistoryClearConfirmText(state.items.size)) },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        historyViewModel.clearAllHistory()
                        selectedHistoryKeys = emptySet()
                        isHistoryBatchMode = false
                        showHistoryClearConfirm = false
                    }
                ) {
                    AppText("清空")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showHistoryClearConfirm = false }) {
                    AppText("取消")
                }
            }
        )
    }

    if (pendingHistorySingleDeleteKey != null && historyViewModel != null) {
        AppAlertDialog(
            onDismissRequest = { pendingHistorySingleDeleteKey = null },
            title = { AppText("删除历史记录") },
            text = { AppText("确认删除这条历史记录吗？") },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        pendingHistorySingleDeleteKey?.let { historyViewModel.startVideoDissolve(it) }
                        pendingHistorySingleDeleteKey = null
                    }
                ) {
                    AppText("删除")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingHistorySingleDeleteKey = null }) {
                    AppText("取消")
                }
            }
        )
    }
}

@Composable
private fun FavoriteFolderSelector(
    folders: List<com.android.purebilibili.data.model.response.FavFolder>,
    selectedFolderIndex: Int,
    selectedFolderItems: List<com.android.purebilibili.data.model.response.VideoItem>,
    layout: CommonListFavoriteHeaderLayout,
    onFolderSelected: (Int) -> Unit,
) {
    val selectedFolder = folders.getOrNull(selectedFolderIndex) ?: return
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
    val selectedPreviewCover = remember(selectedFolder.cover, selectedFolderItems) {
        resolveFavoriteFolderPreviewCover(
            folder = selectedFolder,
            loadedItems = selectedFolderItems,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = layout.folderChipRowHorizontalPaddingDp.dp,
                end = layout.folderChipRowHorizontalPaddingDp.dp,
                top = layout.folderChipRowTopPaddingDp.dp,
            ),
    ) {
        AppSurface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.container(ContainerLevel.Pill),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = AppChromeSizeTokens.MinimumTouchTarget)
                    .padding(horizontal = layout.folderChipHorizontalPaddingDp.dp),
                horizontalArrangement = Arrangement.spacedBy(layout.folderChipSpacingDp.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FavoriteFolderChipPreview(
                    coverUrl = selectedPreviewCover,
                    selected = true,
                )
                AppText(
                    text = selectedFolder.title,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                AppText(
                    text = "${selectedFolderIndex + 1}/${folders.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppIcon(
                    imageVector = rememberAppChevronDownIcon(),
                    contentDescription = "切换收藏夹",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        AppDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 280.dp, max = 420.dp),
        ) {
            folders.forEachIndexed { index, folder ->
                val isSelected = index == selectedFolderIndex
                AppDropdownMenuItem(
                    text = {
                        AppText(
                            text = folder.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = {
                        FavoriteFolderChipPreview(
                            coverUrl = resolveFavoriteFolderPreviewCover(
                                folder = folder,
                                loadedItems = if (isSelected) selectedFolderItems else emptyList(),
                            ),
                            selected = isSelected,
                        )
                    },
                    trailingIcon = if (isSelected) {
                        {
                            AppIcon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "当前收藏夹",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onFolderSelected(index)
                    },
                )
            }
        }
    }
}

@Composable
private fun FavoriteFolderChipPreview(
    coverUrl: String?,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .size(AppSpacingTokens.ExtraLarge)
            .clip(AppShapes.container(ContainerLevel.Chip))
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(coverUrl),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AppIcon(
                imageVector = rememberAppFolderIcon(),
                contentDescription = null,
                modifier = Modifier.size(AppSpacingTokens.Large - AppSpacingTokens.Micro / 2),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

// 提取通用列表内容组件
@Composable
private fun CommonListContent(
    items: List<com.android.purebilibili.data.model.response.VideoItem>,
    isLoading: Boolean,
    error: String?,
    searchQuery: String,
    columns: Int,
    isFavoritePersonalList: Boolean = false,
    favoriteBatchMode: Boolean = false,
    favoriteSelectedResourceIds: Set<Long> = emptySet(),
    onFavoriteToggleSelect: ((Long) -> Unit)? = null,
    onFavoriteLongPress: ((Long) -> Unit)? = null,
    spacing: androidx.compose.ui.unit.Dp,
    padding: PaddingValues,
    scrollUnderHeader: Boolean = false,
    cardAnimationEnabled: Boolean,
    cardTransitionEnabled: Boolean,
    cardMotionTier: MotionTier,
    showOnlineCount: Boolean,
    videoCardAppearance: CommonListVideoCardAppearance,
    homeDurationStyle: HomeDurationStyle = HomeDurationStyle.OUTSIDE_COVER,
    onVideoClick: (String, Long, String, Boolean) -> Unit,
    onCollectionClick: ((FavoriteCollectionRoute) -> Unit)? = null,
    onRetry: (() -> Unit)? = null,
    onLoadMore: () -> Unit,
    onUnfavorite: ((com.android.purebilibili.data.model.response.VideoItem) -> Unit)?,
    historyDeleteSession: HistoryDeleteSession? = null,
    historyBatchMode: Boolean = false,
    historySelectedKeys: Set<String> = emptySet(),
    resolveHistoryItemKey: (com.android.purebilibili.data.model.response.VideoItem) -> String = { video ->
        video.bvid.ifBlank { video.id.toString() }
    },
    resolveHistoryLookupKey: ((com.android.purebilibili.data.model.response.VideoItem) -> String)? = null,
    resolveHistoryItem: ((com.android.purebilibili.data.model.response.VideoItem) -> HistoryItem?)? = null,
    onHistoryLongDelete: ((String) -> Unit)? = null,
    onHistoryDelete: ((String) -> Unit)? = null,
    onHistoryAddToWatchLater: ((HistoryItem) -> Unit)? = null,
    onHistoryDissolveComplete: ((String) -> Unit)? = null,
    onHistoryToggleSelect: ((String) -> Unit)? = null,
    onUpClick: ((Long) -> Unit)? = null,
    searchPaginationFallbackEnabled: Boolean = false,
    hasMoreSearchResults: Boolean = false,
    isLoadingMoreSearchResults: Boolean = false,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState? = null
) {
    val context = LocalContext.current
    val isHistoryPersonalList = resolveHistoryItem != null
    val isPersonalList = isHistoryPersonalList || isFavoritePersonalList
    val homeFeedCardStyle = if (isPersonalList) {
        HomeFeedCardStyle.CURRENT
    } else {
        SettingsManager
            .getHomeFeedCardStyle(context)
            .collectAsStateWithLifecycle(initialValue = HomeFeedCardStyle.CURRENT)
            .value
    }
    val cardLayout = remember(homeFeedCardStyle) {
        com.android.purebilibili.feature.home.resolveHomeFeedCardLayout(homeFeedCardStyle)
    }
    val gridOuterPaddingDp = if (isPersonalList) 12 else cardLayout.outerPaddingDp
    val gridItemSpacingDp = if (isPersonalList) 12 else cardLayout.itemSpacingDp
    val skeletonCoverAspectRatio = if (isPersonalList) {
        com.android.purebilibili.feature.personal.PERSONAL_LIST_HORIZONTAL_COVER_ASPECT_RATIO
    } else {
        cardLayout.coverAspectRatio
    }
    val resolvedGridState = gridState ?: rememberLazyGridState()
    val fixedHeaderInset = resolveCommonListViewportTopPadding(padding.calculateTopPadding())
    val scrollableHeaderInset = if (scrollUnderHeader) fixedHeaderInset else AppSpacingTokens.None
    val viewportModifier = Modifier
        .fillMaxSize()
        .padding(top = if (scrollUnderHeader) AppSpacingTokens.None else fixedHeaderInset)
    val emptyViewportModifier = Modifier
        .fillMaxSize()
        .padding(top = fixedHeaderInset)
    if (isLoading && items.isEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(
                start = spacing,
                end = spacing,
                top = scrollableHeaderInset + spacing,
                bottom = padding.calculateBottomPadding() + spacing
            ),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalArrangement = Arrangement.spacedBy(spacing),
            modifier = viewportModifier
        ) {
            items(columns * 4, key = { it }) {
                VideoGridItemSkeleton(coverAspectRatio = skeletonCoverAspectRatio)
            }
        }
    } else if (error != null && items.isEmpty()) {
        Column(
            modifier = emptyViewportModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppText(
                text = error,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(AppSpacingTokens.Medium))
                AppButton(onClick = onRetry) {
                    AppText("重试")
                }
            }
        }
    } else if (items.isEmpty()) {
        Box(modifier = emptyViewportModifier, contentAlignment = Alignment.Center) {
             AppText("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val filteredItems = androidx.compose.runtime.remember(items, searchQuery) {
            filterCommonListVideosByQuery(items, searchQuery)
        }
        LaunchedEffect(
            searchPaginationFallbackEnabled,
            searchQuery,
            items.size,
            filteredItems.size,
            hasMoreSearchResults,
            isLoadingMoreSearchResults
        ) {
            if (
                searchPaginationFallbackEnabled &&
                shouldLoadMoreCommonListSearchResults(
                    searchQuery = searchQuery,
                    filteredItemCount = filteredItems.size,
                    hasMore = hasMoreSearchResults,
                    isLoadingMore = isLoadingMoreSearchResults
                )
            ) {
                onLoadMore()
            }
        }

        if (filteredItems.isEmpty() && searchQuery.isNotEmpty()) {
             Box(emptyViewportModifier, contentAlignment = Alignment.Center) {
                AppText("没有找到相关视频", color = MaterialTheme.colorScheme.onSurfaceVariant)
             }
        } else {
            // 自动加载更多
            val shouldLoadMore = androidx.compose.runtime.remember(resolvedGridState) {
                androidx.compose.runtime.derivedStateOf {
                    val layoutInfo = resolvedGridState.layoutInfo
                    val total = layoutInfo.totalItemsCount
                    val last = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    total > 0 && last >= total - 4
                }
            }
            LaunchedEffect(shouldLoadMore.value) {
                if (shouldLoadMore.value) onLoadMore()
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = resolvedGridState,
                contentPadding = PaddingValues(
                    start = gridOuterPaddingDp.dp,
                    end = gridOuterPaddingDp.dp,
                    top = scrollableHeaderInset + gridOuterPaddingDp.dp,
                    bottom = padding.calculateBottomPadding() + gridOuterPaddingDp.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(gridItemSpacingDp.dp),
                verticalArrangement = Arrangement.spacedBy(gridItemSpacingDp.dp),
                modifier = viewportModifier
            ) {
                 itemsIndexed(
                    items = filteredItems,
                    key = { _, item -> resolveHistoryItemKey(item) },
                    span = { _, item ->
                        if (item.isCollectionResource) GridItemSpan(columns) else GridItemSpan(1)
                    }
                ) { index, video ->
                    val historyKey = resolveHistoryItemKey(video)
                    val historyItem = resolveHistoryItem?.invoke(video)
                    val historyCardPresentation = remember(historyItem) {
                        resolveHistoryCardPresentation(historyItem)
                    }
                    val displayedVideo = historyCardPresentation?.videoItem ?: video
                    val supportsHistoryDissolve = onHistoryLongDelete != null && onHistoryDissolveComplete != null
                    val isDissolving = supportsHistoryDissolve &&
                        historyKey in resolveActiveHistoryDeleteKeys(historyDeleteSession)
                    val shouldKeepPlaceholderHidden = supportsHistoryDissolve &&
                        shouldKeepHistoryDeletePlaceholderHidden(historyDeleteSession, historyKey)
                    val isSelected = historyBatchMode && historyKey in historySelectedKeys
                    val historyDeleteAnimationMode = historyDeleteSession?.animationMode
                        ?: HistoryDeleteAnimationMode.SINGLE_DISSOLVE
                    val historySelectionShape = if (historyItem?.business == HistoryBusiness.ARTICLE) {
                        AppShapes.container(ContainerLevel.Sheet)
                    } else {
                        AppShapes.container(ContainerLevel.Card)
                    }

                    val cardContent: @Composable () -> Unit = {
                        Box {
                            if (video.isCollectionResource) {
                                FavoriteCollectionRow(
                                    item = video,
                                    onClick = {
                                        resolveFavoriteCollectionRoute(video)?.let { route ->
                                            onCollectionClick?.invoke(route)
                                        }
                                    }
                                )
                            } else if (historyItem != null) {
                                HistoryPersonalCard(
                                    item = historyItem,
                                    selected = isSelected,
                                    batchMode = historyBatchMode,
                                    transitionEnabled = cardTransitionEnabled,
                                    onClick = {
                                        if (historyBatchMode) {
                                            onHistoryToggleSelect?.invoke(historyKey)
                                        } else {
                                            resolveCommonListVideoNavigationRequest(
                                                video = video,
                                                fallbackLookupKey = resolveHistoryLookupKey?.invoke(video)
                                            )?.let { request ->
                                                onVideoClick(
                                                    request.lookupKey,
                                                    request.cid,
                                                    request.coverUrl,
                                                    request.isVertical
                                                )
                                            }
                                        }
                                    },
                                    onLongClick = { onHistoryLongDelete?.invoke(historyKey) },
                                    onUpClick = historyItem.videoItem.owner.mid
                                        .takeIf { it > 0L }
                                        ?.let { mid -> { onUpClick?.invoke(mid) } },
                                    onAddToWatchLater = historyItem
                                        .takeIf(::canAddHistoryToWatchLater)
                                        ?.let { eligible -> { onHistoryAddToWatchLater?.invoke(eligible) } },
                                    onDelete = { onHistoryDelete?.invoke(historyKey) },
                                )
                            } else if (isFavoritePersonalList) {
                                FavoritePersonalCard(
                                    item = video,
                                    transitionEnabled = cardTransitionEnabled,
                                    batchMode = favoriteBatchMode,
                                    selected = video.id in favoriteSelectedResourceIds,
                                    canRemove = onUnfavorite != null,
                                    onClick = {
                                        if (favoriteBatchMode) {
                                            onFavoriteToggleSelect?.invoke(video.id)
                                        } else {
                                            resolveCommonListVideoNavigationRequest(
                                                video = video,
                                                fallbackLookupKey = resolveHistoryLookupKey?.invoke(video)
                                            )?.let { request ->
                                                onVideoClick(
                                                    request.lookupKey,
                                                    request.cid,
                                                    request.coverUrl,
                                                    request.isVertical
                                                )
                                            }
                                        }
                                    },
                                    onLongClick = { onFavoriteLongPress?.invoke(video.id) },
                                    onRemove = onUnfavorite?.let { remove ->
                                        { remove(video) }
                                    },
                                )
                            } else {
                                ElegantVideoCard(
                                    video = displayedVideo,
                                    index = index,
                                    animationEnabled = cardAnimationEnabled,
                                    motionTier = cardMotionTier,
                                    transitionEnabled = cardTransitionEnabled,
                                    glassEnabled = videoCardAppearance.glassEnabled,
                                    blurEnabled = videoCardAppearance.blurEnabled,
                                    showCoverGlassBadges = videoCardAppearance.showCoverGlassBadges,
                                    showInfoGlassBadges = videoCardAppearance.showInfoGlassBadges,
                                    showUpBadge = historyCardPresentation?.showUpBadge ?: true,
                                    coverAspectRatio = cardLayout.coverAspectRatio,
                                    compactMetadata = cardLayout.compactMetadata,
                                    homeDurationStyle = homeDurationStyle,
                                    showOnlineCount = showOnlineCount,
                                    onClick = { _, _ ->
                                        if (historyBatchMode) {
                                            onHistoryToggleSelect?.invoke(historyKey)
                                        } else {
                                            resolveCommonListVideoNavigationRequest(
                                                video = video,
                                                fallbackLookupKey = resolveHistoryLookupKey?.invoke(video)
                                            )?.let { request ->
                                                onVideoClick(
                                                    request.lookupKey,
                                                    request.cid,
                                                    request.coverUrl,
                                                    request.isVertical
                                                )
                                            }
                                        }
                                    },
                                    onUnfavorite = if (onUnfavorite != null) { { onUnfavorite(video) } } else null,
                                    onUpClick = onUpClick,
                                    onLongClick = if (!historyBatchMode && supportsHistoryDissolve) {
                                        { onHistoryLongDelete(historyKey) }
                                    } else null
                                )
                            }

                            if (historyBatchMode && historyItem == null) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .border(
                                            width = if (isSelected) AppSpacingTokens.Micro else AppSpacingTokens.Micro / 2,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                            },
                                            shape = historySelectionShape
                                        )
                                        .background(
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                            } else {
                                                Color.Transparent
                                            },
                                            shape = historySelectionShape
                                        )
                                )
                                AppIcon(
                                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                    contentDescription = if (isSelected) "已选择" else "未选择",
                                    tint = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(AppSpacingTokens.Small)
                                )
                            }
                        }
                    }

                    if (supportsHistoryDissolve) {
                        MaybeDissolvableVideoCard(
                            isDissolving = isDissolving,
                            onDissolveComplete = { onHistoryDissolveComplete(historyKey) },
                            cardId = historyKey,
                            preset = DissolveAnimationPreset.TELEGRAM_FAST,
                            collapseAfterDissolve = shouldCollapseHistoryDeleteCard(historyDeleteAnimationMode),
                            publishGlobalDissolveState = shouldJiggleHistoryDeleteCards(historyDeleteAnimationMode),
                            keepInvisibleAfterDissolve = shouldKeepPlaceholderHidden ||
                                historyDeleteAnimationMode == HistoryDeleteAnimationMode.DIRECT_DELETE,
                            modifier = Modifier.jiggleOnDissolve(
                                cardId = historyKey,
                                enabled = shouldJiggleHistoryDeleteCards(historyDeleteAnimationMode),
                                isCurrentCardDissolving = isDissolving
                            )
                        ) {
                            cardContent()
                        }
                    } else {
                        cardContent()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun HistoryArticleCard(
    article: VideoItem,
    transitionEnabled: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val articleId = article.id.coerceAtLeast(0L)
    val coverTransitionKey = remember(articleId) {
        resolveArticleSharedTransitionKey(articleId, ArticleSharedElementSlot.COVER)
    }
    val cardBoundsRef = remember { object { var value: androidx.compose.ui.geometry.Rect? = null } }
    val triggerArticleClick = {
        cardBoundsRef.value?.let { bounds ->
            CardPositionManager.recordCardPosition(
                bounds = bounds,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx,
                density = density.density
            )
        }
        onClick()
    }
    val baseCoverModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(resolveHistoryArticleCoverAspectRatio())
    val coverModifier = if (transitionEnabled && sharedTransitionScope != null && animatedVisibilityScope != null && articleId > 0L) {
        with(sharedTransitionScope) {
            baseCoverModifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = coverTransitionKey),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ -> commonListSharedBoundsMotionSpec() },
                clipInOverlayDuringTransition = OverlayClip(AppShapes.container(ContainerLevel.Sheet))
            )
        }
    } else {
        baseCoverModifier
    }
    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                cardBoundsRef.value = coordinates.boundsInRoot()
            }
            .combinedClickable(
                onClick = triggerArticleClick,
                onLongClick = onLongClick
            ),
        shape = AppShapes.container(ContainerLevel.Sheet),
        colors = CardDefaults.elevatedCardColors(
            containerColor = AppSurfaceTokens.cardContainer()
        ),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall, topEnd = AppSpacingTokens.Large + AppSpacingTokens.ExtraSmall))
            ) {
                AsyncImage(
                    model = article.pic,
                    contentDescription = article.title,
                    modifier = coverModifier,
                    contentScale = ContentScale.Crop
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = AppSpacingTokens.Medium, vertical = AppSpacingTokens.Medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
            ) {
                AppSurface(
                    shape = AppShapes.container(ContainerLevel.Pill),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    AppText(
                        text = "专栏",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro, vertical = AppSpacingTokens.ExtraSmall)
                    )
                }
                AppText(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                AppText(
                    text = article.owner.name.ifBlank { "未知作者" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FavoriteSubscribedFolderList(
    folders: List<com.android.purebilibili.data.model.response.FavFolder>,
    searchQuery: String,
    padding: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    spacing: androidx.compose.ui.unit.Dp,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    transitionEnabled: Boolean,
    onLoadMore: () -> Unit,
    onFolderClick: (com.android.purebilibili.data.model.response.FavFolder) -> Unit
) {
    if (folders.isEmpty()) {
        val message = if (searchQuery.isNotBlank()) "没有找到相关追更" else "暂无追更合集"
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppText(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val shouldLoadMore = androidx.compose.runtime.remember {
        androidx.compose.runtime.derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore.value, hasMore, isLoadingMore) {
        if (shouldLoadMore.value && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier
            .responsiveContentWidth(resolveCommonListSingleColumnMaxWidth())
            .fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = spacing,
            end = spacing,
            top = padding.calculateTopPadding() + spacing,
            bottom = padding.calculateBottomPadding() + spacing + AppSpacingTokens.ExtraLarge
        ),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items = folders, key = { "favorite_subscribed_${it.id}_${it.fid}" }) { folder ->
            FavoriteSubscribedFolderRow(
                folder = folder,
                transitionEnabled = transitionEnabled,
                onClick = { onFolderClick(folder) }
            )
        }
    }
}

private sealed interface CommonListScrollState {
    data class Grid(val state: androidx.compose.foundation.lazy.grid.LazyGridState) : CommonListScrollState
    data class List(val state: androidx.compose.foundation.lazy.LazyListState) : CommonListScrollState
}

@Composable
private fun FavoriteSubscribedFolderRow(
    folder: com.android.purebilibili.data.model.response.FavFolder,
    transitionEnabled: Boolean,
    onClick: () -> Unit
) {
    val sharedElementRoute = remember(folder) {
        resolveSubscribedFavoriteFolderRoute(folder)
    }
    val previewCover = remember(folder.cover) {
        resolveFavoriteFolderPreviewCover(folder, emptyList())
    }
    AppSurface(
        modifier = Modifier
            .fillMaxWidth()
            .favoriteCollectionSharedBounds(
                route = sharedElementRoute,
                transitionEnabled = transitionEnabled
            )
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.container(ContainerLevel.Dialog)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacingTokens.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FavoriteSubscribedFolderPreview(
                coverUrl = previewCover,
                title = folder.title
            )
            Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = folder.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
                AppText(
                    text = "${folder.media_count} 个内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
            AppAssistChip(
                onClick = onClick,
                label = { AppText("订阅") }
            )
        }
    }
}

@Composable
private fun FavoriteSubscribedFolderPreview(
    coverUrl: String?,
    title: String
) {
    val shape = AppShapes.container(ContainerLevel.Field)
    Box(
        modifier = Modifier
            .width(resolveFavoriteSubscribedFolderPreviewWidth())
            .aspectRatio(16f / 9f)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (coverUrl != null) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(coverUrl),
                contentDescription = "$title 最新视频封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AppIcon(
                imageVector = rememberAppFolderIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Modifier.favoriteCollectionSharedBounds(
    route: FavoriteCollectionRoute?,
    transitionEnabled: Boolean
): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sharedElementId = remember(route?.type, route?.id) {
        route?.let { resolveFavoriteCollectionSharedElementId(it.type, it.id) }
    }
    if (
        !transitionEnabled ||
        route?.sharedElementTransition != true ||
        sharedElementId == null ||
        sharedTransitionScope == null ||
        animatedVisibilityScope == null
    ) {
        return this
    }
    val sharedElementKey = remember(sharedElementId) {
        BiliPaiSharedElementKey.Raw(
            namespace = "favorite_collection",
            id = sharedElementId
        )
    }
    return with(sharedTransitionScope) {
        this@favoriteCollectionSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = sharedElementKey),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = { _, _ -> commonListSharedBoundsMotionSpec() },
            clipInOverlayDuringTransition = OverlayClip(AppShapes.container(ContainerLevel.Dialog))
        )
    }
}

@Composable
private fun FavoriteProgressBadgeCapsule(
    modifier: Modifier = Modifier,
    title: String,
    badge: FavoriteProgressBadge
) {
    val widthSpec = resolveFavoriteProgressBadgeWidthSpec()
    AppSurface(
        modifier = modifier.widthIn(min = widthSpec.minWidth, max = widthSpec.maxWidth),
        shape = AppShapes.container(ContainerLevel.Floating),
        color = AppSurfaceTokens.cardContainer().copy(alpha = 0.9f),
        tonalElevation = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
        shadowElevation = AppSpacingTokens.Small
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacingTokens.Medium, vertical = AppSpacingTokens.Small + AppSpacingTokens.Micro),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Micro)
        ) {
            AppText(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AppText(
                text = badge.primaryText,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            AppText(
                text = badge.secondaryText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            badge.footnoteText?.let { footnote ->
                AppHorizontalDivider(
                    modifier = Modifier.padding(vertical = AppSpacingTokens.Micro),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    thickness = AppSpacingTokens.Micro / 4
                )
                AppText(
                    text = footnote,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FavoriteCollectionRow(
    item: com.android.purebilibili.data.model.response.VideoItem,
    onClick: () -> Unit
) {
    val subtitleParts = remember(item.owner.name, item.collectionMediaCount, item.collectionSubtitle) {
        buildList {
            item.owner.name.takeIf { it.isNotBlank() }?.let(::add)
            item.collectionMediaCount.takeIf { it > 0 }?.let { add("${it} 个视频") }
            item.collectionSubtitle.takeIf { it.isNotBlank() }?.let(::add)
        }
    }
    val subtitle = remember(subtitleParts) { subtitleParts.joinToString(separator = " · ") }

    AppSurface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = AppShapes.container(ContainerLevel.Card)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacingTokens.Medium + AppSpacingTokens.Micro, vertical = AppSpacingTokens.Medium + AppSpacingTokens.Micro),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = rememberAppFolderIcon(),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
                    AppText(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
            AppAssistChip(
                onClick = onClick,
                label = { AppText("合集") }
            )
        }
    }
}
