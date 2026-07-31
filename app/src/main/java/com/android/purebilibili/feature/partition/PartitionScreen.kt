// 文件路径: feature/partition/PartitionScreen.kt
package com.android.purebilibili.feature.partition
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
//  Cupertino Icons - iOS SF Symbols 风格图标
import io.github.alexzhirkevich.cupertino.icons.CupertinoIcons
import io.github.alexzhirkevich.cupertino.icons.outlined.*
import io.github.alexzhirkevich.cupertino.icons.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocalMovies
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.TheaterComedy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.AdaptivePullToRefreshBox
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.CutePersonLoadingIndicator
import com.android.purebilibili.core.util.resolveReplaceRefreshPage
import com.android.purebilibili.core.ui.animation.DampedDragAnimationState
import com.android.purebilibili.core.ui.LocalSharedTransitionEnabled
import com.android.purebilibili.core.ui.globalWallpaperAwareBackground
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.store.BottomBarLiquidGlassPreset
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.rememberAppChromeLiquidGlassEnabled
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.data.model.response.BangumiType
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.data.repository.VideoRepository
import com.android.purebilibili.feature.common.resolveIndexedVideoLazyKey
import com.android.purebilibili.feature.home.components.cards.HomeStyleSingleColumnVideoCard
import com.android.purebilibili.feature.home.resolveHomeFeedCardLayout
import com.android.purebilibili.feature.home.components.BottomBarIndicatorLayerTransform
import com.android.purebilibili.feature.home.components.BottomBarLiquidOrientation
import com.android.purebilibili.feature.home.components.BottomBarMatchedLiquidIndicator
import com.android.purebilibili.feature.home.components.bottomBarMatchedCaptureOverflow
import com.android.purebilibili.feature.home.components.rememberBottomBarMatchedLiquidChromeState
import com.android.purebilibili.feature.home.components.resolveAndroidNativeIdleIndicatorSurfaceColor
import com.android.purebilibili.feature.home.components.resolveBottomBarBackdropPresetIndicatorLens
import com.android.purebilibili.feature.home.components.resolveBottomBarCaptureSafeInsetDp
import com.android.purebilibili.feature.home.components.resolveBottomBarBackdropPresetProgress
import com.android.purebilibili.feature.home.components.resolveBottomBarIndicatorGlowAlpha
import com.android.purebilibili.feature.home.components.resolveBottomBarLiquidGlassHighlightAlpha
import com.android.purebilibili.feature.home.components.resolveBottomBarRefractionMotionProfile
import com.android.purebilibili.feature.home.components.resolveSharedBottomBarCapsuleShape
import com.android.purebilibili.feature.home.components.rememberBottomBarIndicatorDragScaleProgress
import com.android.purebilibili.feature.home.components.normalizeTopTabLabelMode
import com.android.purebilibili.feature.home.components.resolveTopTabCategoryIcon
import com.android.purebilibili.feature.home.components.resolveSegmentedControlMotionProgress
import com.android.purebilibili.feature.home.components.resolveSegmentedControlMotionSpec
import com.android.purebilibili.feature.home.components.shouldShowTopTabIcon
import com.android.purebilibili.feature.home.components.shouldShowTopTabText
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.android.purebilibili.core.ui.blur.unifiedBlur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 *  分区数据类
 */
data class PartitionCategory(
    val id: Int,
    val name: String
)

/**
 *  所有分区列表 (参考官方 Bilibili API)
 * tid 是 Bilibili 官方的分区 ID，用于 x/web-interface/newlist 接口
 * 注意：番剧/国创/电影/电视剧/纪录片是特殊分区，使用不同的 API
 */
val allPartitions = listOf(
    // === 视频分区（支持 newlist API）===
    PartitionCategory(1, "动画"),
    PartitionCategory(13, "番剧"),      // 特殊分区
    PartitionCategory(167, "国创"),     // 特殊分区
    PartitionCategory(3, "音乐"),
    PartitionCategory(129, "舞蹈"),
    PartitionCategory(4, "游戏"),
    PartitionCategory(36, "知识"),
    PartitionCategory(188, "科技"),
    PartitionCategory(234, "运动"),
    PartitionCategory(223, "汽车"),
    PartitionCategory(160, "生活"),
    PartitionCategory(211, "美食"),
    PartitionCategory(217, "动物圈"),
    PartitionCategory(119, "鬼畜"),
    PartitionCategory(155, "时尚"),
    PartitionCategory(202, "资讯"),
    PartitionCategory(5, "娱乐"),
    // === 特殊分区（番剧/电影等使用不同 API）===
    PartitionCategory(23, "电影"),      // 特殊分区
    PartitionCategory(11, "电视剧"),    // 特殊分区
    PartitionCategory(177, "纪录片"),   // 特殊分区
    PartitionCategory(181, "影视")      // 特殊分区
)

private val partitionTabs = listOf(
    PartitionCategory(0, "全站")
) + allPartitions

private val PartitionSideRailItemHeight = 48.dp
private val PartitionSideRailItemSpacing = 4.dp
private val PartitionVideoListMaxPush = 20.dp

internal fun resolvePartitionBangumiType(partitionId: Int): Int? = when (partitionId) {
    13 -> BangumiType.ANIME.value
    167 -> BangumiType.GUOCHUANG.value
    23 -> BangumiType.MOVIE.value
    11 -> BangumiType.TV_SHOW.value
    177 -> BangumiType.DOCUMENTARY.value
    else -> null
}

internal data class PartitionSideRailIndicatorHorizontalPadding(
    val start: androidx.compose.ui.unit.Dp,
    val end: androidx.compose.ui.unit.Dp
)

internal fun resolvePartitionSideRailLabelMode(requestedLabelMode: Int): Int =
    normalizeTopTabLabelMode(requestedLabelMode)

internal fun shouldShowPartitionSideRailIcon(labelMode: Int): Boolean =
    shouldShowTopTabIcon(resolvePartitionSideRailLabelMode(labelMode))

internal fun shouldShowPartitionSideRailText(labelMode: Int): Boolean =
    shouldShowTopTabText(resolvePartitionSideRailLabelMode(labelMode))

internal fun resolvePartitionSideRailIcon(
    partitionId: Int,
    iconFamily: AppSemanticIconFamily,
    selected: Boolean,
): ImageVector = when (iconFamily) {
    AppSemanticIconFamily.MATERIAL -> when (partitionId) {
        0 -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
        1 -> if (selected) Icons.Filled.Animation else Icons.Outlined.Animation
        13 -> resolveTopTabCategoryIcon("番剧", iconFamily, selected)
        167 -> if (selected) Icons.Filled.Flag else Icons.Outlined.Flag
        3 -> if (selected) Icons.Filled.MusicNote else Icons.Outlined.MusicNote
        129 -> if (selected) Icons.Filled.DirectionsRun else Icons.Outlined.DirectionsRun
        4 -> resolveTopTabCategoryIcon("游戏", iconFamily, selected)
        36 -> resolveTopTabCategoryIcon("知识", iconFamily, selected)
        188 -> resolveTopTabCategoryIcon("科技", iconFamily, selected)
        234 -> if (selected) Icons.Filled.SportsSoccer else Icons.Outlined.SportsSoccer
        223 -> if (selected) Icons.Filled.DirectionsCar else Icons.Outlined.DirectionsCar
        160 -> resolveTopTabCategoryIcon("推荐", iconFamily, selected)
        211 -> if (selected) Icons.Filled.Restaurant else Icons.Outlined.Restaurant
        217 -> if (selected) Icons.Filled.Pets else Icons.Outlined.Pets
        119, 5 -> if (selected) Icons.Filled.TheaterComedy else Icons.Outlined.TheaterComedy
        155 -> if (selected) Icons.Filled.Checkroom else Icons.Outlined.Checkroom
        202 -> if (selected) Icons.Filled.Newspaper else Icons.Outlined.Newspaper
        23 -> if (selected) Icons.Filled.Movie else Icons.Outlined.Movie
        11 -> resolveTopTabCategoryIcon("番剧", iconFamily, selected)
        177 -> if (selected) Icons.Filled.OndemandVideo else Icons.Outlined.OndemandVideo
        181 -> if (selected) Icons.Filled.LocalMovies else Icons.Outlined.LocalMovies
        else -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
    }
    AppSemanticIconFamily.CUPERTINO -> when (partitionId) {
        0 -> CupertinoIcons.Outlined.Grid
        1, 23 -> if (selected) CupertinoIcons.Filled.Film else CupertinoIcons.Outlined.Film
        13, 11 -> resolveTopTabCategoryIcon("番剧", iconFamily, selected)
        167 -> if (selected) CupertinoIcons.Filled.Flag else CupertinoIcons.Outlined.Flag
        3 -> CupertinoIcons.Outlined.MusicNote
        129 -> CupertinoIcons.Outlined.FigureWalk
        4 -> resolveTopTabCategoryIcon("游戏", iconFamily, selected)
        36 -> resolveTopTabCategoryIcon("知识", iconFamily, selected)
        188 -> resolveTopTabCategoryIcon("科技", iconFamily, selected)
        234 -> CupertinoIcons.Outlined.Soccerball
        223 -> if (selected) CupertinoIcons.Filled.Car else CupertinoIcons.Outlined.Car
        160 -> resolveTopTabCategoryIcon("推荐", iconFamily, selected)
        211 -> CupertinoIcons.Outlined.ForkKnife
        217 -> if (selected) CupertinoIcons.Filled.Pawprint else CupertinoIcons.Outlined.Pawprint
        119, 5 -> if (selected) CupertinoIcons.Filled.Theatermasks else CupertinoIcons.Outlined.Theatermasks
        155 -> if (selected) CupertinoIcons.Filled.Tshirt else CupertinoIcons.Outlined.Tshirt
        202 -> if (selected) CupertinoIcons.Filled.Newspaper else CupertinoIcons.Outlined.Newspaper
        177 -> if (selected) CupertinoIcons.Filled.Video else CupertinoIcons.Outlined.Video
        181 -> if (selected) CupertinoIcons.Filled.Popcorn else CupertinoIcons.Outlined.Popcorn
        else -> CupertinoIcons.Outlined.Grid
    }
}

internal fun resolvePartitionSideRailIndicatorHorizontalPadding(
    contentPadding: PaddingValues,
    layoutDirection: LayoutDirection
): PartitionSideRailIndicatorHorizontalPadding {
    return PartitionSideRailIndicatorHorizontalPadding(
        start = contentPadding.calculateStartPadding(layoutDirection),
        end = contentPadding.calculateEndPadding(layoutDirection)
    )
}

data class PartitionFeedUiState(
    val selectedPartition: PartitionCategory = partitionTabs.first(),
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

class PartitionFeedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PartitionFeedUiState())
    val uiState = _uiState.asStateFlow()

    private var currentPage = 1
    private var hasMore = true
    private var requestGeneration = 0

    init {
        loadSelectedPartition(mode = PartitionLoadMode.RESET)
    }

    fun selectPartition(partition: PartitionCategory) {
        if (_uiState.value.selectedPartition.id == partition.id) return
        _uiState.update {
            it.copy(
                selectedPartition = partition,
                videos = emptyList(),
                error = null
            )
        }
        loadSelectedPartition(mode = PartitionLoadMode.RESET)
    }

    fun loadMore() {
        loadSelectedPartition(mode = PartitionLoadMode.APPEND)
    }

    fun refresh() {
        if (_uiState.value.isRefreshing) return
        loadSelectedPartition(mode = PartitionLoadMode.REPLACE_REFRESH)
    }

    private fun loadSelectedPartition(mode: PartitionLoadMode) {
        val isRefresh = mode == PartitionLoadMode.REPLACE_REFRESH
        val isReset = mode == PartitionLoadMode.RESET
        if (_uiState.value.isLoading && !isReset && !isRefresh) return
        if (mode == PartitionLoadMode.APPEND && !hasMore) return

        val pageToFetch = when (mode) {
            PartitionLoadMode.RESET -> 1
            PartitionLoadMode.APPEND -> currentPage
            PartitionLoadMode.REPLACE_REFRESH -> resolveReplaceRefreshPage(
                nextLoadPage = currentPage,
                hasMore = hasMore
            )
        }
        if (isReset || isRefresh) {
            if (isReset) {
                currentPage = 1
                hasMore = true
            }
            requestGeneration++
        }
        val generation = requestGeneration
        val partition = _uiState.value.selectedPartition
        val replaceList = isReset || isRefresh

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = !isRefresh,
                    isRefreshing = isRefresh,
                    error = null
                )
            }
            val result = if (partition.id == 0) {
                VideoRepository.getPopularVideos(page = pageToFetch)
            } else {
                VideoRepository.getRegionVideos(tid = partition.id, page = pageToFetch)
            }
            if (generation != requestGeneration) return@launch

            result
                .onSuccess { newVideos ->
                    hasMore = newVideos.isNotEmpty()
                    currentPage = if (newVideos.isNotEmpty()) {
                        pageToFetch + 1
                    } else if (isRefresh) {
                        1
                    } else {
                        currentPage
                    }
                    _uiState.update { state ->
                        val nextVideos = when {
                            isRefresh && newVideos.isEmpty() -> state.videos
                            replaceList -> newVideos
                            else -> state.videos + newVideos
                        }
                        state.copy(
                            videos = nextVideos,
                            isLoading = false,
                            isRefreshing = false,
                            error = if (nextVideos.isEmpty()) {
                                if (isRefresh || isReset) "没有更多内容了" else state.error
                            } else {
                                null
                            }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message ?: "加载失败"
                        )
                    }
                }
        }
    }
}

private enum class PartitionLoadMode {
    RESET,
    APPEND,
    REPLACE_REFRESH
}

/**
 *  分区页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartitionScreen(
    onBack: () -> Unit,
    onVideoClick: (String, Long, String) -> Unit = { _, _, _ -> },
    onBangumiClick: (Int) -> Unit = {}
) {
    val hazeState = com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    AppScaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = "分区",
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        AppIcon(rememberAppBackIcon(), contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                modifier = Modifier.unifiedBlur(
                    hazeState = hazeState
                )
            )
        }
    ) { paddingValues ->
        PartitionContent(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            hazeState = hazeState,
            onVideoClick = { video -> onVideoClick(video.bvid, video.cid, video.pic) },
            onBangumiClick = onBangumiClick
        )
    }
}

/**
 * 分区主体内容。独立页面和首页内嵌分区页共用，避免两套分区网格状态分叉。
 */
@Composable
fun PartitionContent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        top = 8.dp,
        bottom = 16.dp,
        start = 16.dp,
        end = 16.dp
    ),
    hazeState: HazeState? = null,
    onVideoClick: (VideoItem) -> Unit = {},
    onBangumiClick: (Int) -> Unit = {},
    viewModel: PartitionFeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val homeSettings by SettingsManager.getHomeSettings(context).collectAsStateWithLifecycle(initialValue = HomeSettings())
    val topChromeIconFamily = rememberAppTopChromePolicy().iconFamily
    val liquidGlassIndicatorEnabled = rememberAppChromeLiquidGlassEnabled(
        individualEnabled = homeSettings.isBottomBarLiquidGlassEnabled,
        androidNativeEnabled = homeSettings.androidNativeLiquidGlassEnabled,
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val layoutDirection = LocalLayoutDirection.current
    val startPadding = contentPadding.calculateStartPadding(layoutDirection)
    val endPadding = contentPadding.calculateEndPadding(layoutDirection)
    val topPadding = contentPadding.calculateTopPadding()
    val bottomPadding = contentPadding.calculateBottomPadding()
    var sideRailVideoPushTargetPx by remember { mutableFloatStateOf(0f) }
    val sideRailVideoPushPx by animateFloatAsState(
        targetValue = sideRailVideoPushTargetPx,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "partitionVideoListPush"
    )

    val shouldLoadMore by remember(state.videos.size, state.isLoading) {
        derivedStateOf {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex != null && lastVisibleIndex >= state.videos.lastIndex - 4
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.isLoading && state.videos.isNotEmpty()) {
            viewModel.loadMore()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .globalWallpaperAwareBackground()
            .responsiveContentWidth(maxWidth = 1000.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (hazeState != null) {
                        Modifier.hazeSource(state = hazeState)
                    } else {
                        Modifier
                    }
                )
        ) {
            PartitionSideRail(
                partitions = partitionTabs,
                selectedId = state.selectedPartition.id,
                labelMode = homeSettings.topTabLabelMode,
                iconFamily = topChromeIconFamily,
                modifier = Modifier.width(92.dp),
                contentPadding = PaddingValues(
                    start = startPadding,
                    top = topPadding + 8.dp,
                    bottom = bottomPadding,
                    end = 4.dp
                ),
                liquidGlassIndicatorEnabled = liquidGlassIndicatorEnabled,
                liquidGlassPreset = homeSettings.bottomBarLiquidGlassPreset,
                onVideoListPushChanged = { sideRailVideoPushTargetPx = it },
                onPartitionSelected = { partition ->
                    val bangumiType = resolvePartitionBangumiType(partition.id)
                    if (bangumiType != null) {
                        onBangumiClick(bangumiType)
                    } else {
                        viewModel.selectPartition(partition)
                    }
                }
            )

            // Match list content top (status/insets + 8dp) so indicator sits above first row.
            val partitionRefreshIndicatorTopInset = topPadding + 8.dp
            AdaptivePullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                indicatorTopInset = partitionRefreshIndicatorTopInset,
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer { translationX = sideRailVideoPushPx }
            ) {
                PartitionVideoList(
                    state = state,
                    listState = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 8.dp,
                        top = partitionRefreshIndicatorTopInset,
                        end = endPadding,
                        bottom = bottomPadding
                    ),
                    onVideoClick = onVideoClick
                )
            }
        }
    }
}

@Composable
private fun PartitionSideRail(
    partitions: List<PartitionCategory>,
    selectedId: Int,
    labelMode: Int,
    iconFamily: AppSemanticIconFamily,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    liquidGlassIndicatorEnabled: Boolean,
    liquidGlassPreset: BottomBarLiquidGlassPreset,
    onVideoListPushChanged: (Float) -> Unit,
    onPartitionSelected: (PartitionCategory) -> Unit
) {
    val listState = rememberLazyListState()
    val selectedIndex = partitions.indexOfFirst { it.id == selectedId }.coerceAtLeast(0)
    val density = LocalDensity.current
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val resolvedLabelMode = resolvePartitionSideRailLabelMode(labelMode)
    val showIcon = shouldShowPartitionSideRailIcon(resolvedLabelMode)
    val showText = shouldShowPartitionSideRailText(resolvedLabelMode)
    val matchedChromeState = rememberBottomBarMatchedLiquidChromeState(
        initialIndex = selectedIndex,
        itemCount = partitions.size,
        orientation = BottomBarLiquidOrientation.VERTICAL,
        isScrollInProgressProvider = { listState.isScrollInProgress },
        onIndexChanged = { index ->
            partitions.getOrNull(index)?.let(onPartitionSelected)
        }
    )
    val dragState = matchedChromeState.dragState
    LaunchedEffect(selectedIndex) {
        dragState.updateIndex(selectedIndex)
    }

    BoxWithConstraints(modifier = modifier.fillMaxHeight()) {
        val itemHeightPx = with(density) { PartitionSideRailItemHeight.toPx() }
        val itemSlotHeightPx = with(density) { (PartitionSideRailItemHeight + PartitionSideRailItemSpacing).toPx() }
        val contentTopPaddingPx = with(density) { contentPadding.calculateTopPadding().toPx() }
        val indicatorHorizontalPadding = resolvePartitionSideRailIndicatorHorizontalPadding(
            contentPadding = contentPadding,
            layoutDirection = LocalLayoutDirection.current
        )
        val maxVideoPushPx = with(density) { PartitionVideoListMaxPush.toPx() }
        val currentIndicatorOffsetPxProvider = {
            resolvePartitionSideRailIndicatorOffsetPx(
                indicatorPosition = dragState.value,
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffsetPx = listState.firstVisibleItemScrollOffset,
                contentTopPaddingPx = contentTopPaddingPx,
                itemSlotHeightPx = itemSlotHeightPx
            )
        }
        val indicatorWidth = (maxWidth - indicatorHorizontalPadding.start - indicatorHorizontalPadding.end)
            .coerceAtLeast(0.dp)
        val fullIndicatorLensSpec = resolveBottomBarBackdropPresetIndicatorLens(progress = 1f)
        val captureSafeInset = resolveBottomBarCaptureSafeInsetDp(
            indicatorWidthDp = indicatorWidth.value,
            refractionHeightDp = fullIndicatorLensSpec.refractionHeightDp,
            refractionAmountDp = fullIndicatorLensSpec.refractionAmountDp,
            panelOffsetDp = 0f
        ).dp
        val railPageBackdrop = rememberLayerBackdrop()

        // The vertical indicator renders above the visible labels, so it only needs a stable page
        // sample. Sampling the scrolling LazyColumn becomes invalid while the long-press layer is
        // translated and scaled. The overflow keeps the 88/56 drag scale and lens inside capture.
        Box(
            modifier = Modifier
                .matchParentSize()
                .bottomBarMatchedCaptureOverflow(captureSafeInset)
                .alpha(0f)
                .layerBackdrop(railPageBackdrop)
                .background(AppSurfaceTokens.background())
        )

        PartitionSideRailMovingIndicator(
            dragState = dragState,
            itemSlotHeightPx = itemSlotHeightPx,
            indicatorOffsetPxProvider = currentIndicatorOffsetPxProvider,
            indicatorWidth = indicatorWidth,
            liquidGlassIndicatorEnabled = liquidGlassIndicatorEnabled,
            liquidGlassPreset = liquidGlassPreset,
            backdrop = railPageBackdrop,
            maxVideoPushPx = maxVideoPushPx,
            horizontalPadding = indicatorHorizontalPadding,
            onVideoListPushChanged = onVideoListPushChanged,
            modifier = Modifier.matchParentSize()
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .partitionSideRailIndicatorLongPressDrag(
                    dragState = dragState,
                    itemHeightPx = itemHeightPx,
                    itemSlotHeightPx = itemSlotHeightPx,
                    currentIndicatorTopPx = currentIndicatorOffsetPxProvider,
                    itemCount = partitions.size
                ),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(PartitionSideRailItemSpacing)
        ) {
            itemsIndexed(
                items = partitions,
                key = { _, partition -> partition.id }
            ) { index, partition ->
                PartitionSideRailItem(
                    partition = partition,
                    selected = partition.id == selectedId,
                    selectionProgress = resolvePartitionSideRailItemSelectionProgress(
                        itemIndex = index,
                        indicatorPosition = dragState.value
                    ),
                    showIcon = showIcon,
                    showText = showText,
                    iconFamily = iconFamily,
                    onClick = { onPartitionSelected(partition) }
                )
            }
        }
    }
}

@Composable
private fun PartitionSideRailMovingIndicator(
    dragState: DampedDragAnimationState,
    itemSlotHeightPx: Float,
    indicatorOffsetPxProvider: () -> Float,
    indicatorWidth: androidx.compose.ui.unit.Dp,
    liquidGlassIndicatorEnabled: Boolean,
    liquidGlassPreset: BottomBarLiquidGlassPreset,
    backdrop: top.yukonga.miuix.kmp.blur.Backdrop,
    maxVideoPushPx: Float,
    horizontalPadding: PartitionSideRailIndicatorHorizontalPadding,
    onVideoListPushChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = resolveSharedBottomBarCapsuleShape()
    val isDarkTheme = isSystemInDarkTheme()
    val motionSpec = remember { resolveSegmentedControlMotionSpec() }
    val pressProgress by remember {
        derivedStateOf { dragState.pressProgress }
    }
    val refractionMotionProfile = resolveBottomBarRefractionMotionProfile(
        position = dragState.value,
        velocity = dragState.velocityPxPerSecond,
        isDragging = dragState.isDragging,
        motionSpec = motionSpec
    )
    val motionProgress = resolveSegmentedControlMotionProgress(
        pressProgress = pressProgress,
        refractionProgress = refractionMotionProfile.progress,
        tapPressRefractionEnabled = true
    )
    val videoListPushPx = resolvePartitionVideoListPushPx(
        pressProgress = pressProgress,
        dragOffsetPx = dragState.dragOffset,
        itemSlotHeightPx = itemSlotHeightPx,
        maxPushPx = maxVideoPushPx
    )
    SideEffect {
        onVideoListPushChanged(videoListPushPx)
    }
    val indicatorDragScaleProgress = rememberBottomBarIndicatorDragScaleProgress(
        isDragging = dragState.isDragging
    )
    val indicatorLayerScaleProgress = maxOf(indicatorDragScaleProgress, pressProgress)
    // Align with home bottom bar indicator: press-driven lens + no compound scale transform.
    val indicatorLensSpec = resolveBottomBarBackdropPresetIndicatorLens(
        progress = pressProgress
    )

    Box(modifier = modifier) {
        val density = LocalDensity.current
        BottomBarMatchedLiquidIndicator(
            visible = true,
            dockContentAlpha = 1f,
            indicatorTranslationXPx = with(density) { horizontalPadding.start.toPx() },
            indicatorTranslationYPx = indicatorOffsetPxProvider(),
            indicatorPanelOffsetPx = 0f,
            indicatorWidth = indicatorWidth,
            indicatorHeight = PartitionSideRailItemHeight,
            shellShape = shape,
            liquidGlassPreset = liquidGlassPreset,
            contentBackdrop = backdrop,
            backdrop = backdrop,
            indicatorLensSpec = indicatorLensSpec,
            effectivePressProgress = pressProgress,
            indicatorIdleSurfaceColor = resolveAndroidNativeIdleIndicatorSurfaceColor(darkTheme = isDarkTheme),
            glassEnabled = liquidGlassIndicatorEnabled,
            motionProgress = motionProgress,
            velocityItemsPerSecond = dragState.deformationVelocityItemsPerSecond,
            isDragging = dragState.isDragging,
            indicatorLayerScaleProgress = indicatorLayerScaleProgress,
            bottomBarMotionSpec = motionSpec,
            isDarkTheme = isDarkTheme,
            orientation = BottomBarLiquidOrientation.VERTICAL,
            indicatorAlignment = Alignment.TopStart
        )
    }
}

@Composable
private fun PartitionSideRailItem(
    partition: PartitionCategory,
    selected: Boolean,
    selectionProgress: Float,
    showIcon: Boolean,
    showText: Boolean,
    iconFamily: AppSemanticIconFamily,
    onClick: () -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val clampedSelectionProgress = selectionProgress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PartitionSideRailItemHeight)
            .clip(resolveSharedBottomBarCapsuleShape())
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val contentColor = when {
                clampedSelectionProgress > 0f -> lerp(
                    unselectedColor,
                    selectedColor,
                    clampedSelectionProgress
                )
                pressed -> MaterialTheme.colorScheme.onSurface
                else -> unselectedColor
            }
            if (showIcon) {
                AppIcon(
                    imageVector = resolvePartitionSideRailIcon(
                        partitionId = partition.id,
                        iconFamily = iconFamily,
                        selected = selected || clampedSelectionProgress > 0.5f,
                    ),
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (showText) 18.dp else 24.dp)
                )
            }
            if (showIcon && showText) {
                Spacer(modifier = Modifier.height(1.dp))
            }
            if (showText) {
                AppText(
                    text = partition.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontSize = if (showIcon) 12.sp else 16.sp,
                    lineHeight = if (showIcon) 14.sp else 20.sp,
                    fontWeight = if (selected || clampedSelectionProgress > 0.5f) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    color = contentColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

internal fun shouldStartPartitionSideRailIndicatorDrag(
    pointerY: Float,
    indicatorTopPx: Float,
    indicatorHeightPx: Float
): Boolean {
    if (indicatorHeightPx <= 0f) return false
    return pointerY in indicatorTopPx..(indicatorTopPx + indicatorHeightPx)
}

internal fun resolvePartitionSideRailIndicatorOffsetPx(
    indicatorPosition: Float,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffsetPx: Int,
    contentTopPaddingPx: Float,
    itemSlotHeightPx: Float
): Float {
    return contentTopPaddingPx +
        indicatorPosition * itemSlotHeightPx -
        firstVisibleItemIndex * itemSlotHeightPx -
        firstVisibleItemScrollOffsetPx
}

internal fun resolvePartitionSideRailItemSelectionProgress(
    itemIndex: Int,
    indicatorPosition: Float
): Float {
    return (1f - abs(indicatorPosition - itemIndex.toFloat())).coerceIn(0f, 1f)
}

internal fun resolvePartitionVideoListPushPx(
    pressProgress: Float,
    dragOffsetPx: Float,
    itemSlotHeightPx: Float,
    maxPushPx: Float
): Float {
    if (maxPushPx <= 0f) return 0f
    val dragProgress = if (itemSlotHeightPx > 0f) {
        (abs(dragOffsetPx) / itemSlotHeightPx).coerceIn(0f, 1f)
    } else {
        0f
    }
    val progress = max(pressProgress.coerceIn(0f, 1f), dragProgress * 0.65f)
    return maxPushPx * EaseOut.transform(progress)
}

private fun Modifier.partitionSideRailIndicatorLongPressDrag(
    dragState: DampedDragAnimationState,
    itemHeightPx: Float,
    itemSlotHeightPx: Float,
    currentIndicatorTopPx: () -> Float,
    itemCount: Int
): Modifier = pointerInput(dragState, itemHeightPx, itemSlotHeightPx, itemCount) {
    val velocityTracker = VelocityTracker()
    awaitPointerEventScope {
        while (true) {
            val down = awaitFirstDown(requireUnconsumed = false)
            if (!shouldStartPartitionSideRailIndicatorDrag(
                    pointerY = down.position.y,
                    indicatorTopPx = currentIndicatorTopPx(),
                    indicatorHeightPx = itemHeightPx
                )
            ) {
                continue
            }

            val longPress = awaitLongPressOrCancellation(down.id) ?: continue
            longPress.consume()
            velocityTracker.resetTracking()
            velocityTracker.addPosition(longPress.uptimeMillis, longPress.position)
            dragState.onDrag(0f, itemSlotHeightPx)

            var isCancelled = false
            try {
                verticalDrag(longPress.id) { change ->
                    change.consume()
                    velocityTracker.addPosition(change.uptimeMillis, change.position)
                    val dragAmount = change.position.y - change.previousPosition.y
                    val velocityY = velocityTracker.calculateVelocity().y
                    dragState.onDrag(dragAmount, itemSlotHeightPx, velocityY)
                }
            } catch (e: Exception) {
                isCancelled = true
            }

            val velocityY = if (isCancelled) 0f else velocityTracker.calculateVelocity().y
            dragState.onDragEnd(
                velocityX = velocityY,
                itemWidthPx = itemSlotHeightPx,
                notifyIndexChanged = true
            )
        }
    }
}

@Composable
private fun PartitionVideoList(
    state: PartitionFeedUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
    onVideoClick: (VideoItem) -> Unit
) {
    val context = LocalContext.current
    val homeFeedCardStyle by SettingsManager
        .getHomeFeedCardStyle(context)
        .collectAsStateWithLifecycle(initialValue = HomeFeedCardStyle.OFFICIAL)
    val cardLayout = remember(homeFeedCardStyle) {
        resolveHomeFeedCardLayout(homeFeedCardStyle)
    }
    val sharedTransitionEnabled = LocalSharedTransitionEnabled.current
    val sharedElementSourceRoute = LocalVideoCardSharedElementSourceRoute.current
        ?.takeIf { it.isNotBlank() }
        ?: "partition"
    when {
        state.videos.isEmpty() && state.isLoading -> {
            Box(modifier = modifier.fillMaxHeight()) {
                CutePersonLoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        state.videos.isEmpty() && state.error != null -> {
            Box(modifier = modifier.fillMaxHeight()) {
                AppText(
                    text = state.error,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        else -> {
            LazyColumn(
                state = listState,
                modifier = modifier.fillMaxHeight(),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = state.videos,
                    key = { index, video ->
                        resolveIndexedVideoLazyKey(
                            namespace = "partition_feed_item",
                            index = index,
                            bvid = video.bvid,
                            aid = video.aid,
                            cid = video.cid
                        )
                    }
                ) { _, video ->
                    HomeStyleSingleColumnVideoCard(
                        video = video,
                        sourceRoute = sharedElementSourceRoute,
                        coverAspectRatio = cardLayout.coverAspectRatio,
                        transitionEnabled = sharedTransitionEnabled,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onVideoClick(video) },
                    )
                }

                if (state.isLoading) {
                    item(key = "partition_loading_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CutePersonLoadingIndicator(size = 24.dp)
                        }
                    }
                }
            }
        }
    }
}
