package com.android.purebilibili.feature.video.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.TodayWatchDislikedVideoSnapshot
import com.android.purebilibili.core.store.TodayWatchFeedbackStore
import com.android.purebilibili.core.store.withDislikedVideoFeedback
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.components.UpBadgeName
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.rememberHapticFeedback
import com.android.purebilibili.data.model.response.RecommendationFeedbackLocalAction
import com.android.purebilibili.data.model.response.RecommendationFeedbackReason
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.repository.ActionRepository
import com.android.purebilibili.data.repository.BlockedUpRepository
import com.android.purebilibili.feature.home.HomeFeedCardLayout
import com.android.purebilibili.feature.home.resolveHomeFeedCardLayout
import com.android.purebilibili.feature.video.ui.FollowBadgeTone
import com.android.purebilibili.feature.video.ui.resolveVideoFollowVisualPolicy
import com.android.purebilibili.navigation.VideoRoute
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 相关推荐默认跟随首页官方卡片的 4:3 封面；列表会按首页样式设置覆盖。
 */
internal const val RELATED_VIDEO_CARD_COVER_ASPECT_RATIO = 4f / 3f

internal const val RELATED_VIDEO_GRID_COLUMNS = 1

/**
 * Related Videos Header
 */
@Composable
fun RelatedVideosHeader() {
    AppSurface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "\u66f4\u591a\u63a8\u8350",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun resolveRelatedVideoCardPressScaleTarget(
    isPressed: Boolean,
    transitionEnabled: Boolean
): Float = 1f

@Suppress("UNUSED_PARAMETER")
internal fun shouldEnableRelatedVideoCoverCrossfade(
    transitionEnabled: Boolean
): Boolean = false

@Suppress("UNUSED_PARAMETER")
internal fun shouldTriggerRelatedVideoPressHaptic(
    isPressed: Boolean,
    transitionEnabled: Boolean
): Boolean = false

internal fun resolveRelatedVideoSharedElementSourceRoute(sourceRoute: String?): String {
    return sourceRoute
        ?.substringBefore("?")
        ?.takeIf { it.isNotBlank() }
        ?: VideoRoute.base
}

internal fun chunkRelatedVideosForHomeStyleGrid(
    videos: List<RelatedVideo>,
): List<List<RelatedVideo>> {
    if (videos.isEmpty()) return emptyList()
    return videos.chunked(RELATED_VIDEO_GRID_COLUMNS)
}

@Composable
internal fun rememberRelatedVideoCardLayout(): HomeFeedCardLayout {
    val context = LocalContext.current
    val homeFeedCardStyle by SettingsManager
        .getHomeFeedCardStyle(context)
        .collectAsStateWithLifecycle(initialValue = HomeFeedCardStyle.CURRENT)
    return remember(homeFeedCardStyle) {
        resolveHomeFeedCardLayout(homeFeedCardStyle)
    }
}

/** 相关推荐单列横卡：点击时冻结整卡与封面几何，由 Miuix NavTransition 接管飞行。 */
@Composable
fun RelatedVideoItem(
    video: RelatedVideo,
    isFollowed: Boolean = false,
    showUpBadge: Boolean = true,
    coverAspectRatio: Float = RELATED_VIDEO_CARD_COVER_ASPECT_RATIO,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }
    val screenHeightPx = remember(configuration.screenHeightDp, density) {
        with(density) { configuration.screenHeightDp.dp.toPx() }
    }
    val densityValue = density.density
    val sourceRoute = resolveRelatedVideoSharedElementSourceRoute(
        LocalVideoCardSharedElementSourceRoute.current
    )
    val cardCoordinatesRef = remember { object { var value: LayoutCoordinates? = null } }
    val coverCoordinatesRef = remember { object { var value: LayoutCoordinates? = null } }
    val context = LocalContext.current
    // Same URL + cache key as the list AsyncImage so return handoff reuses stationary pixels.
    val stationaryCoverUrl = remember(video.pic) {
        FormatUtils.resolveVideoCoverUrl(video.pic, useLowQuality = false)
    }
    val coverRequest = remember(stationaryCoverUrl) {
        ImageRequest.Builder(context)
            .data(stationaryCoverUrl)
            .crossfade(false)
            .memoryCacheKey(stationaryCoverUrl)
            .diskCacheKey(stationaryCoverUrl)
            .build()
    }
    val triggerRelatedVideoClick = {
        cardCoordinatesRef.value
            ?.takeIf { it.isAttached }
            ?.boundsInRoot()
            ?.let { bounds ->
                CardPositionManager.recordVideoCardPosition(
                    bvid = video.bvid,
                    sourceRoute = sourceRoute,
                    bounds = bounds,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    density = densityValue,
                    sourceCornerDp = 12,
                    coverBounds = coverCoordinatesRef.value
                        ?.takeIf { it.isAttached }
                        ?.boundsInRoot(),
                    sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                    sourceChromeSnapshot = VideoCardSourceChromeSnapshot(
                        title = video.title,
                        ownerName = video.owner.name,
                        ownerFaceUrl = video.owner.face,
                        viewText = FormatUtils.formatStat(video.stat.view.toLong()),
                        danmakuText = FormatUtils.formatStat(video.stat.danmaku.toLong()),
                        durationText = FormatUtils.formatDuration(video.duration),
                        followed = isFollowed,
                        // Related horizontal card keeps play/danmaku in the info column.
                        infoPresentation = com.android.purebilibili.core.ui.transition
                            .resolveVideoCardSourceInfoPresentation(
                                publishTimeText = "",
                                showStatsInInfo = true,
                            ),
                        coverUrl = stationaryCoverUrl,
                        coverCacheKey = stationaryCoverUrl,
                    ),
                )
            }
        onClick()
        Unit
    }
    val cardShape = AppShapes.container(ContainerLevel.Card)
    val coverShape = AppShapes.container(ContainerLevel.Field)
    val coverWidth = 144.dp
    val coverHeight = coverWidth / coverAspectRatio.coerceAtLeast(1f)
    // 排版对齐首页单列卡片:标题用 feed 紧凑级,统计用 labelSmall。
    val contentTypography = com.android.purebilibili.core.ui.feedContentTypography()
    // 标题固定两行占位:一行标题时不把下方元数据撑出大片空隙,列表内所有卡片对齐一致。
    val titleTwoLinesHeight = contentTypography.title.lineHeight.let { line ->
        if (line.isSp) line else contentTypography.title.fontSize * 1.2f
    }.let { with(density) { (it * 2).toDp() } }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                cardCoordinatesRef.value = coordinates
            }
            .clip(cardShape)
            .background(AppSurfaceTokens.cardContainer())
            .clickable(onClick = triggerRelatedVideoClick)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(coverWidth)
                .height(coverHeight)
                .onGloballyPositioned { coordinates ->
                    coverCoordinatesRef.value = coordinates
                }
                .clip(coverShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = coverRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
            AppText(
                text = FormatUtils.formatDuration(video.duration),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        blurRadius = 4f,
                        offset = Offset(0f, 1f)
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(coverHeight),
            // 标题固定两行占位,作者/播放量/弹幕紧随其后紧凑成组,不再 SpaceBetween 拉开间距。
            verticalArrangement = Arrangement.Top,
        ) {
            AppText(
                text = video.title,
                style = contentTypography.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(titleTwoLinesHeight)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                UpBadgeName(
                    name = video.owner.name,
                    inlineTrailingContent = if (isFollowed) {
                        {
                            val followVisualPolicy = resolveVideoFollowVisualPolicy(isFollowing = true, darkTheme = true)
                            AppText(
                                text = "已关注",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = when (followVisualPolicy.relatedBadgeTone) {
                                    FollowBadgeTone.PRIMARY -> MaterialTheme.colorScheme.primary
                                    null -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    } else {
                        null
                    },
                    leadingContent = if (
                        com.android.purebilibili.core.ui.LocalUpBadgeVisibility.current.showAvatars &&
                        video.owner.face.isNotEmpty()
                    ) {
                        {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(FormatUtils.fixImageUrl(video.owner.face))
                                    .crossfade(false)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    } else {
                        null
                    },
                    nameStyle = MaterialTheme.typography.labelMedium,
                    nameColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    badgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    badgeBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    showUpBadge = showUpBadge,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(
                        icon = Icons.Filled.PlayArrow,
                        text = FormatUtils.formatStat(video.stat.view.toLong())
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatItem(
                        icon = Icons.Filled.ChatBubble,
                        text = FormatUtils.formatStat(video.stat.danmaku.toLong())
                    )
                    if (onMoreClick != null) {
                        val moreHaptic = rememberHapticFeedback()
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    moreHaptic(HapticType.LIGHT)
                                    onMoreClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AppText(
                                text = "⋮",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun RelatedVideoGridRow(
    videos: List<RelatedVideo>,
    cardLayout: HomeFeedCardLayout,
    followingMids: Set<Long> = emptySet(),
    showUpBadge: Boolean = true,
    onVideoClick: (RelatedVideo) -> Unit,
    onVideoHidden: ((RelatedVideo) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var actionVideo by remember { mutableStateOf<RelatedVideo?>(null) }
    var blockCreatorRequest by remember {
        mutableStateOf<RelatedVideoBlockRequest?>(null)
    }
    var isBlockingCreator by remember { mutableStateOf(false) }
    val blockedUpRepository = remember(context) { BlockedUpRepository(context) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = cardLayout.outerPaddingDp.dp, vertical = 2.dp)
    ) {
        videos.firstOrNull()?.let { video ->
            RelatedVideoItem(
                video = video,
                isFollowed = video.owner.mid in followingMids,
                showUpBadge = showUpBadge,
                coverAspectRatio = cardLayout.coverAspectRatio,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onVideoClick(video) },
                onMoreClick = { actionVideo = video }
            )
        }
    }

    val pendingVideo = actionVideo
    if (pendingVideo != null) {
        RelatedVideoActionSheet(
            video = pendingVideo,
            onWatchLater = {
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        ActionRepository.toggleWatchLater(pendingVideo.aid, true)
                    }
                    val message = result.fold(
                        onSuccess = { "已添加到稍后再看" },
                        onFailure = { error -> error.message ?: "添加失败" }
                    )
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            },
            onReasonSelected = { reason ->
                if (reason.localAction == RecommendationFeedbackLocalAction.CREATOR &&
                    pendingVideo.owner.mid > 0L
                ) {
                    blockCreatorRequest = RelatedVideoBlockRequest(pendingVideo, reason)
                } else {
                    scope.launch {
                        recordRelatedVideoFeedback(
                            context = context,
                            video = pendingVideo,
                            reason = reason
                        )
                        if (shouldRemoveRelatedVideoAfterFeedback(reason)) {
                            onVideoHidden?.invoke(pendingVideo)
                        }
                        Toast.makeText(
                            context,
                            resolveRelatedFeedbackToast(reason),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onDismissRequest = { actionVideo = null }
        )
    }

    val blockRequest = blockCreatorRequest
    if (blockRequest != null) {
        AppAlertDialog(
            onDismissRequest = {
                if (!isBlockingCreator) {
                    blockCreatorRequest = null
                }
            },
            title = { AppText("屏蔽 UP 主") },
            text = {
                AppText(
                    "确定要屏蔽 ${blockRequest.video.owner.name.ifBlank { "该 UP 主" }} 吗？\n" +
                        "屏蔽后将不再推荐该 UP 主的视频。"
                )
            },
            confirmButton = {
                AppDialogAction(
                    onClick = {
                        if (!isBlockingCreator) {
                            isBlockingCreator = true
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    blockedUpRepository.blockUpWithBilibiliSync(
                                        mid = blockRequest.video.owner.mid,
                                        name = blockRequest.video.owner.name,
                                        face = blockRequest.video.owner.face
                                    )
                                }
                                recordRelatedVideoFeedback(
                                    context = context,
                                    video = blockRequest.video,
                                    reason = blockRequest.reason
                                )
                                onVideoHidden?.invoke(blockRequest.video)
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                isBlockingCreator = false
                                blockCreatorRequest = null
                            }
                        }
                    }
                ) {
                    AppText("屏蔽", color = Color.Red)
                }
            },
            dismissButton = {
                AppDialogAction(
                    onClick = {
                        if (!isBlockingCreator) {
                            blockCreatorRequest?.let { request ->
                                blockCreatorRequest = null
                                scope.launch {
                                    recordRelatedVideoFeedback(
                                        context = context,
                                        video = request.video,
                                        reason = request.reason
                                    )
                                    onVideoHidden?.invoke(request.video)
                                    Toast.makeText(
                                        context,
                                        resolveRelatedFeedbackToast(request.reason),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                ) {
                    AppText("暂不屏蔽")
                }
            }
        )
    }
}

private data class RelatedVideoBlockRequest(
    val video: RelatedVideo,
    val reason: RecommendationFeedbackReason,
)

private suspend fun recordRelatedVideoFeedback(
    context: android.content.Context,
    video: RelatedVideo,
    reason: RecommendationFeedbackReason
) {
    withContext(Dispatchers.IO) {
        val includeCreator = reason.localAction == RecommendationFeedbackLocalAction.CREATOR &&
            video.owner.mid > 0L
        val keywords = when (reason.localAction) {
            RecommendationFeedbackLocalAction.SIMILAR_CONTENT ->
                extractRelatedFeedbackKeywords(video.title)
            else -> emptySet()
        }
        val snapshot = TodayWatchFeedbackStore.getSnapshot(context).withDislikedVideoFeedback(
            video = TodayWatchDislikedVideoSnapshot(
                bvid = video.bvid,
                title = video.title,
                creatorName = video.owner.name,
                creatorMid = video.owner.mid,
                dislikedAtMillis = System.currentTimeMillis()
            ),
            keywords = keywords,
            includeCreatorSignal = includeCreator
        )
        TodayWatchFeedbackStore.saveSnapshot(context, snapshot)
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AppIcon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        AppText(
            text = text,
            style = com.android.purebilibili.core.ui.feedContentTypography().statistic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
