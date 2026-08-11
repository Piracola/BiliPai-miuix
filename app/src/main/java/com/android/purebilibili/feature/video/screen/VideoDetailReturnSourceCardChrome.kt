package com.android.purebilibili.feature.video.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.transition.LocalMiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeVisualFrame
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceLayout
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.ViewInfo

/**
 * Landing geometry for reconstructing the source card inside the Miuix flying entry.
 *
 * All sizes are **click-time screen pixels of the source card**. After inverse scale
 * `1/sourceScale` and the outer morph, the resting frame must match the stationary list card.
 */
internal data class VideoDetailReturnSourceCardLayout(
    val sourceScale: Float,
    val cardWidthPx: Float,
    val cardHeightPx: Float,
    val coverHeightPx: Float,
    /** Cover band width within the card (screen px); full width for STACKED. */
    val coverWidthPx: Float,
    val infoWidthPx: Float,
    val infoHeightPx: Float,
    val cardAnchorXInViewportPx: Float,
    val cardAnchorYInViewportPx: Float,
    val infoAnchorXInViewportPx: Float,
    val infoAnchorYInViewportPx: Float,
    val layout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
) {
    val canRender: Boolean
        get() = sourceScale > 0f &&
            cardWidthPx > 1f &&
            cardHeightPx > 1f &&
            infoWidthPx > 1f &&
            infoHeightPx > 1f &&
            layout != VideoCardSourceLayout.COVER_ONLY

    @Deprecated("Use infoWidthPx", ReplaceWith("infoWidthPx"))
    val sourceWidthPx: Float get() = infoWidthPx

    @Deprecated("Use infoHeightPx", ReplaceWith("infoHeightPx"))
    val sourceInfoHeightPx: Float get() = infoHeightPx

    @Deprecated("Use infoAnchorYInViewportPx", ReplaceWith("infoAnchorYInViewportPx"))
    val anchorYInViewportPx: Float get() = infoAnchorYInViewportPx

    @Deprecated("Use infoAnchorXInViewportPx", ReplaceWith("infoAnchorXInViewportPx"))
    val anchorXInViewportPx: Float get() = infoAnchorXInViewportPx
}

internal data class VideoDetailReturnSourceCardChromeModel(
    val title: String,
    val ownerName: String,
    val viewText: String,
    val danmakuText: String,
    val durationText: String = "",
    val followed: Boolean = false,
)

internal fun resolveVideoDetailReturnSourceCardChromeModel(
    info: ViewInfo?,
    snapshot: VideoCardSourceChromeSnapshot?,
): VideoDetailReturnSourceCardChromeModel? {
    if (info != null) {
        return VideoDetailReturnSourceCardChromeModel(
            title = info.title,
            ownerName = info.owner.name,
            viewText = FormatUtils.formatStat(info.stat.view.toLong()),
            danmakuText = FormatUtils.formatStat(info.stat.danmaku.toLong()),
            durationText = "",
            followed = false,
        )
    }
    val frozen = snapshot ?: return null
    return VideoDetailReturnSourceCardChromeModel(
        title = frozen.title,
        ownerName = frozen.ownerName,
        viewText = frozen.viewText,
        danmakuText = frozen.danmakuText,
        durationText = frozen.durationText,
        followed = frozen.followed,
    )
}

private fun emptyLayout(
    layout: VideoCardSourceLayout = VideoCardSourceLayout.COVER_ONLY,
) = VideoDetailReturnSourceCardLayout(
    sourceScale = 0f,
    cardWidthPx = 0f,
    cardHeightPx = 0f,
    coverHeightPx = 0f,
    coverWidthPx = 0f,
    infoWidthPx = 0f,
    infoHeightPx = 0f,
    cardAnchorXInViewportPx = 0f,
    cardAnchorYInViewportPx = 0f,
    infoAnchorXInViewportPx = 0f,
    infoAnchorYInViewportPx = 0f,
    layout = layout,
)

/**
 * 将全屏详情壳中的卡片几何反向补偿到点击时的源卡尺寸。
 *
 * - [VideoCardSourceLayout.STACKED]：封面在上、信息在下（推荐双列等）
 * - [VideoCardSourceLayout.SIDE_BY_SIDE]：封面在左、信息在右（分区横卡 / 相关推荐）
 */
internal fun resolveVideoDetailReturnSourceCardLayout(
    viewportWidthPx: Float,
    sourceBounds: Rect?,
    sourceCoverBounds: Rect?,
    sourceLayout: VideoCardSourceLayout? = null,
): VideoDetailReturnSourceCardLayout {
    val viewportWidth = viewportWidthPx.coerceAtLeast(1f)
    val bounds = sourceBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return emptyLayout()
    val coverBounds = sourceCoverBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return emptyLayout()
    val layout = sourceLayout ?: resolveVideoCardSourceLayout(bounds, coverBounds)
    val sourceScale = (bounds.width / viewportWidth).coerceIn(0.01f, 1f)
    val cardAnchorX = 0f
    val cardAnchorY = 0f
    return when (layout) {
        VideoCardSourceLayout.STACKED -> {
            val horizontalTolerance = bounds.width * 0.1f
            val isFullWidthCover = coverBounds.left <= bounds.left + horizontalTolerance &&
                coverBounds.right >= bounds.right - horizontalTolerance
            val isVerticallyInsideCard = coverBounds.top >= bounds.top - 1f &&
                coverBounds.bottom in (bounds.top + 1f)..(bounds.bottom + 1f)
            if (!isFullWidthCover || !isVerticallyInsideCard) {
                return emptyLayout(layout)
            }
            val coverHeight = (coverBounds.bottom - bounds.top).coerceAtLeast(0f)
            val infoHeight = (bounds.bottom - coverBounds.bottom).coerceAtLeast(0f)
            if (infoHeight <= 1f || coverHeight <= 1f) {
                return emptyLayout(layout)
            }
            VideoDetailReturnSourceCardLayout(
                sourceScale = sourceScale,
                cardWidthPx = bounds.width,
                cardHeightPx = bounds.height,
                coverHeightPx = coverHeight,
                coverWidthPx = bounds.width,
                infoWidthPx = bounds.width,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = cardAnchorX,
                cardAnchorYInViewportPx = cardAnchorY,
                infoAnchorXInViewportPx = 0f,
                infoAnchorYInViewportPx = coverHeight / sourceScale,
                layout = layout,
            )
        }
        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            val coverOnLeft = coverBounds.center.x <= bounds.center.x
            val coverNarrower = coverBounds.width < bounds.width * 0.85f
            val coverWidth: Float
            val coverHeight: Float
            val infoWidth: Float
            if (coverOnLeft && coverNarrower) {
                coverWidth = (coverBounds.right - bounds.left).coerceAtLeast(0f)
                coverHeight = (coverBounds.bottom - coverBounds.top)
                    .coerceAtLeast(1f)
                    .coerceAtMost(bounds.height)
                infoWidth = (bounds.right - coverBounds.right).coerceAtLeast(0f)
            } else {
                // Explicit SIDE_BY_SIDE (partition/related) with imperfect cover measure:
                // left ~38% band matches HomeStyleSingleColumn cover vs full-width row.
                coverWidth = bounds.width * 0.38f
                coverHeight = bounds.height * 0.85f
                infoWidth = bounds.width - coverWidth
            }
            val infoHeight = bounds.height.coerceAtLeast(0f)
            if (infoWidth <= 1f || infoHeight <= 1f || coverWidth <= 1f) {
                return emptyLayout(layout)
            }
            VideoDetailReturnSourceCardLayout(
                sourceScale = sourceScale,
                cardWidthPx = bounds.width,
                cardHeightPx = bounds.height,
                coverHeightPx = coverHeight,
                coverWidthPx = coverWidth,
                infoWidthPx = infoWidth,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = cardAnchorX,
                cardAnchorYInViewportPx = cardAnchorY,
                infoAnchorXInViewportPx = coverWidth / sourceScale,
                infoAnchorYInViewportPx = 0f,
                layout = layout,
            )
        }
        VideoCardSourceLayout.COVER_ONLY -> emptyLayout(layout)
    }
}

/** Entry-space cover band height (STACKED top band). */
internal fun resolveVideoDetailReturnCoverHeightInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float {
    if (!layout.canRender) return 0f
    return layout.coverHeightPx / layout.sourceScale
}

/** Entry-space cover band width (SIDE_BY_SIDE left band). */
internal fun resolveVideoDetailReturnCoverWidthInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float {
    if (!layout.canRender) return 0f
    return layout.coverWidthPx / layout.sourceScale
}

/**
 * 飞行详情内的来源卡落位层。
 *
 * - **STACKED**（推荐双列）：壳在播放器下 + 上方媒体；信息文案在封面下
 * - **SIDE_BY_SIDE**（分区横卡 / 相关）：整卡在飞行层重绘左封面图 + 右文字，避免
 *   把全屏播放器压进矮横卡变成黑块
 */
@Composable
internal fun BoxScope.VideoDetailReturnSourceCardChrome(
    sourceBounds: Rect?,
    sourceCoverBounds: Rect?,
    morphDepthProgressProvider: () -> Float,
    modifier: Modifier = Modifier,
    sourceLayout: VideoCardSourceLayout? = null,
    chromeModel: VideoDetailReturnSourceCardChromeModel? = null,
    info: ViewInfo? = null,
    sourceChromeSnapshot: VideoCardSourceChromeSnapshot? = null,
    coverUrl: String? = null,
    phaseProvider: () -> VideoCardTransitionBackgroundPhase = {
        VideoCardTransitionBackgroundPhase.RETURNING
    },
    isReturnGestureInProgressProvider: () -> Boolean = { true },
) {
    val model = chromeModel
        ?: resolveVideoDetailReturnSourceCardChromeModel(info, sourceChromeSnapshot)
        ?: return
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val miuixHost = LocalMiuixVideoCardTransitionState.current
    val viewportWidthPx = miuixHost.layoutWidthProvider().takeIf { it > 1f }
        ?: with(density) { configuration.screenWidthDp.dp.toPx() }
    val effectiveLayoutHint = sourceLayout ?: miuixHost.sourceLayout
    val layout = resolveVideoDetailReturnSourceCardLayout(
        viewportWidthPx = viewportWidthPx,
        sourceBounds = sourceBounds,
        sourceCoverBounds = sourceCoverBounds,
        sourceLayout = effectiveLayoutHint,
    )
    if (!layout.canRender) return

    val cardWidth = with(density) { layout.cardWidthPx.toDp() }
    val cardHeight = with(density) { layout.cardHeightPx.toDp() }
    val cardAnchorX = with(density) { layout.cardAnchorXInViewportPx.toDp() }
    val cardAnchorY = with(density) { layout.cardAnchorYInViewportPx.toDp() }
    val coverWidth = with(density) { layout.coverWidthPx.toDp() }
    val coverHeight = with(density) { layout.coverHeightPx.toDp() }
    val infoWidth = with(density) { layout.infoWidthPx.toDp() }
    val infoHeight = with(density) { layout.infoHeightPx.toDp() }
    val infoAnchorX = with(density) { layout.infoAnchorXInViewportPx.toDp() }
    val infoAnchorY = with(density) { layout.infoAnchorYInViewportPx.toDp() }
    val inverseScale = 1f / layout.sourceScale
    val resolvedCoverUrl = coverUrl?.trim()?.takeIf { it.isNotBlank() }
        ?: info?.pic?.trim()?.takeIf { it.isNotBlank() }
    val coverRequest = remember(resolvedCoverUrl) {
        resolvedCoverUrl?.let { url ->
            ImageRequest.Builder(context)
                .data(FormatUtils.resolveVideoCoverUrl(url, useLowQuality = false))
                .crossfade(false)
                .build()
        }
    }

    fun Modifier.landingLayer(): Modifier = graphicsLayer {
        val frame = resolveVideoCardSourceChromeVisualFrame(
            morphDepthProgress = morphDepthProgressProvider(),
            phase = phaseProvider(),
            isReturnGestureInProgress = isReturnGestureInProgressProvider(),
            sourceLayout = layout.layout,
        )
        scaleX = inverseScale * frame.layoutScaleMultiplier
        scaleY = inverseScale * frame.layoutScaleMultiplier
        transformOrigin = TransformOrigin(0f, 0f)
        alpha = frame.alpha
    }

    when (layout.layout) {
        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            // Complete horizontal card on the flying layer (covers black player strip).
            Box(
                modifier = modifier
                    .zIndex(2f)
                    .align(Alignment.TopStart)
                    .offset(x = cardAnchorX, y = cardAnchorY)
                    .width(cardWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .background(AppSurfaceTokens.cardContainer())
                    .padding(AppSpacingTokens.Small),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(coverWidth.coerceAtMost(cardWidth * 0.5f))
                            .height(coverHeight.coerceAtMost(cardHeight - AppSpacingTokens.Small * 2))
                            .clip(RoundedCornerShape(AppSpacingTokens.Small))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        if (coverRequest != null) {
                            AsyncImage(
                                model = coverRequest,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        if (model.durationText.isNotBlank()) {
                            AppText(
                                text = model.durationText,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(AppSpacingTokens.ExtraSmall),
                            )
                        }
                    }
                    SideBySideInfoColumn(
                        model = model,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
        VideoCardSourceLayout.STACKED -> {
            Box(
                modifier = modifier
                    .zIndex(-1f)
                    .align(Alignment.TopStart)
                    .offset(x = cardAnchorX, y = cardAnchorY)
                    .width(cardWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .background(AppSurfaceTokens.cardContainer()),
            )
            Column(
                modifier = modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .offset(x = infoAnchorX, y = infoAnchorY)
                    .width(infoWidth)
                    .height(infoHeight)
                    .landingLayer()
                    .padding(
                        horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro,
                        vertical = AppSpacingTokens.Small,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
            ) {
                LandingInfoTexts(model = model, info = info)
            }
        }
        VideoCardSourceLayout.COVER_ONLY -> Unit
    }
}

@Composable
private fun SideBySideInfoColumn(
    model: VideoDetailReturnSourceCardChromeModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        AppText(
            text = model.title,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AppText(
            text = buildString {
                append("UP ")
                append(model.ownerName)
            },
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
        ) {
            AppText(
                text = model.viewText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            AppText(
                text = model.danmakuText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LandingInfoTexts(
    model: VideoDetailReturnSourceCardChromeModel,
    info: ViewInfo?,
) {
    AppText(
        text = model.title,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    AppText(
        text = buildString {
            append(model.ownerName)
            if (model.followed) {
                append("  ·  已关注")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
    AppText(
        text = buildString {
            append(model.viewText)
            append("播放  ·  ")
            append(model.danmakuText)
            append("弹幕")
            if (model.durationText.isNotBlank()) {
                append("  ·  ")
                append(model.durationText)
            } else if (info != null && info.pubdate > 0L) {
                append("  ·  ")
                append(FormatUtils.formatPublishTime(info.pubdate))
            }
        },
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
