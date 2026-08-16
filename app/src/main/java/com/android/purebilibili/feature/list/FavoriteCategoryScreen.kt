package com.android.purebilibili.feature.list

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppAssistChip
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.FavoritePgcStatus
import com.android.purebilibili.data.model.response.FavoriteSection
import com.android.purebilibili.data.repository.FavoriteCategoryItem
import com.android.purebilibili.feature.personal.PERSONAL_LIST_POSTER_ASPECT_RATIO
import com.android.purebilibili.feature.personal.PersonalMediaCardFrame


@Composable
fun FavoriteCategoryRoute(
    section: FavoriteSection,
    query: String,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onBangumiClick: (Long) -> Unit,
    onArticleClick: (Long, String) -> Unit,
    onTopicClick: (Long) -> Unit,
    onWebClick: (String, String) -> Unit,
    viewModel: FavoriteCategoryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(section) { viewModel.selectSection(section) }
    FavoriteCategoryContent(
        state = state,
        query = query,
        contentPadding = contentPadding,
        onPgcStatusSelected = viewModel::selectPgcStatus,
        onPublishedNotesSelected = viewModel::selectPublishedNotes,
        onRetry = viewModel::retry,
        onLoadMore = viewModel::loadMore,
        onToggleSelection = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onRemoveSelected = viewModel::removeSelected,
        onUpdateSelectedPgcStatus = viewModel::updateSelectedPgcStatus,
        onRemove = viewModel::remove,
        onUpdatePgcStatus = viewModel::updatePgcStatus,
        onBangumiClick = onBangumiClick,
        onArticleClick = onArticleClick,
        onTopicClick = onTopicClick,
        onWebClick = onWebClick,
    )
}

@Composable
private fun FavoriteCategoryContent(
    state: FavoriteCategoryUiState,
    query: String,
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    onPgcStatusSelected: (FavoritePgcStatus) -> Unit,
    onPublishedNotesSelected: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onRemoveSelected: () -> Unit,
    onUpdateSelectedPgcStatus: (FavoritePgcStatus) -> Unit,
    onRemove: (FavoriteCategoryItem) -> Unit,
    onUpdatePgcStatus: (FavoriteCategoryItem, FavoritePgcStatus) -> Unit,
    onBangumiClick: (Long) -> Unit,
    onArticleClick: (Long, String) -> Unit,
    onTopicClick: (Long) -> Unit,
    onWebClick: (String, String) -> Unit,
) {
    val visibleItems = state.items.filter { item ->
        query.isBlank() || item.title.contains(query, ignoreCase = true) ||
            item.subtitle.contains(query, ignoreCase = true)
    }
    Column(modifier = Modifier.fillMaxSize().padding(top = contentPadding.calculateTopPadding())) {
        if (state.section == FavoriteSection.BANGUMI || state.section == FavoriteSection.CINEMA) {
            FavoriteCategoryFilterRow(
                labels = FavoritePgcStatus.entries.map { it.label },
                selectedIndex = FavoritePgcStatus.entries.indexOf(state.pgcStatus),
                onSelected = { FavoritePgcStatus.entries.getOrNull(it)?.let(onPgcStatusSelected) },
            )
        } else if (state.section == FavoriteSection.NOTE) {
            FavoriteCategoryFilterRow(
                labels = listOf("未发布笔记", "公开笔记"),
                selectedIndex = if (state.publishedNotes) 1 else 0,
                onSelected = { onPublishedNotesSelected(it == 1) },
            )
        }

        if (state.selectedIds.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacingTokens.Medium),
                horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
            ) {
                if (state.section == FavoriteSection.BANGUMI || state.section == FavoriteSection.CINEMA) {
                    FavoritePgcStatus.entries.forEach { status ->
                        AppTextButton(onClick = { onUpdateSelectedPgcStatus(status) }) {
                            AppText("移至${status.label}")
                        }
                    }
                } else {
                    AppTextButton(onClick = onRemoveSelected) {
                        AppText("删除(${state.selectedIds.size})")
                    }
                }
                AppTextButton(onClick = onClearSelection) {
                    AppText("取消选择")
                }
            }
        }

        when {
            state.isLoading && state.items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppText("正在加载…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.error != null && state.items.isEmpty() -> Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AppText(state.error, color = MaterialTheme.colorScheme.error)
                AppButton(onClick = onRetry) { AppText("重试") }
            }
            visibleItems.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppText(
                    if (query.isBlank()) "暂无${state.section.label}收藏" else "未找到相关内容",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> FavoriteCategoryGrid(
                state = state.copy(items = visibleItems),
                bottomPadding = contentPadding.calculateBottomPadding(),
                onLoadMore = onLoadMore,
                onToggleSelection = onToggleSelection,
                onRemove = onRemove,
                onUpdatePgcStatus = onUpdatePgcStatus,
                onBangumiClick = onBangumiClick,
                onArticleClick = onArticleClick,
                onTopicClick = onTopicClick,
                onWebClick = onWebClick,
            )
        }
    }
}

@Composable
private fun FavoriteCategoryFilterRow(
    labels: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacingTokens.Medium),
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        labels.forEach { label ->
            val index = labels.indexOf(label)
            AppFilterChip(
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                label = { AppText(label) },
                leadingIcon = null,
            )
        }
    }
}

@Composable
private fun FavoriteCategoryGrid(
    state: FavoriteCategoryUiState,
    bottomPadding: androidx.compose.ui.unit.Dp,
    onLoadMore: () -> Unit,
    onToggleSelection: (Long) -> Unit,
    onRemove: (FavoriteCategoryItem) -> Unit,
    onUpdatePgcStatus: (FavoriteCategoryItem, FavoritePgcStatus) -> Unit,
    onBangumiClick: (Long) -> Unit,
    onArticleClick: (Long, String) -> Unit,
    onTopicClick: (Long) -> Unit,
    onWebClick: (String, String) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val columns = resolveFavoriteCategoryColumnCount(state.section, maxWidth.value)
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = AppSpacingTokens.Medium,
                end = AppSpacingTokens.Medium,
                top = AppSpacingTokens.Small,
                bottom = bottomPadding + AppSpacingTokens.Medium,
            ),
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
        ) {
            itemsIndexed(state.items, key = { _, item -> "${item.section}_${item.id}" }) { index, item ->
                if (index == state.items.lastIndex && state.hasMore && !state.isLoadingMore) {
                    LaunchedEffect(state.page, state.items.size) { onLoadMore() }
                }
                when (state.section) {
                    FavoriteSection.BANGUMI, FavoriteSection.CINEMA -> FavoritePgcCard(
                        item = item,
                        selected = item.id in state.selectedIds,
                        batchMode = state.selectedIds.isNotEmpty(),
                        selectedStatus = state.pgcStatus,
                        onClick = {
                            if (state.selectedIds.isNotEmpty()) onToggleSelection(item.id)
                            else onBangumiClick(item.id)
                        },
                        onLongClick = { onToggleSelection(item.id) },
                        onStatusSelected = { onUpdatePgcStatus(item, it) },
                        onRemove = { onRemove(item) },
                    )
                    FavoriteSection.TOPIC -> AppAssistChip(
                        onClick = { onTopicClick(item.id) },
                        label = { AppText("# ${item.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    else -> FavoriteGenericCategoryCard(
                        item = item,
                        selected = item.id in state.selectedIds,
                        batchMode = state.selectedIds.isNotEmpty(),
                        onClick = {
                            when (item.section) {
                                FavoriteSection.ARTICLE -> onArticleClick(item.id, item.title)
                                FavoriteSection.NOTE, FavoriteSection.COURSE -> onWebClick(item.url, item.title)
                                else -> Unit
                            }
                        },
                        onLongClick = if (item.section == FavoriteSection.NOTE) {
                            { onToggleSelection(item.id) }
                        } else null,
                        onRemove = { onRemove(item) },
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun FavoritePgcCard(
    item: FavoriteCategoryItem,
    selected: Boolean,
    batchMode: Boolean,
    selectedStatus: FavoritePgcStatus,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onStatusSelected: (FavoritePgcStatus) -> Unit,
    onRemove: () -> Unit,
) {
    AppSurface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = AppShapes.container(ContainerLevel.Card),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
    ) {
        Column {
            Box {
                AsyncImage(
                    model = FormatUtils.fixImageUrl(item.cover),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(PERSONAL_LIST_POSTER_ASPECT_RATIO),
                )
                if (item.badge.isNotBlank()) {
                    AppText(
                        text = item.badge,
                        modifier = Modifier.align(Alignment.TopStart).padding(AppSpacingTokens.Small),
                        color = com.android.purebilibili.core.ui.MediaContrastPalette.Foreground,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                if (!batchMode) {
                    AppIconButton(
                        onClick = onRemove,
                        modifier = Modifier.align(Alignment.TopEnd).size(AppChromeSizeTokens.MinimumTouchTarget),
                    ) {
                        AppIcon(Icons.Rounded.Close, contentDescription = "取消收藏")
                    }
                }
            }
            Column(
                modifier = Modifier.padding(AppSpacingTokens.Medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            ) {
                AppText(item.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                AppText(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                ) {
                    FavoritePgcStatus.entries.forEach { status ->
                        AppTextButton(onClick = { onStatusSelected(status) }) {
                            AppText(
                                status.label,
                                color = if (status == selectedStatus) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteGenericCategoryCard(
    item: FavoriteCategoryItem,
    selected: Boolean,
    batchMode: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    onRemove: () -> Unit,
) {
    PersonalMediaCardFrame(
        selected = selected,
        headlineContent = {
            AppText(item.title, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        overlineContent = item.badge.takeIf(String::isNotBlank)?.let { badge ->
            { AppText(badge, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall) }
        },
        supportingContent = item.subtitle.takeIf(String::isNotBlank)?.let { subtitle ->
            {
                AppText(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        coverContent = {
            AsyncImage(
                model = FormatUtils.fixImageUrl(item.cover),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        },
        trailingContent = if (batchMode) null else {
            {
                AppIconButton(onClick = onRemove, modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget)) {
                    AppIcon(Icons.Rounded.Close, contentDescription = "移除收藏")
                }
            }
        },
        onClick = if (batchMode) ({ onLongClick?.invoke(); Unit }) else onClick,
        onLongClick = onLongClick,
    )
}
