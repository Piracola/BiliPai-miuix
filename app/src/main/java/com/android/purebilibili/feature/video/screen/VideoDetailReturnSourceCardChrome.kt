package com.android.purebilibili.feature.video.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.MediaContrastPalette
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.feedContentTypography
import com.android.purebilibili.core.ui.transition.LocalMiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeVisualFrame
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceLayout
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.ViewInfo
import com.android.purebilibili.feature.home.resolveHomeCardInfoSurfaceAppearance
import kotlin.math.roundToInt

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
    /** Cover band width within the card (screen px); full width for STACKED when cover is flush. */
    val coverWidthPx: Float,
    /**
     * Cover origin inside the card (screen px). Non-zero when list cover is inset
     * (e.g. single-column padding) — clip must start here or land frame looks larger then shrinks.
     */
    val coverOffsetXPx: Float = 0f,
    val coverOffsetYPx: Float = 0f,
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
    val viewText: String = "",
    val danmakuText: String = "",
    val durationText: String = "",
    val followed: Boolean = false,
    /** Mirrors list-card info rows frozen at click; drives what LandingInfoTexts paints. */
    val infoPresentation: com.android.purebilibili.core.ui.transition.VideoCardSourceInfoPresentation =
        com.android.purebilibili.core.ui.transition.VideoCardSourceInfoPresentation(),
)

/**
 * Prefer click-time [snapshot] for layout flags so home (title/UP/发布于) does not pick up
 * detail ViewInfo stats that were only on the cover as badges.
 */
internal fun resolveVideoDetailReturnSourceCardChromeModel(
    info: ViewInfo?,
    snapshot: VideoCardSourceChromeSnapshot?,
): VideoDetailReturnSourceCardChromeModel? {
    val frozen = snapshot
    if (frozen != null) {
        return VideoDetailReturnSourceCardChromeModel(
            title = frozen.title.ifBlank { info?.title.orEmpty() },
            ownerName = frozen.ownerName.ifBlank { info?.owner?.name.orEmpty() },
            viewText = frozen.viewText,
            danmakuText = frozen.danmakuText,
            durationText = frozen.durationText,
            followed = frozen.followed,
            infoPresentation = frozen.infoPresentation,
        ).takeIf { it.title.isNotBlank() || it.ownerName.isNotBlank() }
    }
    if (info == null) return null
    // No snapshot: conservative home-like info (title / UP / publish), no invented 弹幕 row.
    val publish = if (info.pubdate > 0L) FormatUtils.formatPublishTime(info.pubdate) else ""
    return VideoDetailReturnSourceCardChromeModel(
        title = info.title,
        ownerName = info.owner.name,
        followed = false,
        infoPresentation = com.android.purebilibili.core.ui.transition.VideoCardSourceInfoPresentation(
            publishTimeText = publish,
            showStatsInInfo = false,
        ),
    )
}

/** Build the tertiary info line exactly as the list card would. */
internal fun resolveVideoDetailReturnInfoSecondaryLine(
    model: VideoDetailReturnSourceCardChromeModel,
): String {
    val presentation = model.infoPresentation
    if (presentation.showStatsInInfo) {
        return buildString {
            if (model.viewText.isNotBlank()) {
                append(model.viewText)
                if (!model.viewText.endsWith("播放")) append("播放")
            }
            if (model.danmakuText.isNotBlank()) {
                if (isNotEmpty()) append("  ·  ")
                append(model.danmakuText)
                if (!model.danmakuText.endsWith("弹幕") && !model.danmakuText.endsWith("评论")) {
                    append("弹幕")
                }
            }
            if (model.durationText.isNotBlank()) {
                if (isNotEmpty()) append("  ·  ")
                append(model.durationText)
            }
        }
    }
    return presentation.publishTimeText
}

/**
 * Landing info plate colors for the flying chrome.
 *
 * When the list froze [useTintedInfoSurface], reuse home info-surface alphas
 * (without realtime haze/liquid — flying overlay has no wallpaper sample).
 */
internal data class VideoDetailReturnInfoSurfaceSpec(
    val useTintedSurface: Boolean,
    val containerColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
)

internal fun resolveVideoDetailReturnInfoSurfaceSpec(
    useTintedInfoSurface: Boolean,
    isDarkTheme: Boolean,
    baseContainerColor: Color,
): VideoDetailReturnInfoSurfaceSpec {
    if (!useTintedInfoSurface) {
        return VideoDetailReturnInfoSurfaceSpec(
            useTintedSurface = false,
            containerColor = baseContainerColor,
            borderColor = Color.Transparent,
            borderWidth = 0.dp,
        )
    }
    val appearance = resolveHomeCardInfoSurfaceAppearance(
        wallpaperTintEnabled = true,
        isDarkTheme = isDarkTheme,
        isDataSaverActive = false,
        hasWallpaperHazeState = false,
        hasLayerBackdrop = false,
        blurEnabled = true,
    )
    return VideoDetailReturnInfoSurfaceSpec(
        useTintedSurface = true,
        containerColor = baseContainerColor.copy(alpha = appearance.containerAlpha),
        borderColor = MediaContrastPalette.Foreground.copy(alpha = appearance.borderAlpha),
        borderWidth = AppSpacingTokens.Micro * 0.4f,
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
    coverOffsetXPx = 0f,
    coverOffsetYPx = 0f,
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
 * - [VideoCardSourceLayout.STACKED]：实时画面在上、信息在下（推荐双列等）
 * - [VideoCardSourceLayout.SIDE_BY_SIDE]：实时画面在左、信息在右（分区横卡等）
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
            // Use measured cover rect (not cardTop→coverBottom) so 4:3 home covers match
            // list pixels; expanding to card top over-clips and lands as a shrink.
            val coverHeight = coverBounds.height.coerceAtLeast(0f)
            val coverOffsetY = (coverBounds.top - bounds.top).coerceAtLeast(0f)
            val infoHeight = (bounds.bottom - coverBounds.bottom).coerceAtLeast(0f)
            if (infoHeight <= 1f || coverHeight <= 1f) {
                return emptyLayout(layout)
            }
            VideoDetailReturnSourceCardLayout(
                sourceScale = sourceScale,
                cardWidthPx = bounds.width,
                cardHeightPx = bounds.height,
                coverHeightPx = coverHeight,
                coverWidthPx = coverBounds.width.coerceAtMost(bounds.width),
                coverOffsetXPx = (coverBounds.left - bounds.left).coerceAtLeast(0f),
                coverOffsetYPx = coverOffsetY,
                infoWidthPx = bounds.width,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = cardAnchorX,
                cardAnchorYInViewportPx = cardAnchorY,
                infoAnchorXInViewportPx = 0f,
                infoAnchorYInViewportPx = (coverOffsetY + coverHeight) / sourceScale,
                layout = layout,
            )
        }
        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            val coverOnLeft = coverBounds.center.x <= bounds.center.x
            val coverNarrower = coverBounds.width < bounds.width * 0.85f
            val coverWidth: Float
            val coverHeight: Float
            val coverOffsetX: Float
            val coverOffsetY: Float
            val infoWidth: Float
            if (coverOnLeft && coverNarrower) {
                // Exact measured cover box (includes list padding inset).
                coverWidth = coverBounds.width.coerceAtLeast(1f)
                coverHeight = coverBounds.height
                    .coerceAtLeast(1f)
                    .coerceAtMost(bounds.height)
                coverOffsetX = (coverBounds.left - bounds.left).coerceAtLeast(0f)
                coverOffsetY = (coverBounds.top - bounds.top).coerceAtLeast(0f)
                infoWidth = (bounds.right - coverBounds.right).coerceAtLeast(0f)
            } else {
                // Explicit SIDE_BY_SIDE with imperfect cover measure:
                // left ~38% band matches HomeStyleSingleColumn cover vs full-width row.
                coverWidth = bounds.width * 0.38f
                coverHeight = bounds.height * 0.85f
                coverOffsetX = 0f
                coverOffsetY = (bounds.height - coverHeight) / 2f
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
                coverOffsetXPx = coverOffsetX,
                coverOffsetYPx = coverOffsetY,
                infoWidthPx = infoWidth,
                infoHeightPx = infoHeight,
                cardAnchorXInViewportPx = cardAnchorX,
                cardAnchorYInViewportPx = cardAnchorY,
                infoAnchorXInViewportPx = (coverOffsetX + coverWidth) / sourceScale,
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

/** Entry-space cover band width (SIDE_BY_SIDE / inset STACKED). */
internal fun resolveVideoDetailReturnCoverWidthInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float {
    if (!layout.canRender) return 0f
    return layout.coverWidthPx / layout.sourceScale
}

/** Entry-space cover left inset inside the flying card. */
internal fun resolveVideoDetailReturnCoverOffsetXInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float {
    if (!layout.canRender) return 0f
    return layout.coverOffsetXPx / layout.sourceScale
}

/** Entry-space cover top inset inside the flying card. */
internal fun resolveVideoDetailReturnCoverOffsetYInEntryPx(
    layout: VideoDetailReturnSourceCardLayout,
): Float {
    if (!layout.canRender) return 0f
    return layout.coverOffsetYPx / layout.sourceScale
}

/**
 * Media geometry inside the full-size detail entry.
 *
 * The child is actually measured at this size instead of drawing full-player pixels through a
 * shrinking clip. That keeps [ContentScale.Crop] identical to the stationary list cover at the
 * handoff frame, including 4:3 covers that are taller than the detail player viewport.
 */
internal data class VideoDetailReturnMediaLayoutFrame(
    val offsetXPx: Int,
    val offsetYPx: Int,
    val widthPx: Int,
    val heightPx: Int,
)

internal fun resolveVideoDetailReturnMediaLayoutFrame(
    containerWidthPx: Int,
    containerHeightPx: Int,
    landingLayout: VideoDetailReturnSourceCardLayout?,
    handoffProgress: Float,
): VideoDetailReturnMediaLayoutFrame {
    val safeContainerWidth = containerWidthPx.coerceAtLeast(1)
    val safeContainerHeight = containerHeightPx.coerceAtLeast(1)
    val landing = landingLayout?.takeIf { it.canRender }
    val progress = if (landing == null) 0f else handoffProgress.coerceIn(0f, 1f)
    fun interpolate(start: Float, end: Float): Int =
        (start + (end - start) * progress).roundToInt()

    val targetWidth = landing
        ?.let(::resolveVideoDetailReturnCoverWidthInEntryPx)
        ?.takeIf { it > 1f }
        ?: safeContainerWidth.toFloat()
    val targetHeight = landing
        ?.let(::resolveVideoDetailReturnCoverHeightInEntryPx)
        ?.takeIf { it > 1f }
        ?: safeContainerHeight.toFloat()
    val targetOffsetX = landing
        ?.let(::resolveVideoDetailReturnCoverOffsetXInEntryPx)
        ?: 0f
    val targetOffsetY = landing
        ?.let(::resolveVideoDetailReturnCoverOffsetYInEntryPx)
        ?: 0f

    return VideoDetailReturnMediaLayoutFrame(
        offsetXPx = interpolate(0f, targetOffsetX),
        offsetYPx = interpolate(0f, targetOffsetY),
        widthPx = interpolate(safeContainerWidth.toFloat(), targetWidth).coerceAtLeast(1),
        heightPx = interpolate(safeContainerHeight.toFloat(), targetHeight).coerceAtLeast(1),
    )
}

/**
 * Keeps the host at the normal player size while remeasuring and placing its media child at the
 * returning list cover geometry. Providers are read during layout so gesture frames do not need
 * to recompose the player subtree.
 */
internal fun Modifier.videoDetailReturnMediaLayout(
    landingLayout: VideoDetailReturnSourceCardLayout?,
    handoffProgressProvider: () -> Float,
): Modifier = layout { measurable, constraints ->
    if (!constraints.hasBoundedWidth || !constraints.hasBoundedHeight) {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    } else {
        val frame = resolveVideoDetailReturnMediaLayoutFrame(
            containerWidthPx = constraints.maxWidth,
            containerHeightPx = constraints.maxHeight,
            landingLayout = landingLayout,
            handoffProgress = handoffProgressProvider(),
        )
        val placeable = measurable.measure(
            Constraints.fixed(
                width = frame.widthPx,
                height = frame.heightPx,
            ),
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            // Source bounds are physical screen coordinates; do not mirror this offset in RTL.
            placeable.place(frame.offsetXPx, frame.offsetYPx)
        }
    }
}

/**
 * 飞行详情内的来源卡落位层 — 与首页同源：整卡壳 + 实时画面 + 文字，无静态封面遮挡层。
 *
 * - **STACKED**（推荐双列）：壳在播放器下；实时画面在上方 cover 带；信息在下方
 * - **SIDE_BY_SIDE**（分区横卡）：壳在播放器下；实时画面在左侧 cover 带；信息在右侧
 *   （禁止再叠一层 AsyncImage 封面，否则会像双层相互遮挡）
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
    @Suppress("UNUSED_PARAMETER") coverUrl: String? = null,
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
    val coverHeight = with(density) { layout.coverHeightPx.toDp() }
    val infoWidth = with(density) { layout.infoWidthPx.toDp() }
    val infoHeight = with(density) { layout.infoHeightPx.toDp() }
    val infoAnchorX = with(density) { layout.infoAnchorXInViewportPx.toDp() }
    val infoAnchorY = with(density) { layout.infoAnchorYInViewportPx.toDp() }
    val inverseScale = 1f / layout.sourceScale

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

    val isDarkTheme = AppSurfaceTokens.chromeBackground().luminance() < 0.5f
    val baseContainer = AppSurfaceTokens.cardContainer()
    val infoSurfaceSpec = remember(
        model.infoPresentation.useTintedInfoSurface,
        isDarkTheme,
        baseContainer,
    ) {
        resolveVideoDetailReturnInfoSurfaceSpec(
            useTintedInfoSurface = model.infoPresentation.useTintedInfoSurface,
            isDarkTheme = isDarkTheme,
            baseContainerColor = baseContainer,
        )
    }
    // STACKED: bottom corners only (matches VideoCard info plate under cover).
    val stackedInfoShape = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = AppSpacingTokens.Small,
        bottomEnd = AppSpacingTokens.Small,
    )
    val sideInfoShape = RoundedCornerShape(AppSpacingTokens.Small)

    fun Modifier.landingInfoSurface(shape: RoundedCornerShape): Modifier {
        if (!infoSurfaceSpec.useTintedSurface) {
            return this.background(infoSurfaceSpec.containerColor, shape)
        }
        return this
            .clip(shape)
            .background(color = infoSurfaceSpec.containerColor, shape = shape)
            .border(
                width = infoSurfaceSpec.borderWidth,
                color = infoSurfaceSpec.borderColor,
                shape = shape,
            )
    }

    when (layout.layout) {
        // Home whole-card contract; only info region placement differs:
        // STACKED = below cover, SIDE_BY_SIDE = right of cover.
        // Live media owns the cover band; themed info plate mirrors list card.
        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            Box(
                modifier = modifier
                    .zIndex(-1f)
                    .align(Alignment.TopStart)
                    .offset(x = cardAnchorX, y = cardAnchorY)
                    .width(cardWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .background(baseContainer),
            )
            Box(
                modifier = modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .offset(x = infoAnchorX, y = cardAnchorY)
                    .width(infoWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .landingInfoSurface(sideInfoShape)
                    .padding(
                        start = AppSpacingTokens.Medium,
                        end = AppSpacingTokens.Small,
                        top = AppSpacingTokens.Small,
                        bottom = AppSpacingTokens.Small,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(coverHeight.coerceAtMost(cardHeight - AppSpacingTokens.Small * 2)),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    LandingInfoTexts(model = model, info = info)
                }
            }
        }
        VideoCardSourceLayout.STACKED -> {
            // Transparent under cover; only info plate carries themed container (list card does too).
            Box(
                modifier = modifier
                    .zIndex(-1f)
                    .align(Alignment.TopStart)
                    .offset(x = cardAnchorX, y = cardAnchorY)
                    .width(cardWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .background(Color.Transparent),
            )
            Column(
                modifier = modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .offset(x = infoAnchorX, y = infoAnchorY)
                    .width(infoWidth)
                    .height(infoHeight)
                    .landingLayer()
                    .landingInfoSurface(stackedInfoShape)
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

/**
 * Home-recommendation info copy used for both STACKED (below) and SIDE_BY_SIDE (right).
 * Typography follows [feedContentTypography] like list cards.
 */
@Composable
private fun LandingInfoTexts(
    model: VideoDetailReturnSourceCardChromeModel,
    @Suppress("UNUSED_PARAMETER") info: ViewInfo?,
) {
    val contentTypography = feedContentTypography()
    // Only paint rows the list card froze in [model.infoPresentation] — no invented 弹幕.
    AppText(
        text = model.title,
        modifier = Modifier.fillMaxWidth(),
        style = contentTypography.title,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
    if (model.ownerName.isNotBlank() || model.followed) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
        ) {
            if (model.ownerName.isNotBlank()) {
                AppText(
                    text = model.ownerName,
                    style = contentTypography.author,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            if (model.followed) {
                AppText(
                    text = "已关注",
                    style = contentTypography.coverBadge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
            }
        }
    }
    val secondaryLine = resolveVideoDetailReturnInfoSecondaryLine(model)
    if (secondaryLine.isNotBlank()) {
        AppText(
            text = secondaryLine,
            modifier = Modifier.fillMaxWidth(),
            style = contentTypography.statistic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
