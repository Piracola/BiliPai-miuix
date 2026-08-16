// 文件路径: feature/watchlater/WatchLaterViewModel.kt
package com.android.purebilibili.feature.watchlater

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.coroutines.AppScope
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.refresh.WatchLaterRefreshBus
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.store.TokenManager
import com.android.purebilibili.data.model.response.FavFolder
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.data.repository.FavoriteRepository
import com.android.purebilibili.data.repository.WatchLaterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WatchLaterViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(WatchLaterUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    // 阶段 4 切片：稍后再看页设置直读收拢到 VM（UI 层不得直接访问 SettingsManager）
    val homeSettings: StateFlow<HomeSettings> = SettingsManager.getHomeSettings(getApplication())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeSettings(),
        )

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
