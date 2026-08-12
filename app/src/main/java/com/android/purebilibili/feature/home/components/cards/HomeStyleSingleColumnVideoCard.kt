package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.MediaContrastPalette

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.feedContentTypography
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.components.UpBadgeName
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.shouldUseVideoCardShellSharedBounds
import com.android.purebilibili.core.ui.transition.videoCardShellSharedBoundsOrEmpty
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.util.HapticType
import com.android.purebilibili.core.util.rememberHapticFeedback
import com.android.purebilibili.data.model.response.VideoItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.PlayArrow
import kotlinx.coroutines.launch

internal val HOME_STYLE_SINGLE_COLUMN_COVER_WIDTH = AppSpacingTokens.TripleExtraLarge * 3

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HomeStyleSingleColumnVideoCard(
    video: VideoItem,
    sourceRoute: String,
    coverAspectRatio: Float,
    transitionEnabled: Boolean,
    sharedTransitionEnabled: Boolean = transitionEnabled,
    isFollowing: Boolean = false,
    showUpBadge: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onMoreClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val contentTypography = feedContentTypography()
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }
    val screenHeightPx = remember(configuration.screenHeightDp, density) {
        with(density) { configuration.screenHeightDp.dp.toPx() }
    }
    val latestOnClick = rememberUpdatedState(onClick)
    val clickScope = rememberCoroutineScope()
    var forceSharedTransitionForClick by remember { mutableStateOf(false) }
    val effectiveTransitionEnabled = transitionEnabled || forceSharedTransitionForClick
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sharedReady = effectiveTransitionEnabled &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    val motionSettings = LocalVideoSharedTransitionSpeedSettings.current
    val motionSpec = remember(sourceRoute, effectiveTransitionEnabled, motionSettings) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRoute,
            transitionEnabled = effectiveTransitionEnabled,
            speedSettings = motionSettings,
        )
    }
    val cardBounds = remember { object { var value: Rect? = null } }
    val coverBounds = remember { object { var value: Rect? = null } }
    val cardShape = AppShapes.container(ContainerLevel.Card)
    val coverShape = AppShapes.container(ContainerLevel.Field)
    val useCardShellSharedBounds = shouldUseVideoCardShellSharedBounds(
        sourceRoute = sourceRoute,
        transitionEnabled = sharedReady,
    )
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
    val triggerClick = {
        cardBounds.value?.let { bounds ->
            CardPositionManager.recordVideoCardPosition(
                bvid = video.bvid,
                sourceRoute = sourceRoute,
                bounds = bounds,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx,
                density = density.density,
                sourceCornerDp = 12,
                coverBounds = coverBounds.value,
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
                sourceChromeSnapshot = VideoCardSourceChromeSnapshot(
                    title = video.title,
                    ownerName = video.owner.name,
                    ownerFaceUrl = video.owner.face,
                    viewText = FormatUtils.formatStat(video.stat.view.toLong()),
                    danmakuText = FormatUtils.formatStat(video.stat.danmaku.toLong()),
                    durationText = FormatUtils.formatDuration(video.duration),
                    followed = isFollowing,
                    // Single-column paints play/danmaku in the info column (not on cover only).
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
        if (sharedTransitionEnabled && !transitionEnabled) {
            forceSharedTransitionForClick = true
            clickScope.launch {
                withFrameNanos { }
                withFrameNanos { }
                latestOnClick.value()
            }
        } else {
            latestOnClick.value()
        }
        Unit
    }
    val coverHeight = HOME_STYLE_SINGLE_COLUMN_COVER_WIDTH /
        coverAspectRatio.coerceAtLeast(1f)

    Row(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                cardBounds.value = coordinates.boundsInRoot()
            }
            .videoCardShellSharedBoundsOrEmpty(
                enabled = useCardShellSharedBounds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bvid = video.bvid,
                sourceRoute = sourceRoute,
                motionSpec = motionSpec,
                clipShape = cardShape,
                crossfadeSourceContent = true,
            )
            .clip(cardShape)
            .background(AppSurfaceTokens.cardContainer())
            .clickable(onClick = triggerClick)
            .padding(AppSpacingTokens.Small),
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(HOME_STYLE_SINGLE_COLUMN_COVER_WIDTH)
                .height(coverHeight)
                .onGloballyPositioned { coordinates ->
                    coverBounds.value = coordinates.boundsInRoot()
                }
                .clip(coverShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = coverRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            AppText(
                text = FormatUtils.formatDuration(video.duration),
                color = MediaContrastPalette.Foreground,
                style = contentTypography.coverBadge.copy(
                    fontWeight = FontWeight.SemiBold,
                    shadow = Shadow(
                        color = MediaContrastPalette.Scrim.copy(alpha = 0.64f),
                        blurRadius = 4f,
                        offset = Offset(0f, 1f),
                    ),
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro),
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(coverHeight),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                AppText(
                    text = video.title,
                    modifier = Modifier.weight(1f),
                    style = contentTypography.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (onMoreClick != null) {
                    val moreHaptic = rememberHapticFeedback()
                    Box(
                        modifier = Modifier
                            .size(AppChromeSizeTokens.MinimumTouchTarget)
                            .clip(CircleShape)
                            .semantics { contentDescription = "更多操作" }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) {
                                moreHaptic(HapticType.LIGHT)
                                onMoreClick()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        AppText(
                            text = "⋮",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            UpBadgeName(
                name = video.owner.name,
                inlineTrailingContent = if (isFollowing) {
                    {
                        AppText(
                            text = "已关注",
                            style = contentTypography.coverBadge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                } else {
                    null
                },
                nameStyle = contentTypography.author,
                nameColor = MaterialTheme.colorScheme.onSurfaceVariant,
                badgeTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                badgeBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                showUpBadge = showUpBadge,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                SingleColumnStatItem(
                    icon = Icons.Filled.PlayArrow,
                    text = FormatUtils.formatStat(video.stat.view.toLong()),
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
                SingleColumnStatItem(
                    icon = Icons.Filled.ChatBubble,
                    text = FormatUtils.formatStat(video.stat.danmaku.toLong()),
                )
            }
        }
    }
}

@Composable
private fun SingleColumnStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    val contentTypography = feedContentTypography()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(AppSpacingTokens.Medium + AppSpacingTokens.Micro),
        )
        AppText(
            text = text,
            style = contentTypography.statistic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
