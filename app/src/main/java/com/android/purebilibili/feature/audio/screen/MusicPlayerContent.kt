package com.android.purebilibili.feature.audio.screen

import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppCircularProgressIndicator
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import androidx.compose.material3.ExperimentalMaterial3Api
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppLinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.components.AppOutlinedTextField
import com.android.purebilibili.core.ui.components.AppSlider
import androidx.compose.material3.SliderDefaults
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.android.purebilibili.core.lifecycle.BackgroundManager
import com.android.purebilibili.feature.audio.lyrics.LyricLine
import com.android.purebilibili.feature.audio.lyrics.resolveActiveLyricIndex
import com.android.purebilibili.feature.audio.lyrics.resolveLyricFocusScrollOffsetPx
import com.android.purebilibili.feature.audio.player.MusicPlayerUiState
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.home.components.BottomBarMatchedReusableLiquidDock
import com.android.purebilibili.feature.video.playback.audio.AudioQualityOption
import com.android.purebilibili.feature.video.player.PlayMode
import com.android.purebilibili.feature.video.ui.components.AudioQualitySelectionMenu
import com.android.purebilibili.feature.video.ui.components.DolbyBadge
import com.android.purebilibili.feature.video.ui.components.HiResBadge
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.filled.BackwardEnd
import io.github.alexzhirkevich.cupertino.icons.filled.ForwardEnd
import io.github.alexzhirkevich.cupertino.icons.filled.Pause
import io.github.alexzhirkevich.cupertino.icons.filled.Play
import io.github.alexzhirkevich.cupertino.icons.outlined.ChevronDown
import io.github.alexzhirkevich.cupertino.icons.outlined.Ellipsis
import io.github.alexzhirkevich.cupertino.icons.outlined.MusicNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop as miuixLayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop as rememberMiuixLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

private val MusicFallbackColor = Color(0xFF342B42)
private val MusicContentColor = Color.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MusicPlayerContent(
    state: MusicPlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onQueueItemSelected: (Int) -> Unit = {},
    onPlayModeChange: (PlayMode) -> Unit = {},
    onLyricsOffsetChange: (Long) -> Unit = {},
    onLyricsRetry: () -> Unit = {},
    onLyricsSearch: (String) -> Unit = {},
    onLyricsCandidateSelected: (Int) -> Unit = {},
    onVideoModeClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSleepTimerClick: (() -> Unit)? = null,
    sleepTimerLabel: String = "定时关闭",
    audioQualityLabel: String = "音质",
    audioQualityOptions: List<AudioQualityOption> = emptyList(),
    requestedAudioQuality: Int = -1,
    isHiResAudioSelected: Boolean = false,
    isDolbyAudioSelected: Boolean = false,
    onAudioQualitySelected: ((Int) -> Unit)? = null,
    onPipClick: (() -> Unit)? = null,
    onToggleOrientation: (() -> Unit)? = null,
    orientationActionLabel: String = "横屏",
    isInPipMode: Boolean = false,
    liquidGlassEffectsEnabled: Boolean = false,
    lyricsBlurEffectsEnabled: Boolean = true,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var paletteColor by remember { mutableStateOf(MusicFallbackColor) }
    var artworkBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showAudioQuality by remember { mutableStateOf(false) }
    var showLyricsSearch by remember { mutableStateOf(false) }
    var progressSeekRevision by remember { mutableIntStateOf(0) }
    var lyricsControlsVisible by remember(state.title) { mutableStateOf(true) }
    var lyricSearchText by remember(state.title) { mutableStateOf(state.title) }
    val systemReduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val effectiveReduceMotion = reduceMotion || systemReduceMotion
    val musicBackdrop = rememberMiuixLayerBackdrop()
    val selectionBackdrop = rememberLayerBackdrop()

    LaunchedEffect(state.coverUrl) {
        val result = loadMusicArtwork(context.imageLoader, state.coverUrl, context)
        artworkBitmap = result?.first
        paletteColor = result?.second ?: MusicFallbackColor
    }

    val backgroundColor by animateColorAsState(
        targetValue = paletteColor,
        animationSpec = if (effectiveReduceMotion) snap() else AppMotionTokens.emphasizedSpec(),
        label = "music_palette"
    )
    val glassEnabled = resolveMusicLiquidGlassEnabled(
        sdkInt = Build.VERSION.SDK_INT,
        effectsEnabled = liquidGlassEffectsEnabled,
        isAppInBackground = BackgroundManager.isInBackground,
        reduceMotion = effectiveReduceMotion
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        val layout = resolveMusicPlayerLayout(maxWidth.value.roundToInt(), isInPipMode)
        val availableWidthDp = maxWidth.value.roundToInt()
        val availableHeightDp = maxHeight.value.roundToInt()
        if (layout != MusicPlayerLayout.PIP_ARTWORK) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .miuixLayerBackdrop(musicBackdrop)
            ) {
                MusicArtworkBackground(
                    coverUrl = state.coverUrl,
                    backgroundColor = backgroundColor
                )
            }
        }
        when (layout) {
            MusicPlayerLayout.PIP_ARTWORK -> MusicArtwork(
                coverUrl = state.coverUrl,
                bitmap = artworkBitmap,
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape
            )

            MusicPlayerLayout.COMPACT_PAGER -> {
                val pagerState = rememberPagerState(pageCount = { 2 })
                val pagerScope = rememberCoroutineScope()
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(selectionBackdrop)
                    ) { page ->
                        if (page == 0) {
                            PlayerPage(
                                state = state,
                                artworkBitmap = artworkBitmap,
                                artworkSizeDp = resolveMusicArtworkSizeDp(
                                    availableWidthDp,
                                    availableHeightDp,
                                    layout
                                ),
                                glassEnabled = glassEnabled,
                                onPlayPause = onPlayPause,
                                onSeek = { positionMs ->
                                    progressSeekRevision += 1
                                    onSeek(positionMs)
                                },
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onPlayModeChange = onPlayModeChange,
                                miuixBackdrop = musicBackdrop,
                                audioQualityLabel = audioQualityLabel,
                                isHiResAudioSelected = isHiResAudioSelected,
                                isDolbyAudioSelected = isDolbyAudioSelected,
                                onAudioQualityClick = onAudioQualitySelected?.let {
                                    { showAudioQuality = true }
                                },
                                glassTintColor = backgroundColor,
                                modifier = Modifier.padding(bottom = 70.dp)
                            )
                        } else {
                            LyricsPage(
                                state = state,
                                glassEnabled = glassEnabled,
                                onPlayPause = onPlayPause,
                                onSeek = { positionMs ->
                                    progressSeekRevision += 1
                                    onSeek(positionMs)
                                },
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onLyricsOffsetChange = onLyricsOffsetChange,
                                onLyricsRetry = onLyricsRetry,
                                onOpenLyricsSearch = { showLyricsSearch = true },
                                blurEffectsEnabled = lyricsBlurEffectsEnabled,
                                reduceMotion = effectiveReduceMotion,
                                glassTintColor = backgroundColor,
                                miuixBackdrop = musicBackdrop,
                                progressSeekRevision = progressSeekRevision,
                                controlsVisible = lyricsControlsVisible,
                                onControlsVisibleChange = { lyricsControlsVisible = it },
                                showPageSwitcher = true
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = pagerState.currentPage != 1 || lyricsControlsVisible,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = if (effectiveReduceMotion) EnterTransition.None else fadeIn() + slideInVertically { it / 2 },
                        exit = if (effectiveReduceMotion) ExitTransition.None else fadeOut() + slideOutVertically { it / 2 }
                    ) {
                        BottomBarLiquidSegmentedControl(
                            items = listOf("播放", "歌词"),
                            selectedIndex = pagerState.currentPage,
                            onSelected = { page ->
                                pagerScope.launch { pagerState.animateScrollToPage(page) }
                            },
                            modifier = Modifier
                                .navigationBarsPadding()
                                .padding(horizontal = 72.dp, vertical = 8.dp),
                            height = 52.dp,
                            indicatorHeight = 46.dp,
                            liquidGlassEffectsEnabled = glassEnabled,
                            preferInlineContentStyle = false,
                            backdrop = selectionBackdrop,
                            miuixBackdrop = musicBackdrop,
                            isScrollInProgressProvider = { pagerState.isScrollInProgress },
                            indicatorPositionProvider = {
                                resolveMusicPagerIndicatorPosition(
                                    currentPage = pagerState.currentPage,
                                    currentPageOffsetFraction = pagerState.currentPageOffsetFraction
                                )
                            }
                        )
                    }
                }
            }

            MusicPlayerLayout.EXPANDED_SPLIT -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 56.dp, start = 32.dp, end = 32.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                PlayerPage(
                    state = state,
                    artworkBitmap = artworkBitmap,
                    artworkSizeDp = resolveMusicArtworkSizeDp(
                        availableWidthDp,
                        availableHeightDp,
                        layout
                    ),
                    glassEnabled = glassEnabled,
                    onPlayPause = onPlayPause,
                    onSeek = { positionMs ->
                        progressSeekRevision += 1
                        onSeek(positionMs)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onPlayModeChange = onPlayModeChange,
                    miuixBackdrop = musicBackdrop,
                    audioQualityLabel = audioQualityLabel,
                    isHiResAudioSelected = isHiResAudioSelected,
                    isDolbyAudioSelected = isDolbyAudioSelected,
                    onAudioQualityClick = onAudioQualitySelected?.let {
                        { showAudioQuality = true }
                    },
                    glassTintColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )
                LyricsPage(
                    state = state,
                    glassEnabled = glassEnabled,
                    onPlayPause = onPlayPause,
                    onSeek = onSeek,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onLyricsOffsetChange = onLyricsOffsetChange,
                    onLyricsRetry = onLyricsRetry,
                    onOpenLyricsSearch = { showLyricsSearch = true },
                    blurEffectsEnabled = lyricsBlurEffectsEnabled,
                    reduceMotion = effectiveReduceMotion,
                    glassTintColor = backgroundColor,
                    miuixBackdrop = musicBackdrop,
                    progressSeekRevision = progressSeekRevision,
                    controlsVisible = lyricsControlsVisible,
                    onControlsVisibleChange = { lyricsControlsVisible = it },
                    showPageSwitcher = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!isInPipMode) {
            MusicTopBar(
                glassEnabled = glassEnabled,
                miuixBackdrop = musicBackdrop,
                onBack = onBack,
                onMore = { showActions = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }

    if (showActions) {
        AppModalBottomSheet(
            onDismissRequest = { showActions = false },
            containerColor = backgroundColor.copy(alpha = 0.92f),
            contentColor = MusicContentColor
        ) {
            AppText(
                text = "播放器操作",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            if (state.queueControls.showQueue) {
                MusicActionSheetItem("播放队列") {
                    showActions = false
                    showQueue = true
                }
            }
            onVideoModeClick?.let { action ->
                MusicActionSheetItem("返回视频") {
                    showActions = false
                    action()
                }
            }
            onCollectionClick?.let { action ->
                MusicActionSheetItem("选集 / 合集") {
                    showActions = false
                    action()
                }
            }
            onSleepTimerClick?.let { action ->
                MusicActionSheetItem(sleepTimerLabel) {
                    showActions = false
                    action()
                }
            }
            onPipClick?.let { action ->
                MusicActionSheetItem("画中画") {
                    showActions = false
                    action()
                }
            }
            onToggleOrientation?.let { action ->
                MusicActionSheetItem(orientationActionLabel) {
                    showActions = false
                    action()
                }
            }
            MusicActionSheetItem("搜索歌词") {
                showActions = false
                showLyricsSearch = true
            }
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }

    if (showAudioQuality && onAudioQualitySelected != null) {
        AudioQualitySelectionMenu(
            options = audioQualityOptions,
            requestedAudioQuality = requestedAudioQuality,
            onAudioQualitySelected = { quality ->
                onAudioQualitySelected(quality)
                showAudioQuality = false
            },
            onDismiss = { showAudioQuality = false }
        )
    }

    if (showQueue) {
        AppModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = backgroundColor.copy(alpha = 0.96f),
            contentColor = MusicContentColor
        ) {
            AppText(
                text = "待播清单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                itemsIndexed(state.queue, key = { _, item -> item.stableId }) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onQueueItemSelected(index)
                                showQueue = false
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(AppShapes.container(ContainerLevel.Field)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            AppText(
                                text = item.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (index == state.currentQueueIndex) FontWeight.Bold else FontWeight.Normal
                            )
                            AppText(
                                text = item.artist,
                                color = MusicContentColor.copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (index == state.currentQueueIndex) {
                            AppIcon(CupertinoIcons.Outlined.MusicNote, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    if (showLyricsSearch) {
        AppModalBottomSheet(
            onDismissRequest = { showLyricsSearch = false },
            containerColor = backgroundColor.copy(alpha = 0.97f),
            contentColor = MusicContentColor
        ) {
            AppText(
                text = "手动匹配歌词",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppOutlinedTextField(
                    value = lyricSearchText,
                    onValueChange = { lyricSearchText = it },
                    label = { AppText("歌名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                AppTextButton(onClick = { onLyricsSearch(lyricSearchText) }) {
                    AppText("搜索")
                }
            }
            if (state.isLyricsSearching) {
                AppCircularProgressIndicator(
                    color = MusicContentColor,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp)
                )
            } else if (state.lyricCandidates.isEmpty()) {
                AppText(
                    text = "输入歌名后搜索网易云、QQ 音乐与酷狗",
                    color = MusicContentColor.copy(alpha = 0.62f),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    itemsIndexed(state.lyricCandidates) { index, candidate ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLyricsCandidateSelected(index)
                                    showLyricsSearch = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            AppText(candidate.title, fontWeight = FontWeight.SemiBold)
                            AppText(
                                text = "${candidate.artist} · ${candidate.sourceLabel}",
                                color = MusicContentColor.copy(alpha = 0.62f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MusicArtworkBackground(coverUrl: String, backgroundColor: Color) {
    Box(Modifier.fillMaxSize()) {
        if (coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(64.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.52f
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            backgroundColor.copy(alpha = 0.42f),
                            Color.Black.copy(alpha = 0.66f),
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun PlayerPage(
    state: MusicPlayerUiState,
    artworkBitmap: ImageBitmap?,
    artworkSizeDp: Int,
    glassEnabled: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onPlayModeChange: (PlayMode) -> Unit,
    miuixBackdrop: MiuixBackdrop?,
    audioQualityLabel: String,
    isHiResAudioSelected: Boolean,
    isDolbyAudioSelected: Boolean,
    onAudioQualityClick: (() -> Unit)?,
    glassTintColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, top = 76.dp, end = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading && state.coverUrl.isBlank()) {
            AppCircularProgressIndicator(color = MusicContentColor)
        } else {
            MusicArtwork(
                coverUrl = state.coverUrl,
                bitmap = artworkBitmap,
                modifier = Modifier.size(artworkSizeDp.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth()) {
            AppText(
                text = state.title,
                color = MusicContentColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            AppText(
                text = state.artist.ifBlank { "未知艺术家" },
                color = MusicContentColor.copy(alpha = 0.64f),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            state.error?.let {
                AppText(it, color = Color(0xFFFF9B92), style = MaterialTheme.typography.bodySmall)
            }
        }
        onAudioQualityClick?.let { action ->
            Spacer(Modifier.height(14.dp))
            MusicAudioQualityControl(
                label = audioQualityLabel,
                isHiResSelected = isHiResAudioSelected,
                isDolbySelected = isDolbyAudioSelected,
                onClick = action
            )
        }
        Spacer(Modifier.height(14.dp))
        MusicProgress(state, onSeek)
        Spacer(Modifier.height(8.dp))
        PlaybackControls(state, onPlayPause, onPrevious, onNext)
        Spacer(Modifier.height(12.dp))
        MusicPlayModeDock(
            mode = state.playMode,
            glassEnabled = glassEnabled,
            miuixBackdrop = miuixBackdrop,
            onPlayModeChange = onPlayModeChange
        )
    }
}

@Composable
private fun MusicAudioQualityControl(
    label: String,
    isHiResSelected: Boolean,
    isDolbySelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MusicContentColor.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "音质",
            color = MusicContentColor.copy(alpha = 0.72f),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = label.ifBlank { "音质" },
            color = MusicContentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (isHiResSelected) {
            HiResBadge()
        }
        if (isDolbySelected) {
            DolbyBadge()
        }
    }
}

@Composable
private fun MusicPlayModeDock(
    mode: PlayMode,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onPlayModeChange: (PlayMode) -> Unit
) {
    BottomBarLiquidSegmentedControl(
        items = listOf("顺序播放", "随机播放", "单曲循环", "列表循环"),
        selectedIndex = resolveMusicPlayModeIndex(mode),
        onSelected = { onPlayModeChange(resolveMusicPlayMode(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        height = 52.dp,
        indicatorHeight = 46.dp,
        labelFontSize = 13.sp,
        liquidGlassEffectsEnabled = glassEnabled,
        preferInlineContentStyle = false,
        miuixBackdrop = miuixBackdrop
    )
}

@Composable
private fun MusicArtwork(
    coverUrl: String,
    bitmap: ImageBitmap?,
    modifier: Modifier,
    shape: Shape = AppShapes.container(ContainerLevel.Card)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF615571), Color(0xFF27212F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            coverUrl.isNotBlank() -> AsyncImage(
                model = coverUrl,
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            else -> AppIcon(
                CupertinoIcons.Outlined.MusicNote,
                contentDescription = null,
                tint = MusicContentColor.copy(alpha = 0.78f),
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun MusicProgress(state: MusicPlayerUiState, onSeek: (Long) -> Unit) {
    val duration = state.durationMs.coerceAtLeast(1L)
    var draggedPosition by remember { mutableStateOf<Float?>(null) }
    AppSlider(
        value = draggedPosition ?: state.positionMs.coerceIn(0L, duration).toFloat(),
        onValueChange = { draggedPosition = it },
        onValueChangeFinished = {
            draggedPosition?.let { onSeek(it.toLong()) }
            draggedPosition = null
        },
        valueRange = 0f..duration.toFloat(),
        colors = SliderDefaults.colors(
            thumbColor = MusicContentColor,
            activeTrackColor = MusicContentColor,
            inactiveTrackColor = MusicContentColor.copy(alpha = 0.24f)
        )
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        AppText(formatMusicTime(state.positionMs), color = MusicContentColor.copy(alpha = 0.58f), fontSize = 11.sp)
        AppText("-${formatMusicTime((state.durationMs - state.positionMs).coerceAtLeast(0L))}", color = MusicContentColor.copy(alpha = 0.58f), fontSize = 11.sp)
    }
}

@Composable
private fun PlaybackControls(
    state: MusicPlayerUiState,
    onPlayPause: () -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaybackIconButton(
            icon = CupertinoIcons.Filled.BackwardEnd,
            description = "上一首",
            enabled = state.queueControls.hasPrevious && onPrevious != null,
            onClick = onPrevious ?: {}
        )
        AppIconButton(onClick = onPlayPause, modifier = Modifier.size(72.dp)) {
            if (state.isBuffering) {
                AppCircularProgressIndicator(color = MusicContentColor, modifier = Modifier.size(36.dp))
            } else {
                AppIcon(
                    imageVector = if (state.isPlaying) CupertinoIcons.Filled.Pause else CupertinoIcons.Filled.Play,
                    contentDescription = if (state.isPlaying) "暂停" else "播放",
                    tint = MusicContentColor,
                    modifier = Modifier.size(46.dp)
                )
            }
        }
        PlaybackIconButton(
            icon = CupertinoIcons.Filled.ForwardEnd,
            description = "下一首",
            enabled = state.queueControls.hasNext && onNext != null,
            onClick = onNext ?: {}
        )
    }
}

@Composable
private fun PlaybackIconButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    AppIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(56.dp)) {
        AppIcon(
            imageVector = icon,
            contentDescription = description,
            tint = MusicContentColor.copy(alpha = if (enabled) 1f else 0.28f),
            modifier = Modifier.size(32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsPage(
    state: MusicPlayerUiState,
    glassEnabled: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onLyricsOffsetChange: (Long) -> Unit,
    onLyricsRetry: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    blurEffectsEnabled: Boolean,
    reduceMotion: Boolean,
    glassTintColor: Color,
    miuixBackdrop: MiuixBackdrop?,
    progressSeekRevision: Int,
    controlsVisible: Boolean,
    onControlsVisibleChange: (Boolean) -> Unit,
    showPageSwitcher: Boolean,
    modifier: Modifier = Modifier
) {
    val document = state.lyrics
    val currentIndex = document?.let { resolveActiveLyricIndex(it, state.positionMs) } ?: -1
    val blurEnabled = resolveMusicLyricsBlurEnabled(
        sdkInt = Build.VERSION.SDK_INT,
        effectsEnabled = blurEffectsEnabled,
        reduceMotion = reduceMotion
    )
    val listState = rememberLazyListState()
    val isLyricsDragged by listState.interactionSource.collectIsDraggedAsState()
    var showTranslations by remember { mutableStateOf(true) }
    var showLyricsSettings by remember { mutableStateOf(false) }
    var isAutoFollowPaused by remember(document) { mutableStateOf(false) }
    LaunchedEffect(progressSeekRevision) {
        if (progressSeekRevision > 0) {
            isAutoFollowPaused = false
        }
    }
    LaunchedEffect(isLyricsDragged) {
        if (isLyricsDragged) {
            isAutoFollowPaused = true
        }
    }
    LaunchedEffect(currentIndex, isAutoFollowPaused, reduceMotion) {
        if (currentIndex >= 0 && !isAutoFollowPaused) {
            val focusOffset = resolveLyricFocusScrollOffsetPx(
                listState.layoutInfo.viewportSize.height
            )
            if (reduceMotion) {
                listState.scrollToItem(currentIndex, focusOffset)
            } else {
                listState.animateScrollToItem(currentIndex, focusOffset)
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onControlsVisibleChange(!controlsVisible) }
            .padding(top = 72.dp, bottom = 16.dp)
    ) {
        if (document == null || document.lines.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppText(
                    text = when {
                        state.isLyricsSearching -> "正在匹配歌词…"
                        state.lyricsError != null -> "歌词加载失败"
                        else -> "未找到匹配歌词"
                    },
                    color = MusicContentColor.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassTextButton(
                        "重新匹配",
                        glassEnabled,
                        miuixBackdrop,
                        onLyricsRetry
                    )
                    GlassTextButton(
                        "手动搜索",
                        glassEnabled,
                        miuixBackdrop,
                        onOpenLyricsSearch
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 28.dp,
                    top = 120.dp,
                    end = 28.dp,
                    bottom = 260.dp
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                itemsIndexed(document.lines, key = { index, line -> "${line.startTimeMs}:$index" }) { index, line ->
                    LyricLineContent(
                        line = line,
                        isCurrent = index == currentIndex,
                        positionMs = state.positionMs - document.offsetMs,
                        showTranslations = showTranslations,
                        focusStyle = resolveMusicLyricFocusStyle(index, currentIndex, blurEnabled),
                        reduceMotion = reduceMotion,
                        onClick = {
                            isAutoFollowPaused = false
                            onSeek(line.startTimeMs + document.offsetMs)
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = if (showPageSwitcher) 68.dp else 0.dp
                ),
            enter = if (reduceMotion) EnterTransition.None else fadeIn() + slideInVertically { it / 2 },
            exit = if (reduceMotion) ExitTransition.None else fadeOut() + slideOutVertically { it / 2 }
        ) {
            LyricsPrimaryControls(
                state = state,
                glassEnabled = glassEnabled,
                miuixBackdrop = miuixBackdrop,
                onPlayPause = onPlayPause,
                onSeek = onSeek,
                onPrevious = onPrevious,
                onNext = onNext,
                onOpenSettings = { showLyricsSettings = true },
                onHideControls = { onControlsVisibleChange(false) }
            )
        }
        if (!controlsVisible) {
            LyricsImmersiveProgress(
                state = state,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        if (isAutoFollowPaused && controlsVisible) {
            GlassTextButton(
                label = "回到当前歌词",
                glassEnabled = glassEnabled,
                miuixBackdrop = miuixBackdrop,
                onClick = { isAutoFollowPaused = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }

    if (showLyricsSettings) {
        AppModalBottomSheet(
            onDismissRequest = { showLyricsSettings = false },
            containerColor = glassTintColor.copy(alpha = 0.97f),
            contentColor = MusicContentColor
        ) {
            LyricsSettingsContent(
                showTranslations = showTranslations,
                lyricsOffsetMs = document?.offsetMs ?: 0L,
                onToggleTranslations = { showTranslations = !showTranslations },
                onLyricsOffsetChange = onLyricsOffsetChange,
                onLyricsRetry = onLyricsRetry,
                onOpenLyricsSearch = {
                    showLyricsSettings = false
                    onOpenLyricsSearch()
                }
            )
        }
    }
}

@Composable
private fun LyricsPrimaryControls(
    state: MusicPlayerUiState,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    onHideControls: () -> Unit
) {
    val shape = AppShapes.container(ContainerLevel.Floating)
    BottomBarMatchedReusableLiquidDock(
        shape = shape,
        modifier = Modifier.fillMaxWidth(),
        backdrop = miuixBackdrop,
        liquidGlassEffectsEnabled = glassEnabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (it) {
                        Modifier
                    } else {
                        Modifier.background(AppSurfaceTokens.cardContainer(), shape)
                    }
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MusicProgress(state, onSeek)
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlaybackControls(state, onPlayPause, onPrevious, onNext, modifier = Modifier.weight(1f))
                AppTextButton(onClick = onOpenSettings, modifier = Modifier.height(48.dp)) {
                    AppText("歌词设置", color = MusicContentColor, fontSize = 12.sp)
                }
                AppTextButton(onClick = onHideControls, modifier = Modifier.height(48.dp)) {
                    AppText("收起", color = MusicContentColor, fontSize = 12.sp)
                }
            }
        }
    }
}

private fun formatLyricsOffset(offsetMs: Long): String {
    if (offsetMs == 0L) return "校正 0.00s"
    val absoluteMs = kotlin.math.abs(offsetMs)
    val seconds = absoluteMs / 1_000L
    val hundredths = (absoluteMs % 1_000L) / 10L
    val sign = if (offsetMs > 0L) "+" else "-"
    return "校正 $sign$seconds.${hundredths.toString().padStart(2, '0')}s"
}

@Composable
private fun LyricsImmersiveProgress(
    state: MusicPlayerUiState,
    modifier: Modifier = Modifier
) {
    val duration = state.durationMs.coerceAtLeast(1L)
    AppLinearProgressIndicator(
        progress = { state.positionMs.coerceIn(0L, duration).toFloat() / duration.toFloat() },
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
        color = MusicContentColor,
        trackColor = MusicContentColor.copy(alpha = 0.22f)
    )
}

@Composable
private fun LyricsSettingsContent(
    showTranslations: Boolean,
    lyricsOffsetMs: Long,
    onToggleTranslations: () -> Unit,
    onLyricsOffsetChange: (Long) -> Unit,
    onLyricsRetry: () -> Unit,
    onOpenLyricsSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppText("歌词设置", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        MusicActionSheetItem(if (showTranslations) "隐藏翻译与罗马音" else "显示翻译与罗马音", onToggleTranslations)
        AppText("歌词时间校正 · ${formatLyricsOffset(lyricsOffsetMs)}", color = MusicContentColor.copy(alpha = 0.72f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextButton(onClick = { onLyricsOffsetChange(-250L) }, modifier = Modifier.height(48.dp)) { AppText("歌词提前 0.25 秒") }
            AppTextButton(onClick = { onLyricsOffsetChange(250L) }, modifier = Modifier.height(48.dp)) { AppText("歌词延后 0.25 秒") }
        }
        AppTextButton(onClick = { onLyricsOffsetChange(-lyricsOffsetMs) }, modifier = Modifier.height(48.dp)) { AppText("重置歌词时间") }
        MusicActionSheetItem("重新匹配歌词", onLyricsRetry)
        MusicActionSheetItem("手动搜索歌词", onOpenLyricsSearch)
    }
}

@Composable
private fun LyricLineContent(
    line: LyricLine,
    isCurrent: Boolean,
    positionMs: Long,
    showTranslations: Boolean,
    focusStyle: MusicLyricFocusStyle,
    reduceMotion: Boolean,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = focusStyle, label = "lyric_focus")
    val blurRadius = transition.animateDp(
        transitionSpec = { if (reduceMotion) snap() else AppMotionTokens.standardSpec() },
        label = "lyric_blur"
    ) { it.blurRadiusDp.dp }
    val alpha = transition.animateFloat(
        transitionSpec = { if (reduceMotion) snap() else AppMotionTokens.standardSpec() },
        label = "lyric_alpha"
    ) { it.alphaPercent / 100f }
    val focusModifier = if (Build.VERSION.SDK_INT >= 31 && blurRadius.value > 0.dp) {
        Modifier.blur(blurRadius.value, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(focusModifier)
            .graphicsLayer { this.alpha = alpha.value }
            .clickable(onClick = onClick)
    ) {
        AppText(
            text = buildLyricText(line, isCurrent, positionMs),
            color = MusicContentColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )
        line.translations.firstOrNull()?.takeIf { showTranslations && it.isNotBlank() }?.let {
            AppText(
                text = it,
                color = MusicContentColor.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        line.romanization?.takeIf { showTranslations && it.isNotBlank() }?.let {
            AppText(
                text = it,
                color = MusicContentColor.copy(alpha = 0.58f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

private fun buildLyricText(line: LyricLine, isCurrent: Boolean, positionMs: Long): AnnotatedString {
    if (!isCurrent || line.spans.isEmpty()) return AnnotatedString(line.text)
    return buildAnnotatedString {
        line.spans.forEach { span ->
            val active = positionMs >= span.startTimeMs
            pushStyle(SpanStyle(color = MusicContentColor.copy(alpha = if (active) 1f else 0.38f)))
            append(span.text)
            pop()
        }
    }
}

@Composable
private fun MusicTopBar(
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        GlassIconButton(
            CupertinoIcons.Outlined.ChevronDown,
            "返回",
            glassEnabled,
            miuixBackdrop,
            onBack
        )
        GlassIconButton(
            CupertinoIcons.Outlined.Ellipsis,
            "更多操作",
            glassEnabled,
            miuixBackdrop,
            onMore
        )
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    description: String,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onClick: () -> Unit
) {
    BottomBarMatchedReusableLiquidDock(
        shape = CircleShape,
        modifier = Modifier.size(48.dp),
        backdrop = miuixBackdrop,
        liquidGlassEffectsEnabled = glassEnabled
    ) {
        AppIconButton(
            onClick = onClick,
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (it) {
                        Modifier
                    } else {
                        Modifier.background(AppSurfaceTokens.cardContainer(), CircleShape)
                    }
                )
        ) {
            AppIcon(icon, contentDescription = description, tint = MusicContentColor)
        }
    }
}

@Composable
private fun GlassTextButton(
    label: String,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = AppShapes.container(ContainerLevel.Pill)
    BottomBarMatchedReusableLiquidDock(
        shape = shape,
        modifier = modifier
            .height(48.dp),
        backdrop = miuixBackdrop,
        liquidGlassEffectsEnabled = glassEnabled
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (it) {
                        Modifier
                    } else {
                        Modifier.background(AppSurfaceTokens.cardContainer(), shape)
                    }
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 15.dp),
            contentAlignment = Alignment.Center
        ) {
            AppText(label, color = MusicContentColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MusicActionSheetItem(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AppText(label, color = MusicContentColor, style = MaterialTheme.typography.bodyLarge)
    }
}

private suspend fun loadMusicArtwork(
    imageLoader: ImageLoader,
    coverUrl: String,
    context: android.content.Context
): Pair<ImageBitmap, Color>? = withContext(Dispatchers.IO) {
    if (coverUrl.isBlank()) return@withContext null
    runCatching {
        val request = ImageRequest.Builder(context)
            .data(coverUrl)
            .allowHardware(false)
            .size(512, 512)
            .build()
        val result = imageLoader.execute(request) as SuccessResult
        val bitmap = (result.drawable as BitmapDrawable).bitmap
        val palette = Palette.from(bitmap).clearFilters().generate()
        val colorInt = palette.mutedSwatch?.rgb
            ?: palette.darkMutedSwatch?.rgb
            ?: palette.dominantSwatch?.rgb
            ?: 0xFF342B42.toInt()
        bitmap.asImageBitmap() to Color(colorInt)
    }.getOrNull()
}

internal fun formatMusicTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}
