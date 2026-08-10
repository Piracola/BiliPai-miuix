// 文件路径: feature/watchlater/WatchLaterScreen.kt
package com.android.purebilibili.feature.watchlater
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.MediaContrastPalette

import android.app.Application
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import com.android.purebilibili.core.ui.animation.DissolveAnimationPreset
import com.android.purebilibili.core.ui.animation.MaybeDissolvableVideoCard
import com.android.purebilibili.core.ui.animation.jiggleOnDissolve
import com.android.purebilibili.core.ui.blur.rememberRecoverableHazeState
import dev.chrisbanes.haze.HazeState
import com.android.purebilibili.core.ui.blur.hazeSourceCompat
import com.android.purebilibili.core.ui.blur.unifiedBlur
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.coroutines.AppScope
import com.android.purebilibili.core.refresh.WatchLaterRefreshBus
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalBottomBarContentPadding
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppDropdownMenu
import com.android.purebilibili.core.ui.components.AppDropdownMenuItem
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppSearchField
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.ui.rememberAppPlayIcon
import com.android.purebilibili.core.ui.rememberAppWatchLaterIcon
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.LocalVideoSharedTransitionSpeedSettings
import com.android.purebilibili.core.ui.transition.resolveVideoCardSharedTransitionMotionSpec
import com.android.purebilibili.core.ui.transition.shouldUseVideoCardShellSharedBounds
import com.android.purebilibili.core.ui.transition.videoCardShellSharedBoundsOrEmpty
import com.android.purebilibili.feature.home.components.cards.videoCardShellReturnChromeAlpha
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.data.model.response.FavFolder
import com.android.purebilibili.data.repository.FavoriteRepository
import com.android.purebilibili.data.repository.WatchLaterRepository
import com.android.purebilibili.feature.common.resolveIndexedVideoLazyKey
import com.android.purebilibili.feature.personal.PersonalMediaCardFrame
import com.android.purebilibili.core.util.CardPositionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import com.android.purebilibili.core.util.FormatUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// 辅助函数：格式化时长
private fun formatDuration(seconds: Int): String {
    return FormatUtils.formatDuration(seconds)
}

// 辅助函数：格式化数字
private fun formatNumber(num: Int): String {
    return when {
        num >= 10000 -> String.format("%.1f万", num / 10000f)
        else -> num.toString()
    }
}

// 辅助函数：修复封面 URL 协议（B站API可能返回http或缺少协议的URL）
private fun fixCoverUrl(url: String?): String {
    if (url.isNullOrEmpty()) return ""
    return when {
        url.startsWith("//") -> "https:$url"
        url.startsWith("http://") -> url.replaceFirst("http://", "https://")
        else -> url
    }
}

private const val WATCH_LATER_DELETE_MAX_ATTEMPTS = 3
private const val WATCH_LATER_DELETE_RETRY_BASE_DELAY_MS = 850L

internal fun isRetryableWatchLaterDeleteError(code: Int, message: String): Boolean {
    if (code in setOf(-412, -352, -509, 22015, 34004)) return true
    if (message.isBlank()) return false
    return message.contains("频繁") ||
        message.contains("过快") ||
        message.contains("风控") ||
        message.contains("稍后") ||
        message.contains("too many", ignoreCase = true) ||
        message.contains("rate", ignoreCase = true)
}

internal fun resolveWatchLaterPlayAllStartTarget(
    items: List<VideoItem>
): Pair<String, Long>? {
    val first = items.firstOrNull() ?: return null
    return first.bvid to first.cid
}

private fun resolveWatchLaterPlaybackTargetOrDefault(
    items: List<VideoItem>,
    bvid: String,
    fallbackCid: Long = 0L
): WatchLaterPlaybackTarget {
    return resolveWatchLaterPlaybackTarget(items, bvid)
        ?: WatchLaterPlaybackTarget(
            bvid = bvid,
            cid = fallbackCid.coerceAtLeast(0L),
            resumePositionMs = 0L
        )
}

internal fun resolveWatchLaterTitle(itemCount: Int): String {
    return "稍后再看 ($itemCount)"
}

/**
 * 稍后再看 UI 状态
 */
data class WatchLaterUiState(
    val items: List<VideoItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
    val isManaging: Boolean = false,
    val isLoadingMore: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val filter: WatchLaterFilter = WatchLaterFilter.ALL,
    val query: String = "",
    val sortOrder: WatchLaterSortOrder = WatchLaterSortOrder.FORWARD,
    val favoriteFolders: List<FavFolder> = emptyList(),
    val isTransferLoading: Boolean = false,
    val error: String? = null,
    val dissolvingIds: Set<String> = emptySet() // [新增] 用于已播放 Thanos Snap 动画的卡片
)

/**
 * 稍后再看 ViewModel
 */
class WatchLaterViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WatchLaterUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private data class WatchLaterManagementSnapshot(
        val state: WatchLaterUiState,
        val affectedCount: Int
    )
    private val tabCache = mutableMapOf<WatchLaterFilter, WatchLaterUiState>()
    private var loadGeneration = 0L
    
    init {
        observeWatchLaterRefresh()
    }

    private fun observeWatchLaterRefresh() {
        viewModelScope.launch {
            WatchLaterRefreshBus.changes.collect {
                loadData(showLoading = false)
            }
        }
    }
    
    fun loadData(showLoading: Boolean = true) = loadPage(reset = true, showLoading = showLoading)

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        loadPage(reset = false, showLoading = false)
    }

    fun selectFilter(filter: WatchLaterFilter) {
        if (filter == _uiState.value.filter) return
        tabCache[_uiState.value.filter] = _uiState.value.copy(isLoading = false, isLoadingMore = false)
        val current = _uiState.value
        val cached = tabCache[filter]
        _uiState.value = if (
            cached != null && cached.query == current.query && cached.sortOrder == current.sortOrder
        ) {
            cached.copy(filter = filter, dissolvingIds = emptySet())
        } else {
            WatchLaterUiState(
                isLoading = true,
                filter = filter,
                query = current.query,
                sortOrder = current.sortOrder,
                favoriteFolders = current.favoriteFolders,
            )
        }
        if (cached == null || cached.query != current.query || cached.sortOrder != current.sortOrder) {
            loadPage(reset = true, showLoading = true)
        }
    }

    fun updateQuery(query: String) {
        if (query == _uiState.value.query) return
        _uiState.value = _uiState.value.copy(query = query)
        tabCache.clear()
        loadPage(reset = true, showLoading = true)
    }

    fun updateSortOrder(order: WatchLaterSortOrder) {
        if (order == _uiState.value.sortOrder) return
        _uiState.value = _uiState.value.copy(sortOrder = order)
        tabCache.clear()
        loadPage(reset = true, showLoading = true)
    }

    private fun loadPage(reset: Boolean, showLoading: Boolean) {
        val request = _uiState.value
        val requestPage = if (reset) 1 else request.page + 1
        val generation = ++loadGeneration
        _uiState.value = if (reset) {
            request.copy(
                isLoading = showLoading || request.items.isEmpty(),
                isLoadingMore = false,
                error = null,
                page = 1,
                hasMore = false,
            )
        } else {
            request.copy(isLoadingMore = true, error = null)
        }
        viewModelScope.launch {
            val result = WatchLaterRepository.getPage(
                page = requestPage,
                viewed = request.filter.viewed,
                keyword = request.query,
                ascending = request.sortOrder == WatchLaterSortOrder.REVERSE,
            )
            if (generation != loadGeneration || _uiState.value.filter != request.filter) return@launch
            result.fold(
                onSuccess = { page ->
                    val current = _uiState.value
                    val next = current.copy(
                        items = if (reset) page.items else (current.items + page.items).distinctBy { it.aid },
                        totalCount = page.totalCount,
                        isLoading = false,
                        isLoadingMore = false,
                        page = requestPage,
                        hasMore = page.hasMore,
                        error = null,
                    )
                    _uiState.value = next
                    tabCache[next.filter] = next
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message ?: "加载失败",
                    )
                },
            )
        }
    }
    
    // [新增] 开始消散动画
    fun startVideoDissolve(bvid: String) {
        _uiState.value = _uiState.value.copy(
            dissolvingIds = _uiState.value.dissolvingIds + bvid
        )
    }

    // [新增] 动画完成，执行删除
    fun completeVideoDissolve(bvid: String) {
        // 先从 UI 状态移除 ID（动画结束），然后调用删除逻辑
        _uiState.value = _uiState.value.copy(
            dissolvingIds = _uiState.value.dissolvingIds - bvid
        )
        // 查找对应的 aid 进行删除
        val item = _uiState.value.items.find { it.bvid == bvid }
        item?.let { deleteItem(it.id) }
    }

    /**
     * 从稍后再看删除视频
     */
    fun deleteItem(aid: Long) {
        // 乐观更新：直接从列表中移除，不需要重新请求
        val snapshotState = _uiState.value
        val currentList = snapshotState.items
        val newList = currentList.filter { it.id != aid }
        val removedBvid = currentList.firstOrNull { it.id == aid }?.bvid
        _uiState.value = _uiState.value.copy(
            items = newList,
            totalCount = (snapshotState.totalCount - (currentList.size - newList.size)).coerceAtLeast(newList.size),
            dissolvingIds = if (removedBvid == null) {
                _uiState.value.dissolvingIds
            } else {
                _uiState.value.dissolvingIds - removedBvid
            }
        )

        viewModelScope.launch {
            try {
                val csrf = com.android.purebilibili.core.store.TokenManager.csrfCache ?: ""
                if (csrf.isEmpty()) {
                    _uiState.value = snapshotState
                    android.widget.Toast.makeText(getApplication(), "请先登录", android.widget.Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val result = deleteWatchLaterAidWithRetry(aid = aid, csrf = csrf)
                if (result.isSuccess) {
                    tabCache.clear()
                    android.widget.Toast.makeText(getApplication(), "已从稍后再看移除", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    _uiState.value = snapshotState
                    android.widget.Toast.makeText(
                        getApplication(),
                        "移除失败: ${result.exceptionOrNull()?.message ?: "请稍后重试"}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = snapshotState
                android.widget.Toast.makeText(getApplication(), "移除失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteItems(aids: List<Long>) {
        if (aids.isEmpty()) return
        val aidSet = aids.toSet()
        val snapshotState = _uiState.value
        val snapshot = snapshotState.items
        val removeCount = snapshot.count { it.id in aidSet }
        val optimisticItems = snapshot.filterNot { it.id in aidSet }
        _uiState.value = _uiState.value.copy(
            items = optimisticItems,
            totalCount = (snapshotState.totalCount - removeCount).coerceAtLeast(optimisticItems.size),
            dissolvingIds = _uiState.value.dissolvingIds - snapshot.filter { it.id in aidSet }.map { it.bvid }.toSet()
        )

        AppScope.ioScope.launch {
            try {
                val csrf = com.android.purebilibili.core.store.TokenManager.csrfCache ?: ""
                if (csrf.isEmpty()) {
                    withContext(Dispatchers.Main.immediate) {
                        _uiState.value = snapshotState
                        android.widget.Toast.makeText(getApplication(), "请先登录", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val successIds = deleteWatchLaterItemsInBackground(aids = aids, csrf = csrf)

                withContext(Dispatchers.Main.immediate) {
                    val successCount = successIds.size
                    if (successCount > 0) tabCache.clear()
                    _uiState.value = _uiState.value.copy(
                        items = snapshot.filterNot { it.id in successIds },
                        totalCount = (snapshotState.totalCount - successCount).coerceAtLeast(
                            snapshot.count { it.id !in successIds }
                        ),
                        dissolvingIds = _uiState.value.dissolvingIds -
                            snapshot.filter { it.id in successIds }.map { it.bvid }.toSet()
                    )

                    if (successCount == aids.size) {
                        android.widget.Toast.makeText(getApplication(), "已删除 ${aids.size} 个视频", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(
                            getApplication(),
                            "批量删除完成：成功 $successCount / ${aids.size}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main.immediate) {
                    _uiState.value = snapshotState
                    android.widget.Toast.makeText(getApplication(), "批量删除失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun deleteWatchLaterItemsInBackground(
        aids: List<Long>,
        csrf: String
    ): Set<Long> = withContext(Dispatchers.IO) {
        val response = NetworkModule.api.deleteMultipleFromWatchLater(
            aids = aids.distinct().joinToString(","),
            csrf = csrf,
        )
        if (response.code == 0) aids.toSet() else emptySet()
    }

    internal fun runManagementAction(action: WatchLaterManagementAction) {
        val snapshotState = _uiState.value
        if (snapshotState.isManaging) return
        val snapshot = applyWatchLaterManagementOptimisticState(snapshotState, action)

        viewModelScope.launch {
            val csrf = com.android.purebilibili.core.store.TokenManager.csrfCache.orEmpty()
            if (csrf.isBlank()) {
                _uiState.value = snapshot.state
                android.widget.Toast.makeText(getApplication(), "请先登录", android.widget.Toast.LENGTH_SHORT).show()
                return@launch
            }

            val result = executeWatchLaterManagementAction(action = action, csrf = csrf)
            handleWatchLaterManagementResult(action, snapshot, result)
        }
    }

    private fun applyWatchLaterManagementOptimisticState(
        snapshotState: WatchLaterUiState,
        action: WatchLaterManagementAction
    ): WatchLaterManagementSnapshot {
        val optimisticItems = resolveWatchLaterItemsAfterManagementAction(
            items = snapshotState.items,
            action = action
        )
        val affectedCount = (snapshotState.items.size - optimisticItems.size).coerceAtLeast(0)
        val optimisticBvids = optimisticItems.map { it.bvid }.toSet()
        val removedBvids = snapshotState.items.map { it.bvid }.filterNot { it in optimisticBvids }.toSet()
        _uiState.value = snapshotState.copy(
            items = optimisticItems,
            totalCount = when (action) {
                WatchLaterManagementAction.CLEAR_INVALID -> snapshotState.totalCount
                WatchLaterManagementAction.CLEAR_VIEWED ->
                    (snapshotState.totalCount - affectedCount).coerceAtLeast(optimisticItems.size)
                WatchLaterManagementAction.CLEAR_ALL -> 0
            },
            isManaging = true,
            dissolvingIds = snapshotState.dissolvingIds - removedBvids
        )
        return WatchLaterManagementSnapshot(snapshotState, affectedCount)
    }

    private fun handleWatchLaterManagementResult(
        action: WatchLaterManagementAction,
        snapshot: WatchLaterManagementSnapshot,
        result: Result<Unit>
    ) {
        if (result.isSuccess) {
            tabCache.clear()
            _uiState.value = _uiState.value.copy(isManaging = false)
            android.widget.Toast.makeText(
                getApplication(),
                resolveWatchLaterManagementSuccessMessage(action, snapshot.affectedCount),
                android.widget.Toast.LENGTH_SHORT
            ).show()
            loadData()
        } else {
            _uiState.value = snapshot.state
            android.widget.Toast.makeText(
                getApplication(),
                "操作失败: ${result.exceptionOrNull()?.message ?: "请稍后重试"}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private suspend fun executeWatchLaterManagementAction(
        action: WatchLaterManagementAction,
        csrf: String
    ): Result<Unit> {
        if (csrf.isBlank()) return Result.failure(Exception("请先登录"))
        return WatchLaterRepository.clear(
            cleanType = when (action) {
                WatchLaterManagementAction.CLEAR_INVALID -> 1
                WatchLaterManagementAction.CLEAR_VIEWED -> 2
                WatchLaterManagementAction.CLEAR_ALL -> null
            }
        )
    }

    fun loadFavoriteFolders() {
        if (_uiState.value.isTransferLoading) return
        viewModelScope.launch {
            val mid = com.android.purebilibili.core.store.TokenManager.midCache
            if (mid == null) {
                android.widget.Toast.makeText(getApplication(), "请先登录", android.widget.Toast.LENGTH_SHORT).show()
                return@launch
            }
            _uiState.value = _uiState.value.copy(isTransferLoading = true)
            FavoriteRepository.getFavFolders(mid).fold(
                onSuccess = { folders ->
                    _uiState.value = _uiState.value.copy(
                        favoriteFolders = folders,
                        isTransferLoading = false,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(isTransferLoading = false)
                    android.widget.Toast.makeText(
                        getApplication(),
                        error.message ?: "加载收藏夹失败",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    fun copyOrMoveToFavorite(aids: Set<Long>, targetMediaId: Long, copy: Boolean) {
        if (aids.isEmpty() || _uiState.value.isTransferLoading) return
        val snapshot = _uiState.value
        if (!copy) {
            _uiState.value = snapshot.copy(
                items = snapshot.items.filterNot { it.aid in aids },
                totalCount = (snapshot.totalCount - aids.size).coerceAtLeast(0),
                isTransferLoading = true,
            )
        } else {
            _uiState.value = snapshot.copy(isTransferLoading = true)
        }
        viewModelScope.launch {
            WatchLaterRepository.copyOrMoveToFavorite(targetMediaId, aids, copy).fold(
                onSuccess = {
                    tabCache.clear()
                    _uiState.value = _uiState.value.copy(isTransferLoading = false)
                    android.widget.Toast.makeText(
                        getApplication(),
                        if (copy) "复制成功" else "移动成功",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                    if (!copy) loadData(showLoading = false)
                },
                onFailure = { error ->
                    _uiState.value = snapshot.copy(isTransferLoading = false)
                    android.widget.Toast.makeText(
                        getApplication(),
                        error.message ?: if (copy) "复制失败" else "移动失败",
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                },
            )
        }
    }

    private suspend fun deleteWatchLaterAidWithRetry(
        aid: Long,
        csrf: String
    ): Result<Unit> {
        val api = NetworkModule.api
        repeat(WATCH_LATER_DELETE_MAX_ATTEMPTS) { attempt ->
            try {
                val response = api.deleteFromWatchLater(aid = aid, csrf = csrf)
                if (response.code == 0) {
                    return Result.success(Unit)
                }

                val retryable = isRetryableWatchLaterDeleteError(response.code, response.message)
                if (!retryable || attempt >= WATCH_LATER_DELETE_MAX_ATTEMPTS - 1) {
                    return Result.failure(
                        Exception(response.message.ifEmpty { "删除失败: ${response.code}" })
                    )
                }
            } catch (e: Exception) {
                if (attempt >= WATCH_LATER_DELETE_MAX_ATTEMPTS - 1) {
                    return Result.failure(e)
                }
            }

            val backoffMs = WATCH_LATER_DELETE_RETRY_BASE_DELAY_MS * (attempt + 1)
            kotlinx.coroutines.delay(backoffMs)
        }
        return Result.failure(Exception("删除失败，请稍后重试"))
    }
}

/**
 *  稍后再看页面
 */

// ... (existing imports)

/**
 *  稍后再看页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchLaterScreen(
    onBack: () -> Unit,
    onVideoClick: (String, Long, Long) -> Unit,
    onPlayAllAudioClick: ((String, Long, Long) -> Unit)? = null,
    initialSearchQuery: String = "",
    onOpenSearchDestination: ((String) -> Unit)? = null,
    viewModel: WatchLaterViewModel = viewModel(),
    globalHazeState: HazeState? = null // [新增]
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val homeSettings by SettingsManager.getHomeSettings(context).collectAsStateWithLifecycle(initialValue = com.android.purebilibili.core.store.HomeSettings(),
        context = kotlin.coroutines.EmptyCoroutineContext
    )
    val hazeState = rememberRecoverableHazeState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isBatchMode by rememberSaveable { mutableStateOf(false) }
    var selectedBvids by rememberSaveable { mutableStateOf(setOf<String>()) }
    var showBatchDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showManagementMenu by rememberSaveable { mutableStateOf(false) }
    var showBatchMenu by rememberSaveable { mutableStateOf(false) }
    var pendingTransferCopy by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var selectedTransferFolderId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingManagementAction by rememberSaveable { mutableStateOf<WatchLaterManagementAction?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf(initialSearchQuery) }
    val displayedItems = state.items

    LaunchedEffect(searchQuery) {
        kotlinx.coroutines.delay(350)
        viewModel.updateQuery(searchQuery)
    }

    LaunchedEffect(state.items) {
        val valid = state.items.map { it.bvid }.toSet()
        selectedBvids = selectedBvids.filter { it in valid }.toSet()
        if (isBatchMode && state.items.isEmpty()) {
            isBatchMode = false
        }
        if (state.items.isEmpty()) {
            pendingManagementAction = null
            showManagementMenu = false
        }
    }

    AppScaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // 使用 Box 包裹实现毛玻璃背景
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .unifiedBlur(hazeState)
            ) {
                Column {
                AppTopBar(
                    title = resolveWatchLaterTitle(
                        state.totalCount.takeIf { it > 0 } ?: state.items.size
                    ),
                    navigationIcon = {
                        AppIconButton(onClick = onBack) {
                            AppIcon(rememberAppBackIcon(), contentDescription = "返回")
                        }
                    },
                    actions = {
                        onOpenSearchDestination?.let { openSearch ->
                            AppIconButton(onClick = { openSearch(searchQuery) }) {
                                AppIcon(Icons.Rounded.Search, contentDescription = "搜索")
                            }
                        }
                        if (state.items.isNotEmpty()) {
                            if (isBatchMode) {
                                val allSelected = selectedBvids.size == state.items.size
                                AppTextButton(
                                    onClick = {
                                        selectedBvids = if (allSelected) emptySet() else state.items.map { it.bvid }.toSet()
                                    }
                                ) {
                                    AppText(if (allSelected) "取消全选" else "全选")
                                }
                                Box {
                                    AppIconButton(
                                        enabled = selectedBvids.isNotEmpty() && !state.isTransferLoading,
                                        onClick = { showBatchMenu = true },
                                    ) {
                                        AppIcon(Icons.Filled.MoreVert, contentDescription = "批量操作")
                                    }
                                    AppDropdownMenu(
                                        expanded = showBatchMenu,
                                        onDismissRequest = { showBatchMenu = false },
                                    ) {
                                        AppDropdownMenuItem(
                                            text = { AppText("复制到收藏夹") },
                                            onClick = {
                                                showBatchMenu = false
                                                pendingTransferCopy = true
                                                selectedTransferFolderId = null
                                                viewModel.loadFavoriteFolders()
                                            },
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("移动到收藏夹") },
                                            onClick = {
                                                showBatchMenu = false
                                                pendingTransferCopy = false
                                                selectedTransferFolderId = null
                                                viewModel.loadFavoriteFolders()
                                            },
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("删除(${selectedBvids.size})") },
                                            onClick = {
                                                showBatchMenu = false
                                                showBatchDeleteConfirm = true
                                            },
                                        )
                                    }
                                }
                                AppTextButton(
                                    onClick = {
                                        isBatchMode = false
                                        selectedBvids = emptySet()
                                    }
                                ) {
                                    AppText("完成")
                                }
                            } else {
                                AppIconButton(
                                    onClick = {
                                        val externalPlaylist = buildExternalPlaylistFromWatchLater(
                                            items = displayedItems,
                                            clickedBvid = displayedItems.firstOrNull()?.bvid
                                        ) ?: return@AppIconButton

                                        com.android.purebilibili.feature.video.player.PlaylistManager.setExternalPlaylist(
                                            externalPlaylist.playlistItems,
                                            externalPlaylist.startIndex,
                                            source = com.android.purebilibili.feature.video.player.ExternalPlaylistSource.WATCH_LATER
                                        )
                                        com.android.purebilibili.feature.video.player.PlaylistManager
                                            .setPlayMode(com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL)

                                        val item = displayedItems[externalPlaylist.startIndex]
                                        val target = resolveWatchLaterPlaybackTargetOrDefault(
                                            items = displayedItems,
                                            bvid = item.bvid,
                                            fallbackCid = item.cid
                                        )
                                        onVideoClick(target.bvid, target.cid, target.resumePositionMs)
                                    }
                                ) {
                                    AppIcon(
                                        rememberAppPlayIcon(),
                                        contentDescription = "全部播放",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                AppTextButton(
                                    onClick = {
                                        viewModel.updateSortOrder(state.sortOrder.toggled())
                                    }
                                ) {
                                    AppText(
                                        if (state.sortOrder == WatchLaterSortOrder.FORWARD) "最近添加"
                                        else "最早添加"
                                    )
                                }

                                Box {
                                    AppIconButton(
                                        enabled = !state.isManaging,
                                        onClick = { showManagementMenu = true }
                                    ) {
                                        AppIcon(
                                            imageVector = Icons.Filled.MoreVert,
                                            contentDescription = "更多管理",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    AppDropdownMenu(
                                        expanded = showManagementMenu,
                                        onDismissRequest = { showManagementMenu = false }
                                    ) {
                                        AppDropdownMenuItem(
                                            text = { AppText("全部听") },
                                            enabled = !state.isManaging,
                                            onClick = {
                                                showManagementMenu = false
                                                val externalPlaylist = buildExternalPlaylistFromWatchLater(
                                                    items = displayedItems,
                                                    clickedBvid = displayedItems.firstOrNull()?.bvid
                                                )
                                                if (externalPlaylist != null) {
                                                    com.android.purebilibili.feature.video.player.PlaylistManager.setExternalPlaylist(
                                                        externalPlaylist.playlistItems,
                                                        externalPlaylist.startIndex,
                                                        source = com.android.purebilibili.feature.video.player.ExternalPlaylistSource.WATCH_LATER
                                                    )
                                                    com.android.purebilibili.feature.video.player.PlaylistManager
                                                        .setPlayMode(com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL)

                                                    resolveWatchLaterPlayAllStartTarget(displayedItems)?.let { target ->
                                                        val playbackTarget = resolveWatchLaterPlaybackTargetOrDefault(
                                                            items = displayedItems,
                                                            bvid = target.first,
                                                            fallbackCid = target.second
                                                        )
                                                        onPlayAllAudioClick?.invoke(
                                                            playbackTarget.bvid,
                                                            playbackTarget.cid,
                                                            playbackTarget.resumePositionMs
                                                        ) ?: onVideoClick(
                                                            playbackTarget.bvid,
                                                            playbackTarget.cid,
                                                            playbackTarget.resumePositionMs
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("批量删除") },
                                            enabled = !state.isManaging,
                                            onClick = {
                                                showManagementMenu = false
                                                isBatchMode = true
                                                selectedBvids = emptySet()
                                            }
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("清除失效") },
                                            enabled = !state.isManaging,
                                            onClick = {
                                                showManagementMenu = false
                                                pendingManagementAction = WatchLaterManagementAction.CLEAR_INVALID
                                            }
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("清空已看") },
                                            enabled = !state.isManaging,
                                            onClick = {
                                                showManagementMenu = false
                                                pendingManagementAction = WatchLaterManagementAction.CLEAR_VIEWED
                                            }
                                        )
                                        AppDropdownMenuItem(
                                            text = { AppText("清空全部") },
                                            enabled = !state.isManaging,
                                            onClick = {
                                                showManagementMenu = false
                                                pendingManagementAction = WatchLaterManagementAction.CLEAR_ALL
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        scrolledContainerColor = Color.Transparent
                    ),
                    scrollBehavior = scrollBehavior
                )
                AppSearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "搜索稍后再看",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacingTokens.Medium),
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacingTokens.Medium),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
                ) {
                    WatchLaterFilter.entries.forEach { filter ->
                        AppFilterChip(
                            selected = state.filter == filter,
                            enabled = !isBatchMode,
                            onClick = { viewModel.selectFilter(filter) },
                            label = {
                                AppText(
                                    if (filter == state.filter) "${filter.label}(${state.totalCount})"
                                    else filter.label
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                }
                
                // 分割线 (仅在滚动时显示? 这里简化一直显示细线或跟随滚动)
                // 暂时不加显式分割线，依靠毛玻璃效果
            }
        },
        containerColor = AppSurfaceTokens.groupedListContainer()
    ) { padding ->
        val bottomContentPadding = LocalBottomBarContentPadding.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSourceCompat(state = hazeState) // 内容作为模糊源（全局源由根层提供）
        ) {
            when {
                state.isLoading -> {
                    com.android.purebilibili.core.ui.skeleton.ContentMediaListSkeleton(
                        modifier = Modifier.fillMaxSize(),
                        itemCount = 8,
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppText(
                            text = state.error ?: "未知错误",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                        AppOutlinedButton(onClick = { viewModel.loadData() }) {
                            AppText("重试")
                        }
                    }
                }
                state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIcon(
                            rememberAppWatchLaterIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Large),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                        AppText(
                            text = if (searchQuery.isBlank()) "稍后再看列表为空" else "未找到相关视频",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(resolveWatchLaterColumnCount(maxWidth.value)),
                        contentPadding = PaddingValues(
                            start = AppSpacingTokens.Medium,
                            end = AppSpacingTokens.Medium,
                            top = padding.calculateTopPadding() + AppSpacingTokens.Small,
                            bottom = bottomContentPadding,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
                        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(
                            items = displayedItems,
                            key = { index, item ->
                                resolveIndexedVideoLazyKey(
                                    namespace = "watch_later_video",
                                    index = index,
                                    bvid = item.bvid,
                                    id = item.id,
                                    aid = item.aid,
                                    cid = item.cid
                                )
                            }
                        ) { index, item ->
                            if (index == displayedItems.lastIndex && state.hasMore && !state.isLoadingMore) {
                                LaunchedEffect(state.page, displayedItems.size) { viewModel.loadMore() }
                            }
                            val isDissolving = item.bvid in state.dissolvingIds
                            val isSelected = item.bvid in selectedBvids

                            MaybeDissolvableVideoCard(
                                isDissolving = isDissolving,
                                onDissolveComplete = { viewModel.completeVideoDissolve(item.bvid) },
                                cardId = item.bvid,
                                preset = DissolveAnimationPreset.TELEGRAM_FAST,
                                modifier = Modifier.jiggleOnDissolve(
                                    cardId = item.bvid,
                                    isCurrentCardDissolving = isDissolving
                                )
                            ) {
                                WatchLaterVideoCard(
                                    item = item,
                                    isBatchMode = isBatchMode,
                                    isSelected = isSelected,
                                    transitionEnabled = homeSettings.cardTransitionEnabled,
                                    onDelete = { viewModel.startVideoDissolve(item.bvid) },
                                    onLongClick = {
                                        isBatchMode = true
                                        selectedBvids = selectedBvids + item.bvid
                                    },
                                    onClick = {
                                        if (isBatchMode) {
                                            selectedBvids = if (item.bvid in selectedBvids) {
                                                selectedBvids - item.bvid
                                            } else {
                                                selectedBvids + item.bvid
                                            }
                                        } else {
                                            val externalPlaylist = buildExternalPlaylistFromWatchLater(
                                                items = displayedItems,
                                                clickedBvid = item.bvid
                                            )
                                            if (externalPlaylist != null) {
                                                com.android.purebilibili.feature.video.player.PlaylistManager.setExternalPlaylist(
                                                    externalPlaylist.playlistItems,
                                                    externalPlaylist.startIndex,
                                                    source = com.android.purebilibili.feature.video.player.ExternalPlaylistSource.WATCH_LATER
                                                )
                                                com.android.purebilibili.feature.video.player.PlaylistManager
                                                    .setPlayMode(com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL)
                                            }

                                            val target = resolveWatchLaterPlaybackTargetOrDefault(
                                                items = displayedItems,
                                                bvid = item.bvid,
                                                fallbackCid = item.cid
                                            )
                                            onVideoClick(target.bvid, target.cid, target.resumePositionMs)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    if (showBatchDeleteConfirm) {
        AppAlertDialog(
            onDismissRequest = { showBatchDeleteConfirm = false },
            title = { AppText("批量删除") },
            text = { AppText("确认删除已选择的 ${selectedBvids.size} 个视频吗？") },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        val aidList = state.items
                            .filter { it.bvid in selectedBvids }
                            .map { it.id }
                        viewModel.deleteItems(aidList)
                        selectedBvids = emptySet()
                        isBatchMode = false
                        showBatchDeleteConfirm = false
                    }
                ) {
                    AppText("删除")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { showBatchDeleteConfirm = false }) {
                    AppText("取消")
                }
            }
        )
    }

    pendingTransferCopy?.let { copy ->
        AppAlertDialog(
            onDismissRequest = { pendingTransferCopy = null },
            title = { AppText(if (copy) "复制到收藏夹" else "移动到收藏夹") },
            text = {
                if (state.isTransferLoading && state.favoriteFolders.isEmpty()) {
                    AppText("正在加载收藏夹…")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                        items(state.favoriteFolders, key = { it.id }) { folder ->
                            AppSurface(
                                onClick = { selectedTransferFolderId = folder.id },
                                modifier = Modifier.fillMaxWidth(),
                                color = if (selectedTransferFolderId == folder.id) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else Color.Transparent,
                            ) {
                                AppText(
                                    folder.title,
                                    modifier = Modifier.padding(AppSpacingTokens.Medium),
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                AppTextButton(
                    enabled = selectedTransferFolderId != null && !state.isTransferLoading,
                    onClick = {
                        val aids = state.items
                            .filter { it.bvid in selectedBvids }
                            .map { it.aid }
                            .filter { it > 0L }
                            .toSet()
                        selectedTransferFolderId?.let { folderId ->
                            viewModel.copyOrMoveToFavorite(aids, folderId, copy)
                        }
                        pendingTransferCopy = null
                        selectedBvids = emptySet()
                        isBatchMode = false
                    },
                ) { AppText("确认") }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingTransferCopy = null }) { AppText("取消") }
            },
        )
    }

    pendingManagementAction?.let { action ->
        val affectedCount = remember(action, state.items) {
            state.items.size - resolveWatchLaterItemsAfterManagementAction(
                items = state.items,
                action = action
            ).size
        }
        AppAlertDialog(
            onDismissRequest = { pendingManagementAction = null },
            title = {
                AppText(
                    when (action) {
                        WatchLaterManagementAction.CLEAR_INVALID -> "清除失效"
                        WatchLaterManagementAction.CLEAR_VIEWED -> "清空已看"
                        WatchLaterManagementAction.CLEAR_ALL -> "清空全部"
                    }
                )
            },
            text = { AppText(resolveWatchLaterManagementConfirmText(action, affectedCount)) },
            confirmButton = {
                AppTextButton(
                    onClick = {
                        viewModel.runManagementAction(action)
                        pendingManagementAction = null
                    }
                ) {
                    AppText("确认")
                }
            },
            dismissButton = {
                AppTextButton(onClick = { pendingManagementAction = null }) {
                    AppText("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun WatchLaterVideoCard(
    item: VideoItem,
    isBatchMode: Boolean,
    isSelected: Boolean,
    transitionEnabled: Boolean,
    onDelete: () -> Unit,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = remember(configuration.screenWidthDp, density) {
        with(density) { configuration.screenWidthDp.dp.toPx() }
    }
    val screenHeightPx = remember(configuration.screenHeightDp, density) {
        with(density) { configuration.screenHeightDp.dp.toPx() }
    }
    val cardBoundsRef = remember { object { var value: androidx.compose.ui.geometry.Rect? = null } }
    val sourceRoute = LocalVideoCardSharedElementSourceRoute.current
    val sharedTransitionSpeedSettings = LocalVideoSharedTransitionSpeedSettings.current
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sharedElementReady = transitionEnabled &&
        item.bvid.isNotBlank() &&
        sourceRoute != null &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    val sharedTransitionMotionSpec = remember(sourceRoute, transitionEnabled, sharedTransitionSpeedSettings) {
        resolveVideoCardSharedTransitionMotionSpec(
            sourceRoute = sourceRoute,
            transitionEnabled = transitionEnabled,
            speedSettings = sharedTransitionSpeedSettings
        )
    }
    val cardClick = {
        if (!isBatchMode) {
            cardBoundsRef.value?.let { bounds ->
                CardPositionManager.recordVideoCardPosition(
                    bvid = item.bvid,
                    sourceRoute = sourceRoute,
                    bounds = bounds,
                    screenWidth = screenWidthPx,
                    screenHeight = screenHeightPx,
                    sourceCornerDp = 8
                )
            }
        }
        onClick()
    }
    val useCardShellSharedBounds = shouldUseVideoCardShellSharedBounds(
        sourceRoute = sourceRoute,
        transitionEnabled = sharedElementReady
    )
    val cardShellShape = AppShapes.container(ContainerLevel.Card)

    PersonalMediaCardFrame(
        selected = isSelected,
        modifier = Modifier
            .videoCardShellSharedBoundsOrEmpty(
                enabled = useCardShellSharedBounds,
                sharedTransitionScope = sharedTransitionScope,
                animatedVisibilityScope = animatedVisibilityScope,
                bvid = item.bvid,
                sourceRoute = sourceRoute,
                motionSpec = sharedTransitionMotionSpec,
                clipShape = cardShellShape,
                // 让播放器接管移动中的整卡，返回时再在 morph 末段接回源卡，避免与播放器重叠。
                crossfadeSourceContent = true,
            )
            .height(IntrinsicSize.Min)
            .onGloballyPositioned { coordinates ->
                cardBoundsRef.value = coordinates.boundsInRoot()
            },
        headlineContent = {
            AppText(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        overlineContent = item.contentType.takeIf(String::isNotBlank)?.let { badges ->
            {
                AppText(
                    badges,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        supportingContent = {
            Column(
                modifier = Modifier.videoCardShellReturnChromeAlpha(
                    enabled = useCardShellSharedBounds,
                    bvid = item.bvid,
                    sourceRoute = sourceRoute,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
            ) {
                AppText(
                    text = item.owner.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                AppText(
                    text = "${formatNumber(item.stat.view)}播放 · ${formatNumber(item.stat.danmaku)}弹幕",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                )
            }
        },
        coverContent = {
            AsyncImage(
                model = fixCoverUrl(item.pic),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        },
        coverOverlayContent = {
            val watched = item.duration > 0 && item.progress >= item.duration
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacingTokens.ExtraSmall)
                    .background(MediaContrastPalette.Scrim.copy(alpha = 0.7f), AppShapes.container(ContainerLevel.Tag))
                    .padding(horizontal = AppSpacingTokens.ExtraSmall, vertical = AppSpacingTokens.Micro)
            ) {
                AppText(
                    text = if (watched) "已看完" else formatDuration(item.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MediaContrastPalette.Foreground,
                )
            }
            if (item.duration > 0 && item.progress > 0) {
                LinearProgressIndicator(
                    progress = { (item.progress.toFloat() / item.duration).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent,
                )
            }
        },
        trailingContent = {
            if (isBatchMode) {
                AppIcon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = if (isSelected) "已选择" else "未选择",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(AppSpacingTokens.ExtraLarge),
                )
            } else {
                AppIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(AppChromeSizeTokens.MinimumTouchTarget),
                ) {
                    AppIcon(Icons.Rounded.Close, contentDescription = "删除")
                }
            }
        },
        onClick = cardClick,
        onLongClick = onLongClick,
    )
}
