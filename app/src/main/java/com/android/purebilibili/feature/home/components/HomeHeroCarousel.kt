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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.HOME_HERO_CAROUSEL_SIDE_PEEK_DP
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselAspectRatio
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemKey
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselItemOrNull
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselPreviewAlpha
import com.android.purebilibili.feature.home.resolveHomeHeroCarouselWidthDp
import kotlin.math.absoluteValue

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
            val activeForPlayback = autoplayEnabled &&
                pagerState.currentPage == page &&
                pagerState.currentPageOffsetFraction.absoluteValue < 0.12f
            HomeHeroCarouselCard(
                video = video,
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

@Composable
private fun HomeHeroCarouselCard(
    video: VideoItem,
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

    val cardShape = AppShapes.container(ContainerLevel.Card)

    AppSurface(
        shape = cardShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = AppSpacingTokens.None,
        shadowElevation = AppSpacingTokens.None,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(cardShape)
            .clickable(onClick = onVideoClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val normalizedCoverUrl = remember(video.pic) { FormatUtils.fixImageUrl(video.pic) }

            Box(modifier = Modifier.fillMaxSize()) {
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
