@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.MediaContrastPalette

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalSharedTransitionEnabled
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.videoCardShellSharedBoundsOrEmpty
import com.android.purebilibili.feature.home.components.cards.videoCardShellReturnChromeAlpha
import com.android.purebilibili.core.util.CardPositionManager
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.HomeHeroCarouselCardTransform
import com.android.purebilibili.feature.home.HOME_HERO_CAROUSEL_SIDE_PEEK_DP
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselAspectRatio
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselCardTransform
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemKey
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemOrNull
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselPreviewAlpha
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselWidthDp
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeHeroCarousel(
    videos: List<VideoItem>,
    autoplayEnabled: Boolean,
    onVideoClick: (VideoItem) -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?,
    modifier: Modifier = Modifier
) {
    if (videos.isEmpty()) return

    val pagerState = rememberPagerState { videos.size }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacingTokens.ExtraSmall)
    ) {
        val sidePeek = HOME_HERO_CAROUSEL_SIDE_PEEK_DP.dp
        val carouselWidth = resolveHomeHeroCarouselWidthDp(maxWidth.value).dp
        val pageWidth = (carouselWidth - sidePeek * 2).coerceAtLeast(AppSpacingTokens.None)
        val aspectRatio = resolveHomeHeroCarouselAspectRatio(carouselWidth.value)
        HorizontalPager(
            state = pagerState,
            key = { page ->
                resolveHomeHeroCarouselItemKey(videos, page, VideoItem::bvid)
            },
            pageSize = PageSize.Fixed(pageWidth),
            pageSpacing = AppSpacingTokens.None,
            contentPadding = PaddingValues(horizontal = sidePeek),
            modifier = Modifier
                .width(carouselWidth)
                .align(Alignment.Center)
        ) { page ->
            val video = resolveHomeHeroCarouselItemOrNull(videos, page)
                ?: return@HorizontalPager
            // 这里确实在组合期读了一个每帧变化的值，但**暂时无法就地修掉**：
            // pageOffset 派生出的 transform 同时喂给三个不同阶段的消费者——
            // Surface 的 shadowElevation（组合期）、Modifier.zIndex（布局期）、
            // 以及多个 graphicsLayer 与渐变 Brush（绘制期）。
            // 只把绘制期那部分下沉不解决问题，前两者仍然会拉着整张卡重组；
            // 真正的修法是把 HomeHeroCarouselCard 的 transform 参数改成 () -> T
            // 并重新安排三类消费者，属于卡片体系收编（计划 4.2）的范围，
            // 不适合塞进一次 lint 清理提交里——那样会在首页最显眼的组件上
            // 混入无法单独回滚的视觉风险。
            @Suppress("FrequentlyChangingValue")
            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                ).coerceIn(-1f, 1f)
            val transform = resolveHomeHeroCarouselCardTransform(pageOffset)
            val activeForPlayback = autoplayEnabled &&
                pagerState.currentPage == page &&
                pageOffset.absoluteValue < 0.12f
            HomeHeroCarouselCard(
                video = video,
                transform = transform,
                activeForPlayback = activeForPlayback,
                aspectRatio = aspectRatio,
                onVideoClick = { onVideoClick(video) },
                onGetPreviewUrl = onGetPreviewUrl
            )
        }

        Row(
            modifier = Modifier
                .width(carouselWidth)
                .align(Alignment.BottomCenter)
                .padding(start = AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall, bottom = AppSpacingTokens.Large + AppSpacingTokens.Micro),
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            videos.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) AppSpacingTokens.Medium - AppSpacingTokens.Micro / 2 else AppSpacingTokens.Small)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) {
                                MediaContrastPalette.Foreground
                            } else {
                                MediaContrastPalette.Foreground.copy(alpha = 0.46f)
                            }
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun HomeHeroCarouselCard(
    video: VideoItem,
    transform: HomeHeroCarouselCardTransform,
    activeForPlayback: Boolean,
    aspectRatio: Float,
    onVideoClick: () -> Unit,
    onGetPreviewUrl: suspend (String, Long) -> String?
) {
    var previewUrl by remember(video.bvid, video.cid) { mutableStateOf<String?>(null) }
    LaunchedEffect(activeForPlayback, video.bvid, video.cid) {
        if (activeForPlayback && previewUrl == null && video.bvid.isNotBlank() && video.cid > 0L) {
            previewUrl = onGetPreviewUrl(video.bvid, video.cid)
        }
    }

    // 整卡 sharedBounds：横幅卡片与详情 shell 同源。
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sourceRoute = LocalVideoCardSharedElementSourceRoute.current
    val sharedTransitionSpeedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val useCardShellSharedBounds = LocalSharedTransitionEnabled.current &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null &&
        video.bvid.isNotBlank() &&
        !sourceRoute.isNullOrBlank()
    val cardShellMotionSpec = remember(
        sourceRoute,
        useCardShellSharedBounds,
        sharedTransitionSpeedSettings
    ) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRoute,
            transitionEnabled = useCardShellSharedBounds,
            speedSettings = sharedTransitionSpeedSettings
        )
    }

    // 点击前写入 CardPositionManager，供返回 morph 对齐源卡 bounds。
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx: Float
    val screenHeightPx: Float
    val densityValue: Float
    remember(configuration.screenWidthDp, configuration.screenHeightDp, density) {
        Triple(
            with(density) { configuration.screenWidthDp.dp.toPx() },
            with(density) { configuration.screenHeightDp.dp.toPx() },
            density.density
        )
    }.let { (w, h, d) ->
        screenWidthPx = w
        screenHeightPx = h
        densityValue = d
    }

    // 卡片坐标句柄：onGloballyPositioned 写入，点击时读取。
    val cardCoordsRef = remember { object { var value: LayoutCoordinates? = null } }

    val cardShape = AppShapes.container(ContainerLevel.Card)
    val cardCornerDp = AppShapes.containerCornerDp(ContainerLevel.Card)

    // 记录源卡位置后进入详情。
    val clickAction: () -> Unit = {
        cardCoordsRef.value?.takeIf { it.isAttached }?.boundsInRoot()?.let { bounds ->
            CardPositionManager.recordVideoCardPosition(
                bvid = video.bvid,
                sourceRoute = sourceRoute,
                bounds = bounds,
                screenWidth = screenWidthPx,
                screenHeight = screenHeightPx,
                density = densityValue,
                sourceCornerDp = cardCornerDp.value.roundToInt(),
            )
        }
        onVideoClick()
    }

    AppSurface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = AppSpacingTokens.None,
        shadowElevation = (transform.shadowElevationFraction * 10f).dp,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .videoCardShellSharedBoundsOrEmpty(
                enabled = useCardShellSharedBounds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bvid = video.bvid,
                sourceRoute = sourceRoute,
                motionSpec = cardShellMotionSpec,
                clipShape = cardShape
            )
            .zIndex(transform.zIndex)
            .graphicsLayer {
                transformOrigin = TransformOrigin(transform.pivotFractionX, 0.5f)
                translationX = transform.translationXFraction * size.width
                scaleX = transform.scale
                scaleY = transform.scale
                alpha = transform.alpha
            }
            .clip(cardShape)
            .onGloballyPositioned { coordinates ->
                cardCoordsRef.value = coordinates
            }
            .clickable(onClick = clickAction)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val normalizedCoverUrl = remember(video.pic) { FormatUtils.fixImageUrl(video.pic) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = transform.contentParallaxFraction * size.width
                        scaleX = transform.contentScale
                        scaleY = transform.contentScale
                    }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(normalizedCoverUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = video.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (activeForPlayback && previewUrl != null) {
                    MutedHeroVideoPlayer(url = previewUrl.orEmpty())
                }
            }
            if (transform.edgeShadeAlpha > 0.001f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (transform.edgeShadeStartFromLeft) {
                                Brush.horizontalGradient(
                                    0f to MediaContrastPalette.Scrim.copy(alpha = transform.edgeShadeAlpha),
                                    0.48f to Color.Transparent,
                                    1f to Color.Transparent
                                )
                            } else {
                                Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    0.52f to Color.Transparent,
                                    1f to MediaContrastPalette.Scrim.copy(alpha = transform.edgeShadeAlpha)
                                )
                            }
                        )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.54f to Color.Transparent,
                            1f to MediaContrastPalette.Scrim.copy(alpha = 0.76f)
                        )
                    )
            )
            // 底部标题与统计（时长 · 播放 · 弹幕）
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall, end = AppSpacingTokens.ExtraLarge + AppSpacingTokens.ExtraSmall, bottom = AppSpacingTokens.Medium + AppSpacingTokens.Micro)
                    .videoCardShellReturnChromeAlpha(
                        enabled = useCardShellSharedBounds,
                        bvid = video.bvid,
                        sourceRoute = sourceRoute,
                    )
            ) {
                // 标题行（预览播放中显示播放图标）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (activeForPlayback) {
                        AppIcon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MediaContrastPalette.Foreground.copy(alpha = 0.9f),
                            modifier = Modifier.size(AppSpacingTokens.ExtraLarge - AppSpacingTokens.Micro)
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro))
                    }
                    AppText(
                        text = video.title,
                        color = MediaContrastPalette.Foreground,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // 统计信息：时长 · 播放量 · 弹幕
                if (video.duration > 0 || video.stat.view > 0 || video.stat.danmaku > 0) {
                    Spacer(modifier = Modifier.height(AppSpacingTokens.ExtraSmall))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                    var separatorNeeded = false
                    // 时长
                    if (video.duration > 0) {
                        AppText(
                            text = FormatUtils.formatDuration(video.duration),
                            color = MediaContrastPalette.Foreground.copy(alpha = 0.65f),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            modifier = Modifier.wrapContentSize()
                        )
                        separatorNeeded = true
                    }
                    // 播放量
                    if (video.stat.view > 0) {
                        if (separatorNeeded) AppText(
                            " · ",
                            color = MediaContrastPalette.Foreground.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                        AppText(
                            text = FormatUtils.formatStat(video.stat.view.toLong()) + "播放",
                            color = MediaContrastPalette.Foreground.copy(alpha = 0.65f),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            modifier = Modifier.wrapContentSize()
                        )
                        separatorNeeded = true
                    }
                    // 弹幕
                    if (video.stat.danmaku > 0) {
                        if (separatorNeeded) AppText(
                            " · ",
                            color = MediaContrastPalette.Foreground.copy(alpha = 0.5f),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                        AppText(
                            text = FormatUtils.formatStat(video.stat.danmaku.toLong()) + "弹幕",
                            color = MediaContrastPalette.Foreground.copy(alpha = 0.65f),
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            modifier = Modifier.wrapContentSize()
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun MutedHeroVideoPlayer(url: String) {
    val context = LocalContext.current
    var hasRenderedFirstFrame by remember(url) { mutableStateOf(false) }
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 0f
        }
    }
    LaunchedEffect(url) {
        hasRenderedFirstFrame = false
        player.setMediaItem(MediaItem.fromUri(Uri.parse(url)))
        player.prepare()
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                hasRenderedFirstFrame = true
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = resolveHomeHeroCarouselPreviewAlpha(hasRenderedFirstFrame)
            }
    )
}
