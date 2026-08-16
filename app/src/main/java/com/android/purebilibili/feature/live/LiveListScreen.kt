package com.android.purebilibili.feature.live

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Photo
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import com.android.purebilibili.core.ui.components.AppBadge
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AdaptivePullToRefreshBox
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalBottomBarContentPadding
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.data.model.response.LiveAreaParent
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch


internal fun com.android.purebilibili.data.model.response.LiveRoom.toLiveRoomItem(): LiveRoomItem =
    LiveRoomItem(
        roomId = roomid,
        title = title,
        cover = displayCover(preferFirstFrame = false),
        systemCover = systemCover.ifBlank { keyframe },
        uname = uname,
        face = face,
        online = viewerCount(),
        areaName = areaName,
    )

@Composable
fun LiveListScreen(
    onBack: () -> Unit,
    onLiveClick: (Long, String, String) -> Unit,
    onSearchClick: () -> Unit,
    onAreaListClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onAreaDetailClick: (Int, Int, String) -> Unit,
    onMatchClick: () -> Unit = {},
    /** 底栏主入口时隐藏返回，更接近 BiliPai 主 tab 形态。 */
    showNavigationBack: Boolean = true,
    viewModel: LiveListViewModel = viewModel(),
    globalHazeState: HazeState? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val palette = rememberLiveChromePalette()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.navigationBarColor
        if (window != null) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        onDispose {
            if (window != null && originalNavBarColor != null) {
                window.navigationBarColor = originalNavBarColor
            }
        }
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val topChromePolicy = rememberAppTopChromePolicy()
    val visualSpec = remember(topChromePolicy.tabPresentation) {
        resolveLiveVisualSpec(topChromePolicy.tabPresentation)
    }
    val metrics = visualSpec.homeMetrics
    val contentWidth = if (windowSizeClass.isExpandedScreen) {
        minOf(windowSizeClass.widthDp, visualSpec.maxContentWidthDp.dp)
    } else {
        windowSizeClass.widthDp
    }
    val gridColumns = remember(contentWidth, windowSizeClass.isTablet) {
        resolveLiveBiliPaiGridColumns(contentWidth.value.toInt(), windowSizeClass.isTablet)
    }
    val gridBottomPadding = LocalBottomBarContentPadding.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.backgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .responsiveContentWidth(maxWidth = visualSpec.maxContentWidthDp.dp)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LiveListHeader(
                metrics = metrics,
                livingCount = state.livingCount,
                primaryFace = state.followItems.firstOrNull()?.face.orEmpty(),
                showNavigationBack = showNavigationBack,
                onBack = onBack,
                onSearchClick = onSearchClick,
                onInboxClick = onFollowingClick,
                onAvatarClick = onAreaListClick
            )
            AdaptivePullToRefreshBox(
                isRefreshing = state.isLoading,
                onRefresh = viewModel::refresh,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = AppSpacingTokens.ExtraSmall),
            ) {
                when {
                    state.isLoading && state.contentItems.isEmpty() && state.followItems.isEmpty() -> {
                        LiveListLoadingState()
                    }
                    state.error != null && state.contentItems.isEmpty() -> {
                        LiveListErrorState(
                            message = state.error ?: "未知错误",
                            onRetry = viewModel::refresh
                        )
                    }
                    else -> {
                        LiveHomeContent(
                            contentItems = state.contentItems,
                            followItems = state.followItems,
                            areaEntries = state.areaEntries,
                            areaList = state.areaList,
                            selectedAreaIndex = state.selectedAreaIndex,
                            selectedParentAreaId = state.selectedParentAreaId,
                            sortTags = state.sortTags,
                            selectedSortType = state.selectedSortType,
                            livingCount = state.livingCount,
                            isLoadingMore = state.isLoadingMore,
                            hasMore = state.hasMore,
                            showFirstFrame = state.showFirstFrame,
                            gridColumns = gridColumns,
                            bottomPadding = gridBottomPadding,
                            metrics = metrics,
                            visualSpec = visualSpec,
                            onLiveClick = onLiveClick,
                            onAreaSelected = viewModel::selectHomeArea,
                            onSortTagSelected = viewModel::selectSortTag,
                            onToggleFirstFrame = viewModel::toggleShowFirstFrame,
                            onLoadMore = viewModel::loadMore,
                            onAreaDetailClick = onAreaDetailClick,
                            onAreaListClick = onAreaListClick,
                            onFollowingClick = onFollowingClick,
                            onMatchClick = onMatchClick,
                            onLongPressCard = { card ->
                                coroutineScope.launch {
                                    val success = com.android.purebilibili.feature.download.DownloadManager
                                        .saveImageToGallery(
                                            context = context,
                                            url = card.coverUrl,
                                            title = card.title
                                        )
                                    Toast.makeText(
                                        context,
                                        if (success) "封面已保存到相册" else "封面保存失败，请稍后重试",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveHomeContent(
    contentItems: List<LiveRoomItem>,
    followItems: List<LiveRoomItem>,
    areaEntries: List<com.android.purebilibili.data.model.response.LiveFeedAreaEntry>,
    areaList: List<LiveAreaParent>,
    selectedAreaIndex: Int,
    selectedParentAreaId: Int,
    sortTags: List<com.android.purebilibili.data.model.response.LiveSecondSortTag>,
    selectedSortType: String?,
    livingCount: Int,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    showFirstFrame: Boolean,
    gridColumns: Int,
    bottomPadding: androidx.compose.ui.unit.Dp,
    metrics: LiveBiliPaiHomeMetrics,
    visualSpec: LiveVisualSpec,
    onLiveClick: (Long, String, String) -> Unit,
    onAreaSelected: (Int) -> Unit,
    onSortTagSelected: (String?) -> Unit,
    onToggleFirstFrame: () -> Unit,
    onLoadMore: () -> Unit,
    onAreaDetailClick: (Int, Int, String) -> Unit,
    onAreaListClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onMatchClick: () -> Unit = {},
    onLongPressCard: (LiveRoomCardUiModel) -> Unit = {}
) {
    val selectedParent = areaList.firstOrNull { it.id == selectedParentAreaId }
        ?: areaList.firstOrNull {
            areaEntries.getOrNull((selectedAreaIndex - 1).coerceAtLeast(0))?.parentAreaId == it.id
        }

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = metrics.safeSpaceDp.dp,
            end = metrics.safeSpaceDp.dp,
            top = metrics.cardSpaceDp.dp,
            bottom = bottomPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(metrics.cardSpaceDp.dp),
        verticalArrangement = Arrangement.spacedBy(metrics.cardSpaceDp.dp)
    ) {
        if (followItems.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveFollowHeader(
                    livingCount = livingCount,
                    onActionClick = onFollowingClick
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveFollowAvatarRow(
                    items = followItems.take(10),
                    metrics = metrics,
                    onLiveClick = onLiveClick
                )
            }
        }
        if (areaEntries.isNotEmpty() || areaList.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveAreaHomeChipRow(
                    areaEntries = resolveLiveHomeAreaEntries(
                        feedEntries = areaEntries,
                        areaParents = areaList
                    ),
                    selectedAreaIndex = selectedAreaIndex,
                    showFirstFrame = showFirstFrame,
                    onAreaSelected = onAreaSelected,
                    onToggleFirstFrame = onToggleFirstFrame,
                    onAreaListClick = onAreaListClick,
                    onMatchClick = onMatchClick,
                )
            }
            if (selectedAreaIndex > 0 && sortTags.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LiveSortTagChipRow(
                        tags = sortTags,
                        selectedSortType = selectedSortType,
                        onSortTagSelected = onSortTagSelected,
                    )
                }
            }
            if (selectedAreaIndex > 0 && !selectedParent?.list.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LiveAreaChildChipRow(
                        items = selectedParent.list.orEmpty(),
                        parentAreaId = selectedParent.id,
                        onAreaDetailClick = onAreaDetailClick
                    )
                }
            }
        }
        when {
            contentItems.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState("暂无直播内容", visualSpec)
            }
            else -> {
                items(contentItems, key = { it.roomId }) { item ->
                    val model = item.toLiveRoomCardUiModel(showFirstFrame)
                    LiveRoomCard(
                        model = model,
                        enableSharedCoverTransition = true,
                        onClick = { onLiveClick(item.roomId, item.title, item.uname) },
                        onLongPress = { onLongPressCard(model) }
                    )
                }
                if (hasMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LiveHomeLoadMoreFooter(
                            isLoadingMore = isLoadingMore,
                            contentCount = contentItems.size,
                            hasMore = hasMore,
                            onLoadMore = onLoadMore,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveListHeader(
    metrics: LiveBiliPaiHomeMetrics,
    livingCount: Int,
    primaryFace: String,
    showNavigationBack: Boolean,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onInboxClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    val compactChrome = rememberAppTopChromePolicy().compactChromeSpec
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = metrics.safeSpaceDp.dp,
                vertical = AppSpacingTokens.Small,
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
        ) {
            if (showNavigationBack) {
                AppSurface(
                    onClick = onBack,
                    color = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AppIcon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "返回",
                            tint = palette.primaryText
                        )
                    }
                }
            } else {
                AppText(
                    text = "直播",
                    color = palette.primaryText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(end = AppSpacingTokens.ExtraSmall),
                )
            }
            AppSurface(
                onClick = onSearchClick,
                color = palette.searchField,
                shape = AppShapes.container(ContainerLevel.Pill),
                modifier = Modifier
                    .weight(1f)
                    .height(compactChrome.primaryHeightDp.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = compactChrome.inputHorizontalPaddingDp.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = palette.secondaryText
                    )
                    Spacer(Modifier.width(AppSpacingTokens.Medium))
                    AppText(
                        text = "搜索直播间 / 主播",
                        color = palette.secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Box {
                AppSurface(
                    onClick = onInboxClick,
                    color = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AppIcon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "开播提醒",
                            tint = palette.primaryText
                        )
                    }
                }
                if (livingCount > 0) {
                    AppBadge(
                        containerColor = palette.accentStrong,
                        contentColor = palette.onAccent,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        AppText(
                            text = if (livingCount > 99) "99+" else livingCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(AppSpacingTokens.TripleExtraLarge)
                    .clickable(onClick = onAvatarClick)
                    .semantics { contentDescription = "全部直播分区" },
                contentAlignment = Alignment.Center,
            ) {
                AppSurface(
                    color = palette.surfaceMuted,
                    shape = CircleShape,
                    modifier = Modifier.size(compactChrome.secondaryButtonSizeDp.dp)
                ) {
                    if (primaryFace.isNotBlank()) {
                        AsyncImage(
                            model = primaryFace,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            AppText(
                                text = "LIVE",
                                color = palette.primaryText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveFollowHeader(
    livingCount: Int,
    onActionClick: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            AppText(
                text = "我的关注  ",
                color = palette.primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            AppText(
                text = livingCount.toString(),
                color = palette.accentStrong,
                style = MaterialTheme.typography.bodySmall,
            )
            AppText(
                text = " 人正在直播",
                color = palette.secondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            modifier = Modifier.clickable(onClick = onActionClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "查看更多",
                color = palette.secondaryText,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(AppSpacingTokens.ExtraSmall))
            AppText(
                text = ">",
                color = palette.secondaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LiveFollowAvatarRow(
    items: List<LiveRoomItem>,
    metrics: LiveBiliPaiHomeMetrics,
    onLiveClick: (Long, String, String) -> Unit
) {
    val palette = rememberLiveChromePalette()
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
        items(items, key = { it.roomId }) { item ->
            Column(
                modifier = Modifier
                    .width(metrics.followItemExtentDp.dp)
                    .clickable { onLiveClick(item.roomId, item.title, item.uname) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size((metrics.followAvatarSizeDp + 5).dp)
                            .clip(CircleShape)
                            .background(palette.accentStrong)
                            .padding(AppSpacingTokens.Micro)
                    ) {
                        AsyncImage(
                            model = item.face.ifBlank { item.cover },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(palette.surface)
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacingTokens.Small))
                AppText(
                    text = item.uname,
                    color = palette.primaryText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LiveAreaHomeChipRow(
    areaEntries: List<com.android.purebilibili.data.model.response.LiveFeedAreaEntry>,
    selectedAreaIndex: Int,
    showFirstFrame: Boolean,
    onAreaSelected: (Int) -> Unit,
    onToggleFirstFrame: () -> Unit,
    onAreaListClick: () -> Unit,
    onMatchClick: () -> Unit,
) {
    // BiliPai: 横向分区 chip + 右侧工具（封面/首帧、赛事、全部分区）
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item(key = "recommend") {
                LiveHomeSelectableChip(
                    label = "推荐",
                    selected = selectedAreaIndex == 0,
                    onClick = { onAreaSelected(0) },
                )
            }
            items(areaEntries.size, key = { index -> "${areaEntries[index].parentAreaId}_${areaEntries[index].areaId}_$index" }) { index ->
                val entry = areaEntries[index]
                LiveHomeSelectableChip(
                    label = entry.title,
                    selected = selectedAreaIndex == index + 1,
                    onClick = { onAreaSelected(index + 1) },
                )
            }
        }
        AppIconButton(
            onClick = onToggleFirstFrame,
            modifier = Modifier.size(40.dp),
        ) {
            AppIcon(
                imageVector = if (showFirstFrame) {
                    Icons.Outlined.Photo
                } else {
                    Icons.Outlined.Image
                },
                contentDescription = if (showFirstFrame) "显示封面" else "显示首帧",
                modifier = Modifier.size(18.dp),
            )
        }
        AppIconButton(
            onClick = onMatchClick,
            modifier = Modifier.size(40.dp),
        ) {
            AppIcon(
                imageVector = Icons.Outlined.SportsEsports,
                contentDescription = "游戏赛事",
                modifier = Modifier.size(18.dp),
            )
        }
        AppIconButton(
            onClick = onAreaListClick,
            modifier = Modifier.size(40.dp),
        ) {
            AppIcon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = "全部标签",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun LiveSortTagChipRow(
    tags: List<com.android.purebilibili.data.model.response.LiveSecondSortTag>,
    selectedSortType: String?,
    onSortTagSelected: (String?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
        items(tags, key = { it.sortType.ifBlank { it.name } }) { tag ->
            val selected = selectedSortType == tag.sortType ||
                (selectedSortType.isNullOrBlank() && tags.firstOrNull() == tag)
            LiveHomeSelectableChip(
                label = tag.name.ifBlank { tag.sortType },
                selected = selected,
                compact = true,
                onClick = { onSortTagSelected(tag.sortType.takeIf { it.isNotBlank() }) },
            )
        }
    }
}

@Composable
private fun LiveHomeLoadMoreFooter(
    isLoadingMore: Boolean,
    contentCount: Int,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
) {
    // 滚到底部时自动请求下一页（对齐 BiliPai onLoadMore）
    LaunchedEffect(contentCount, isLoadingMore, hasMore) {
        if (hasMore && !isLoadingMore && contentCount > 0) {
            onLoadMore()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacingTokens.Medium),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = if (isLoadingMore) "加载更多…" else "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LiveAreaChildChipRow(
    items: List<com.android.purebilibili.data.model.response.LiveAreaChild>,
    parentAreaId: Int,
    onAreaDetailClick: (Int, Int, String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
        items(items, key = { it.id }) { child ->
            LiveHomeSelectableChip(
                label = child.name,
                selected = false,
                compact = true,
                onClick = {
                    onAreaDetailClick(
                        parentAreaId,
                        child.id.toIntOrNull() ?: 0,
                        child.name
                    )
                },
            )
        }
    }
}

@Composable
private fun LiveListLoadingState() {
    val palette = rememberLiveChromePalette()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacingTokens.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = "直播内容加载中…",
            color = palette.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LiveListErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppText(
            text = message,
            color = palette.primaryText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(AppSpacingTokens.Medium))
        AppOutlinedButton(onClick = onRetry) {
            AppText("重试")
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    visualSpec: LiveVisualSpec,
) {
    val palette = rememberLiveChromePalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacingTokens.DoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(visualSpec.emptyStateContainerSizeDp.dp)
                .clip(CircleShape)
                .background(palette.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = palette.secondaryText,
                modifier = Modifier.size(visualSpec.emptyStateIconSizeDp.dp)
            )
        }
        AppText(
            text = message,
            color = palette.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun LiveRoomItem.toLiveRoomCardUiModel(preferFirstFrame: Boolean) = LiveRoomCardUiModel(
    roomId = roomId,
    title = title,
    coverUrl = resolvedCover(preferFirstFrame).ifBlank { face },
    hostName = uname,
    viewerCount = online,
    areaName = areaName,
)
