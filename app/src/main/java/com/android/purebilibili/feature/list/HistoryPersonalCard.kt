package com.android.purebilibili.feature.list

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.MediaContrastPalette
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.shouldUseVideoCardShellSharedBounds
import com.android.purebilibili.core.ui.transition.videoCardShellSharedBoundsOrEmpty
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.HistoryBusiness
import com.android.purebilibili.data.model.response.HistoryItem
import com.android.purebilibili.feature.personal.PersonalMediaCardFrame

internal fun resolveHistoryKindLabel(business: HistoryBusiness): String = when (business) {
    HistoryBusiness.ARCHIVE -> "视频"
    HistoryBusiness.PGC -> "番剧"
    HistoryBusiness.LIVE -> "直播"
    HistoryBusiness.ARTICLE -> "专栏"
    HistoryBusiness.UNKNOWN -> "未知"
}

internal fun resolveHistoryProgressLabel(progress: Int, duration: Int): String = when {
    progress == -1 -> "已看完"
    duration <= 0 -> "已看"
    progress <= 0 -> FormatUtils.formatDuration(duration)
    else -> "${FormatUtils.formatDuration(progress)}/${FormatUtils.formatDuration(duration)}"
}

internal fun canAddHistoryToWatchLater(item: HistoryItem): Boolean =
    item.business == HistoryBusiness.ARCHIVE && item.videoItem.id > 0L

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HistoryPersonalCard(
    item: HistoryItem,
    selected: Boolean,
    batchMode: Boolean,
    transitionEnabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onUpClick: (() -> Unit)?,
    onAddToWatchLater: (() -> Unit)?,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val video = item.videoItem
    var menuExpanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val sourceRoute = LocalVideoCardSharedElementSourceRoute.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val speedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val sharedElementReady = transitionEnabled &&
        video.bvid.isNotBlank() &&
        sourceRoute != null &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    val motionSpec = remember(sourceRoute, transitionEnabled, speedSettings) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRoute,
            transitionEnabled = transitionEnabled,
            speedSettings = speedSettings,
        )
    }
    val useSharedBounds = shouldUseVideoCardShellSharedBounds(
        sourceRoute = sourceRoute,
        transitionEnabled = sharedElementReady,
    )
    val cardBounds = remember { object { var value: androidx.compose.ui.geometry.Rect? = null } }
    val screenWidthPx = configuration.screenWidthDp * density.density
    val screenHeightPx = configuration.screenHeightDp * density.density
    val progressState = remember(item.progress, video.duration, video.view_at) {
        resolveVideoDisplayProgressState(
            serverProgressSec = item.progress,
            durationSec = video.duration,
            viewAt = video.view_at,
        )
    }
    val triggerClick = {
        if (!batchMode) {
            cardBounds.value?.let { bounds ->
                CardPositionManager.recordVideoCardPosition(
                    bvid = video.bvid,
                    sourceRoute = sourceRoute,
                    bounds = bounds,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    sourceCornerDp = 12,
                )
            }
        }
        onClick()
    }

    PersonalMediaCardFrame(
        modifier = modifier
            .videoCardShellSharedBoundsOrEmpty(
                enabled = useSharedBounds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bvid = video.bvid,
                sourceRoute = sourceRoute,
                motionSpec = motionSpec,
                clipShape = AppShapes.container(ContainerLevel.Card),
                crossfadeSourceContent = true,
            )
            .onGloballyPositioned { cardBounds.value = it.boundsInRoot() },
        selected = selected,
        headlineContent = {
            AppText(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        overlineContent = {
            AppText(
                text = resolveHistoryKindLabel(item.business),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        supportingContent = {
            Column {
                val owner = video.owner.name.takeIf { it.isNotBlank() }
                    ?: if (item.business == HistoryBusiness.PGC) "番剧" else "未知作者"
                AppText(
                    text = owner,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                FormatUtils.formatPublishTime(video.view_at).takeIf { it.isNotBlank() }?.let { viewedAt ->
                    AppText(
                        text = viewedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                    )
                }
            }
        },
        coverContent = {
            AsyncImage(
                model = FormatUtils.fixImageUrl(video.pic),
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        },
        coverOverlayContent = {
            AppSurface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacingTokens.ExtraSmall),
                shape = AppShapes.container(ContainerLevel.Tag),
                color = MediaContrastPalette.Scrim.copy(alpha = 0.76f),
            ) {
                AppText(
                    text = resolveHistoryProgressLabel(progressState.progressSec, video.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MediaContrastPalette.Foreground,
                    modifier = Modifier.padding(
                        horizontal = AppSpacingTokens.ExtraSmall,
                        vertical = AppSpacingTokens.Micro,
                    ),
                )
            }
            if (progressState.showProgressBar) {
                LinearProgressIndicator(
                    progress = { progressState.progressFraction },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                )
            }
        },
        trailingContent = if (batchMode) null else {
            {
                Box {
                    AppIconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget),
                    ) {
                        AppIcon(Icons.Filled.MoreVert, contentDescription = "历史记录操作")
                    }
                    AppDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        if (onUpClick != null) {
                            AppDropdownMenuItem(
                                text = { AppText("访问UP主") },
                                onClick = {
                                    menuExpanded = false
                                    onUpClick()
                                },
                            )
                        }
                        if (onAddToWatchLater != null) {
                            AppDropdownMenuItem(
                                text = { AppText("加入稍后再看") },
                                onClick = {
                                    menuExpanded = false
                                    onAddToWatchLater()
                                },
                            )
                        }
                        AppDropdownMenuItem(
                            text = { AppText("删除记录", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                        )
                    }
                }
            }
        },
        onClick = triggerClick,
        onLongClick = onLongClick,
    )
}
