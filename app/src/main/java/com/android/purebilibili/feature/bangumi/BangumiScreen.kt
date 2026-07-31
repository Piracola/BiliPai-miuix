// 文件路径: feature/bangumi/BangumiScreen.kt
package com.android.purebilibili.feature.bangumi
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
//  Cupertino Icons - iOS SF Symbols 风格图标
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import io.github.alexzhirkevich.cupertino.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.rememberAppChevronUpIcon
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppLinearProgressIndicator
import com.android.purebilibili.core.ui.components.AppSmallFloatingActionButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppTextButton
//  已改用 MaterialTheme.colorScheme.primary
import com.android.purebilibili.core.theme.iOSYellow
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.data.model.response.BangumiItem
import com.android.purebilibili.data.model.response.BangumiSearchItem
import com.android.purebilibili.data.model.response.BangumiType
import com.android.purebilibili.data.model.response.FollowBangumiItem
import com.android.purebilibili.data.model.response.TimelineDay
import com.android.purebilibili.data.model.response.TimelineEpisode
import com.android.purebilibili.data.model.response.resolveBangumiIndexFilterGroups
import com.android.purebilibili.data.model.response.resolveBangumiIndexFilterKey
import com.android.purebilibili.data.model.response.resolveBangumiSearchPlaceholder
// [重构] 使用提取的可复用组件
import com.android.purebilibili.feature.bangumi.ui.components.BangumiModeTabs
import com.android.purebilibili.feature.bangumi.ui.components.BangumiIndexFilterRows
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.bangumi.ui.list.BangumiCard
import com.android.purebilibili.feature.bangumi.ui.list.BangumiGrid
import com.android.purebilibili.feature.bangumi.ui.list.BangumiSearchCardGrid
import java.util.Calendar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 番剧主页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun BangumiScreen(
    onBack: () -> Unit,
    onBangumiClick: (Long) -> Unit,  // 点击番剧 -> seasonId
    onBangumiEpisodeClick: (Long, Long) -> Unit = { seasonId, _ -> onBangumiClick(seasonId) },
    initialType: Int = 1,  // 初始类型：1=番剧 2=电影 等
    viewModel: BangumiViewModel = viewModel()
) {
    val displayMode by viewModel.displayMode.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val timelineState by viewModel.timelineState.collectAsStateWithLifecycle()
    val searchState by viewModel.searchState.collectAsStateWithLifecycle()
    val myFollowState by viewModel.myFollowState.collectAsStateWithLifecycle()
    val myFollowType by viewModel.myFollowType.collectAsStateWithLifecycle()
    val myFollowStats by viewModel.myFollowStats.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val searchKeyword by viewModel.searchKeyword.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // 搜索状态
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var indexChromeCollapsed by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // 初始类型切换
    LaunchedEffect(initialType) {
        if (initialType != 1) {
            viewModel.selectType(initialType)
        }
    }
    
    // 番剧类型列表
    val types = listOf(
        BangumiType.ANIME,
        BangumiType.GUOCHUANG,
        BangumiType.MOVIE,
        BangumiType.TV_SHOW,
        BangumiType.DOCUMENTARY,
        BangumiType.VARIETY
    )
    val currentYear = remember {
        Calendar.getInstance().get(Calendar.YEAR)
    }
    val filterGroups = remember(selectedType, currentYear) {
        resolveBangumiIndexFilterGroups(
            seasonType = selectedType,
            currentYear = currentYear
        )
    }
    val listTransitionKey = remember(selectedType, filter) {
        resolveBangumiIndexFilterKey(selectedType, filter)
    }

    LaunchedEffect(displayMode, selectedType, filter) {
        if (displayMode != BangumiDisplayMode.LIST) {
            indexChromeCollapsed = false
        }
    }
    
    //  [修复] 设置导航栏透明，确保底部手势栏沉浸式效果
    androidx.compose.runtime.DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.navigationBarColor ?: android.graphics.Color.TRANSPARENT
        
        if (window != null) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        
        onDispose {
            if (window != null) {
                window.navigationBarColor = originalNavBarColor
            }
        }
    }
    
    AppScaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        topBar = {
            if (showSearchBar) {
                // 搜索模式顶栏
                BangumiSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        if (it.isNotBlank()) {
                            viewModel.searchBangumi(it)
                            keyboardController?.hide()
                        }
                    },
                    onBack = {
                        showSearchBar = false
                        searchQuery = ""
                        viewModel.clearSearch()
                    }
                )
            } else {
                BangumiNavigationBar(
                    title = if (displayMode == BangumiDisplayMode.MY_FOLLOW) "我的追番/追剧" else "番剧影视",
                    isMyFollowMode = displayMode == BangumiDisplayMode.MY_FOLLOW,
                    onBack = {
                        if (displayMode == BangumiDisplayMode.MY_FOLLOW) {
                            viewModel.setDisplayMode(BangumiDisplayMode.LIST)
                        } else {
                            onBack()
                        }
                    },
                    onSearch = { showSearchBar = true },
                    onOpenMyFollow = { viewModel.openMyFollowEntry() }
                )
            }
        },
        //  [修复] 禁用 Scaffold 默认的 WindowInsets 消耗，让内容区域自行处理
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .responsiveContentWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )
                )
                //  [修复] 移除这里的底部内边距，让内容区域自己处理（如 LazyVerticalGrid 的 contentPadding）
        ) {
            val shouldShowIndexChrome = displayMode != BangumiDisplayMode.LIST || !indexChromeCollapsed
            if (displayMode == BangumiDisplayMode.LIST || displayMode == BangumiDisplayMode.TIMELINE) {
                if (shouldShowIndexChrome) {
                    Column {
                        BangumiModeTabs(
                            currentMode = displayMode,
                            onModeChange = { viewModel.setDisplayMode(it) }
                        )

                        if (displayMode == BangumiDisplayMode.LIST) {
                            BangumiTypeTabs(
                                types = types,
                                selectedType = selectedType,
                                onTypeSelected = { viewModel.selectType(it) }
                            )
                            BangumiIndexFilterRows(
                                groups = filterGroups,
                                filter = filter,
                                onFilterChange = { viewModel.updateFilter(it) }
                            )
                        }
                    }
                }
            }
            
            // 内容区域
            when (displayMode) {
                BangumiDisplayMode.LIST -> {
                    key(listTransitionKey) {
                            BangumiPiliPlusHomeContent(
                                listState = listState,
                                timelineState = timelineState,
                                myFollowState = myFollowState,
                                selectedType = selectedType,
                                myFollowType = myFollowType,
                                onRetry = { viewModel.loadBangumiList() },
                                onRetryTimeline = { viewModel.loadTimeline() },
                                onRetryMyFollow = { viewModel.loadMyFollowBangumi(myFollowType) },
                                onLoadMore = { viewModel.loadMore() },
                                onOpenMyFollow = { viewModel.openMyFollowEntry() },
                                onItemClick = onBangumiClick,
                                onChromeCollapsedChange = { indexChromeCollapsed = it }
                            )
                    }
                }
                BangumiDisplayMode.TIMELINE -> {
                    BangumiTimelineContent(
                        timelineState = timelineState,
                        onRetry = { viewModel.loadTimeline() },
                        onBangumiClick = onBangumiClick
                    )
                }
                BangumiDisplayMode.MY_FOLLOW -> {
                    MyBangumiContent(
                        myFollowState = myFollowState,
                        followStats = myFollowStats,
                        followType = myFollowType,
                        onFollowTypeChange = { viewModel.selectMyFollowType(it) },
                        onRetry = { viewModel.loadMyFollowBangumi(myFollowType) },
                        onLoadMore = { viewModel.loadMoreMyFollow() },
                        onBangumiClick = onBangumiClick
                    )
                }
                BangumiDisplayMode.SEARCH -> {
                    BangumiSearchContent(
                        searchState = searchState,
                        selectedType = selectedType,
                        onRetry = { viewModel.searchBangumi(searchKeyword) },
                        onLoadMore = { viewModel.loadMoreSearchResults() },
                        onItemClick = onBangumiClick,
                        onEpisodeClick = onBangumiEpisodeClick
                    )
                }
            }
        }
    }
}

/**
 * PiliPlus 风格番剧首页：最近追番/追剧、时间表、推荐索引在同一信息流中展示。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BangumiPiliPlusHomeContent(
    listState: BangumiListState,
    timelineState: TimelineState,
    myFollowState: MyFollowState,
    selectedType: Int,
    myFollowType: Int,
    onRetry: () -> Unit,
    onRetryTimeline: () -> Unit,
    onRetryMyFollow: () -> Unit,
    onLoadMore: () -> Unit,
    onOpenMyFollow: () -> Unit,
    onItemClick: (Long) -> Unit,
    onChromeCollapsedChange: (Boolean) -> Unit
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val currentOnChromeCollapsedChange by rememberUpdatedState(onChromeCollapsedChange)
    val hasMore = (listState as? BangumiListState.Success)?.hasMore == true
    val shouldShowBackToTop by remember(gridState) {
        derivedStateOf {
            shouldShowBangumiIndexBackToTop(
                firstVisibleItemIndex = gridState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
            )
        }
    }

    LaunchedEffect(gridState) {
        snapshotFlow {
            shouldCollapseBangumiIndexChrome(
                firstVisibleItemIndex = gridState.firstVisibleItemIndex,
                firstVisibleItemScrollOffset = gridState.firstVisibleItemScrollOffset
            )
        }
            .distinctUntilChanged()
            .collect { collapsed ->
                currentOnChromeCollapsedChange(collapsed)
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentOnChromeCollapsedChange(false)
        }
    }

    LaunchedEffect(gridState, hasMore) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= layoutInfo.totalItemsCount - 6
        }.collect { shouldLoad ->
            if (shouldLoad && hasMore) {
                onLoadMore()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 106.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp,
                bottom = 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val shouldShowFollowSection = (myFollowState as? MyFollowState.Error)?.message != "未登录"
            if (shouldShowFollowSection) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BangumiHomeSectionHeader(
                        title = if (myFollowType == MY_FOLLOW_TYPE_BANGUMI) "最近追番" else "最近追剧",
                        actionText = "查看全部",
                        onAction = onOpenMyFollow
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BangumiFollowPreviewSection(
                        state = myFollowState,
                        onRetry = onRetryMyFollow,
                        onOpenMyFollow = onOpenMyFollow,
                        onItemClick = onItemClick
                    )
                }
            }

            if (selectedType == BangumiType.ANIME.value || selectedType == BangumiType.GUOCHUANG.value) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BangumiHomeSectionHeader(
                        title = "追番时间表",
                        actionText = "刷新",
                        onAction = onRetryTimeline
                    )
                }
                item(span = { GridItemSpan(maxLineSpan) }) {
                    BangumiTimelinePreviewSection(
                        state = timelineState,
                        onRetry = onRetryTimeline,
                        onItemClick = onItemClick
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                BangumiHomeSectionHeader(title = "推荐")
            }

            when (listState) {
                is BangumiListState.Loading -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeLoadingStrip(minHeight = 180.dp)
                    }
                }
                is BangumiListState.Error -> {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        HomeErrorStrip(
                            message = listState.message,
                            onRetry = onRetry
                        )
                    }
                }
                is BangumiListState.Success -> {
                    if (listState.isRefreshing) {
                        item(
                            key = "refreshing-indicator",
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            AppLinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 2.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (listState.items.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            AppText(
                                text = "暂无推荐内容",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = listState.items,
                            key = { index, item -> resolveBangumiIndexItemLazyKey(index, item) }
                        ) { _, item ->
                            BangumiCard(
                                item = item,
                                modifier = Modifier.animateItem(),
                                onClick = { onItemClick(item.seasonId) }
                            )
                        }
                        if (listState.hasMore) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HomeLoadingStrip(minHeight = 56.dp)
                            }
                        }
                    }
                }
            }
        }

        if (shouldShowBackToTop) {
            AppSmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        gridState.animateScrollToItem(0)
                        currentOnChromeCollapsedChange(false)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        end = 20.dp,
                        bottom = 28.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    ),
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
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

@Composable
private fun BangumiHomeSectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppText(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            AppTextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                AppText(actionText, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun BangumiFollowPreviewSection(
    state: MyFollowState,
    onRetry: () -> Unit,
    onOpenMyFollow: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    when (state) {
        is MyFollowState.Loading -> HomeLoadingStrip(minHeight = 150.dp)
        is MyFollowState.Error -> HomeErrorStrip(
            message = state.message,
            onRetry = onRetry,
            minHeight = 120.dp
        )
        is MyFollowState.Success -> {
            if (state.items.isEmpty()) {
                AppSurface(
                    onClick = onOpenMyFollow,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        AppText(
                            text = "还没有追番追剧",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    itemsIndexed(
                        items = state.items.take(12),
                        key = { index, item -> resolveMyFollowItemLazyKey(index, item) }
                    ) { _, item ->
                        FollowBangumiHomeCard(
                            item = item,
                            onClick = { onItemClick(item.seasonId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowBangumiHomeCard(
    item: FollowBangumiItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(item.cover),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                            startY = 90f
                        )
                    )
            )
            val bottomText = item.progress.ifBlank {
                item.newEp?.indexShow.orEmpty()
            }
            if (bottomText.isNotBlank()) {
                AppText(
                    text = bottomText,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp)
                )
            }
            if (item.badge.isNotBlank()) {
                BangumiHomeBadge(
                    text = item.badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                )
            }
        }
        AppText(
            text = item.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun BangumiTimelinePreviewSection(
    state: TimelineState,
    onRetry: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    when (state) {
        is TimelineState.Loading -> HomeLoadingStrip(minHeight = 170.dp)
        is TimelineState.Error -> HomeErrorStrip(
            message = state.message,
            onRetry = onRetry,
            minHeight = 130.dp
        )
        is TimelineState.Success -> {
            val visibleDays = state.days.filter { !it.episodes.isNullOrEmpty() }
            if (visibleDays.isEmpty()) {
                AppText(
                    text = "今天暂无更新",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                    fontSize = 14.sp
                )
                return
            }
            var selectedIndex by remember(visibleDays) {
                mutableIntStateOf(visibleDays.indexOfFirst { it.isToday == 1 }.coerceAtLeast(0))
            }
            val selectedDay = visibleDays.getOrNull(selectedIndex) ?: visibleDays.first()
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(visibleDays.size, key = { index -> visibleDays[index].date }) { index ->
                        val day = visibleDays[index]
                        val selected = index == selectedIndex
                        AppSurface(
                            onClick = { selectedIndex = index },
                            shape = RoundedCornerShape(8.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            AppText(
                                text = buildTimelineDayLabel(day),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(
                        items = selectedDay.episodes.orEmpty(),
                        key = { index, episode -> resolveTimelineEpisodeLazyKey(index, episode) }
                    ) { _, episode ->
                        TimelineEpisodeHomeCard(
                            episode = episode,
                            onClick = { onItemClick(episode.seasonId) }
                        )
                    }
                }
            }
        }
    }
}

private fun buildTimelineDayLabel(day: TimelineDay): String {
    if (day.isToday == 1) return "今天"
    val week = when (day.dayOfWeek) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> day.date.takeLast(5)
    }
    return "${day.date.takeLast(5)} $week"
}

@Composable
private fun TimelineEpisodeHomeCard(
    episode: TimelineEpisode,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = FormatUtils.fixImageUrl(episode.cover),
                contentDescription = episode.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                            startY = 90f
                        )
                    )
            )
            val updateLabel = episode.pubIndex.ifBlank { episode.pubTime }
            if (updateLabel.isNotBlank()) {
                AppText(
                    text = updateLabel,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(7.dp)
                )
            }
            if (episode.follow == 1) {
                BangumiHomeBadge(
                    text = "已追",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(5.dp)
                )
            }
        }
        AppText(
            text = episode.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun BangumiHomeBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    AppSurface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(4.dp)
    ) {
        AppText(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

@Composable
private fun HomeLoadingStrip(
    minHeight: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        contentAlignment = Alignment.Center
    ) {
        com.android.purebilibili.core.ui.CutePersonLoadingIndicator(
            size = 32.dp,
            strokeWidth = 2.dp
        )
    }
}

@Composable
private fun HomeErrorStrip(
    message: String,
    onRetry: () -> Unit,
    minHeight: androidx.compose.ui.unit.Dp = 150.dp
) {
    AppSurface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppText(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            AppTextButton(onClick = onRetry) {
                AppText("重试")
            }
        }
    }
}


/**
 * 搜索顶栏
 */
@Composable
private fun BangumiSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit
) {
    AppSurface(
        modifier = Modifier.fillMaxWidth().responsiveContentWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(onClick = onBack) {
                    AppIcon(CupertinoIcons.Default.ChevronBackward, contentDescription = "返回")
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // 搜索框
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        CupertinoIcons.Default.MagnifyingGlass,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) {
                                    AppText(
                                        "搜索番剧名称、声优...",
                                        style = TextStyle(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                inner()
                            }
                        }
                    )
                    
                    if (query.isNotEmpty()) {
                        AppIconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            AppIcon(
                                CupertinoIcons.Default.XmarkCircle,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                AppTextButton(
                    onClick = { onSearch(query) },
                    enabled = query.isNotEmpty()
                ) {
                    AppText(
                        "搜索",
                        color = if (query.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BangumiNavigationBar(
    title: String,
    isMyFollowMode: Boolean,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onOpenMyFollow: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val titleFontSize = resolveBangumiNavigationTitleFontSizeSp(configuration.screenWidthDp).sp

    AppSurface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        shadowElevation = 2.dp
    ) {
        Column {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconButton(onClick = onBack) {
                    AppIcon(CupertinoIcons.Default.ChevronBackward, contentDescription = "返回")
                }
                AppText(
                    text = title,
                    fontSize = titleFontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                AppIconButton(onClick = onSearch) {
                    AppIcon(CupertinoIcons.Default.MagnifyingGlass, contentDescription = "搜索")
                }
                AppIconButton(onClick = onOpenMyFollow) {
                    AppIcon(
                        CupertinoIcons.Default.Bookmark,
                        contentDescription = "我的追番",
                        tint = if (isMyFollowMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
                if (!isMyFollowMode) {
                    Spacer(modifier = Modifier.width(48.dp))
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        }
    }
}

@Composable
private fun BangumiTypeTabs(
    types: List<BangumiType>,
    selectedType: Int,
    onTypeSelected: (Int) -> Unit
) {
    BottomBarLiquidSegmentedControl(
        items = types.map { it.label },
        selectedIndex = types.indexOfFirst { it.value == selectedType }.coerceAtLeast(0),
        onSelected = { index -> types.getOrNull(index)?.let { onTypeSelected(it.value) } },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        height = 46.dp,
        indicatorHeight = 40.dp,
        labelFontSize = 14.sp,
        dragSelectionEnabled = true,
        preferInlineContentStyle = false
    )
}

/**
 * 番剧列表内容
 */
@Composable
private fun BangumiListContent(
    listState: BangumiListState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (Long) -> Unit
) {
    when (listState) {
        is BangumiListState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                com.android.purebilibili.core.ui.CutePersonLoadingIndicator()
            }
        }
        is BangumiListState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(
                        text = listState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(onClick = onRetry) {
                        AppText("重试")
                    }
                }
            }
        }
        is BangumiListState.Success -> {
            BangumiGrid(
                items = listState.items,
                hasMore = listState.hasMore,
                onLoadMore = onLoadMore,
                onItemClick = { onItemClick(it.seasonId) }
            )
        }
    }
}

/**
 * 番剧搜索结果内容
 */
@Composable
private fun BangumiSearchContent(
    searchState: BangumiSearchState,
    selectedType: Int,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (Long) -> Unit,
    onEpisodeClick: (Long, Long) -> Unit
) {
    when (searchState) {
        is BangumiSearchState.Idle -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppText(
                    resolveBangumiSearchPlaceholder(selectedType),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is BangumiSearchState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                com.android.purebilibili.core.ui.CutePersonLoadingIndicator()
            }
        }
        is BangumiSearchState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AppText(
                        text = searchState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(onClick = onRetry) {
                        AppText("重试")
                    }
                }
            }
        }
        is BangumiSearchState.Success -> {
            if (searchState.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AppText(
                        "未找到相关番剧",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                BangumiSearchGrid(
                    items = searchState.items,
                    hasMore = searchState.hasMore,
                    onLoadMore = onLoadMore,
                    onItemClick = onItemClick,
                    onEpisodeClick = onEpisodeClick
                )
            }
        }
    }
}

@Composable
private fun BangumiSearchGrid(
    items: List<BangumiSearchItem>,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onItemClick: (Long) -> Unit,
    onEpisodeClick: (Long, Long) -> Unit
) {
    val gridState = rememberLazyGridState()
    
    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItem >= layoutInfo.totalItemsCount - 4
        }.collect { shouldLoad ->
            if (shouldLoad && hasMore) {
                onLoadMore()
            }
        }
    }
    
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        state = gridState,
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 12.dp,
            end = 12.dp,
            bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(
            items = items,
            key = { index, item -> resolveBangumiSearchItemLazyKey(index, item) }
        ) { _, item ->
            BangumiSearchCardGrid(
                item = item,
                onClick = { onItemClick(item.seasonId.takeIf { it > 0L } ?: item.pgcSeasonId) },
                onEpisodeClick = item.episodes?.firstOrNull { it.id > 0L }?.let { episode ->
                    {
                        val targetSeasonId = item.seasonId.takeIf { it > 0L } ?: item.pgcSeasonId
                        onEpisodeClick(targetSeasonId, episode.id)
                    }
                }
            )
        }
        
        if (hasMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    com.android.purebilibili.core.ui.CutePersonLoadingIndicator(
                        size = 24.dp,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}
