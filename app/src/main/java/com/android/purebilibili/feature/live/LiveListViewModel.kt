// 文件路径: feature/live/LiveListViewModel.kt
package com.android.purebilibili.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.data.model.response.LiveAreaParent
import com.android.purebilibili.data.repository.LiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveRoomItem(
    val roomId: Long,
    val title: String,
    val cover: String,
    val systemCover: String = "",
    val uname: String,
    val face: String,
    val online: Int,
    val areaName: String,
    val liveStatus: Int = 1,
) {
    fun resolvedCover(preferFirstFrame: Boolean): String {
        return if (preferFirstFrame) {
            listOf(systemCover, cover, face).firstOrNull { it.isNotBlank() }.orEmpty()
        } else {
            listOf(cover, systemCover, face).firstOrNull { it.isNotBlank() }.orEmpty()
        }
    }
}

data class LiveListUiState(
    val contentItems: List<LiveRoomItem> = emptyList(),
    val followItems: List<LiveRoomItem> = emptyList(),
    val areaEntries: List<com.android.purebilibili.data.model.response.LiveFeedAreaEntry> = emptyList(),
    val areaList: List<LiveAreaParent> = emptyList(),
    /** 0 = 推荐；其余对应 [areaEntries] 下标 + 1 */
    val selectedAreaIndex: Int = 0,
    val selectedParentAreaId: Int = 0,
    val selectedAreaId: Int = 0,
    val sortTags: List<com.android.purebilibili.data.model.response.LiveSecondSortTag> = emptyList(),
    val selectedSortType: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val page: Int = 1,
    val showFirstFrame: Boolean = false,
    val error: String? = null,
    val livingCount: Int = 0,
)
class LiveListViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveListUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null, page = 1, hasMore = true)
            if (state.selectedAreaIndex == 0) {
                loadRecommendPage(page = 1, append = false)
            } else {
                loadAreaPage(page = 1, append = false)
            }
            // 分区详情子标签仍用 web area list 兜底
            if (_uiState.value.areaList.isEmpty()) {
                runCatching {
                    val response = NetworkModule.api.getLiveAreaList()
                    if (response.code == 0 && response.data != null) {
                        _uiState.value = _uiState.value.copy(areaList = response.data)
                    }
                }
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) return
        viewModelScope.launch {
            val next = state.page + 1
            _uiState.value = state.copy(isLoadingMore = true)
            if (state.selectedAreaIndex == 0) {
                loadRecommendPage(page = next, append = true)
            } else {
                loadAreaPage(page = next, append = true)
            }
        }
    }

    fun selectHomeArea(index: Int) {
        val state = _uiState.value
        if (index == state.selectedAreaIndex) return
        if (index <= 0) {
            _uiState.value = state.copy(
                selectedAreaIndex = 0,
                selectedParentAreaId = 0,
                selectedAreaId = 0,
                selectedSortType = null,
                sortTags = emptyList(),
                page = 1,
                hasMore = true,
                isLoading = true,
                error = null,
            )
            viewModelScope.launch { loadRecommendPage(page = 1, append = false) }
            return
        }
        val entry = resolveLiveHomeAreaEntries(
            feedEntries = state.areaEntries,
            areaParents = state.areaList
        ).getOrNull(index - 1) ?: return
        _uiState.value = state.copy(
            selectedAreaIndex = index,
            selectedParentAreaId = entry.parentAreaId,
            selectedAreaId = entry.areaId,
            selectedSortType = null,
            sortTags = emptyList(),
            page = 1,
            hasMore = true,
            isLoading = true,
            error = null,
        )
        viewModelScope.launch { loadAreaPage(page = 1, append = false) }
    }

    fun selectSortTag(sortType: String?) {
        val state = _uiState.value
        if (state.selectedAreaIndex == 0) return
        if (state.selectedSortType == sortType) return
        _uiState.value = state.copy(
            selectedSortType = sortType,
            page = 1,
            hasMore = true,
            isLoading = true,
            error = null,
        )
        viewModelScope.launch { loadAreaPage(page = 1, append = false) }
    }

    fun toggleShowFirstFrame() {
        _uiState.value = _uiState.value.copy(showFirstFrame = !_uiState.value.showFirstFrame)
    }

    private suspend fun loadRecommendPage(page: Int, append: Boolean) {
        LiveRepository.getLiveFeedHome(page = page).fold(
            onSuccess = { snapshot ->
                val mapped = snapshot.rooms.map { it.toLiveRoomItem() }
                val followMapped = snapshot.followRooms.map { it.toLiveRoomItem() }
                val current = _uiState.value
                val mergedRooms = if (append) {
                    (current.contentItems + mapped).distinctBy { it.roomId }
                } else {
                    mapped
                }
                val areaEntries = when {
                    snapshot.areaEntries.isNotEmpty() -> snapshot.areaEntries
                    !append -> current.areaEntries
                    else -> current.areaEntries
                }.ifEmpty {
                    current.areaList.map {
                        com.android.purebilibili.data.model.response.LiveFeedAreaEntry(
                            title = it.name,
                            areaId = 0,
                            parentAreaId = it.id,
                        )
                    }
                }
                _uiState.value = current.copy(
                    contentItems = mergedRooms,
                    followItems = if (followMapped.isNotEmpty()) followMapped else if (!append) emptyList() else current.followItems,
                    livingCount = if (followMapped.isNotEmpty()) followMapped.size else current.livingCount,
                    areaEntries = areaEntries,
                    page = page,
                    hasMore = snapshot.hasMore,
                    isLoading = false,
                    isLoadingMore = false,
                    error = if (mergedRooms.isEmpty() && !append) "暂无直播" else null,
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = if (!append) error.message ?: "加载失败" else _uiState.value.error,
                )
            }
        )
    }

    private suspend fun loadAreaPage(page: Int, append: Boolean) {
        val state = _uiState.value
        val query = resolveLiveAreaRoomQuery(
            parentAreaId = state.selectedParentAreaId,
            areaId = state.selectedAreaId
        ) ?: run {
            _uiState.value = state.copy(
                isLoading = false,
                isLoadingMore = false,
                error = "无效的直播分区"
            )
            return
        }
        LiveRepository.getLiveSecondHome(
            parentAreaId = query.parentAreaId,
            areaId = query.areaId,
            page = page,
            sortType = state.selectedSortType,
        ).fold(
            onSuccess = { snapshot ->
                val mapped = snapshot.rooms.map { it.toLiveRoomItem() }
                val current = _uiState.value
                val merged = if (append) {
                    (current.contentItems + mapped).distinctBy { it.roomId }
                } else {
                    mapped
                }
                _uiState.value = current.copy(
                    contentItems = merged,
                    sortTags = if (snapshot.sortTags.isNotEmpty()) snapshot.sortTags else current.sortTags,
                    page = page,
                    hasMore = snapshot.hasMore,
                    isLoading = false,
                    isLoadingMore = false,
                    error = if (merged.isEmpty() && !append) "暂无直播内容" else null,
                )
            },
            onFailure = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    error = if (!append) error.message ?: "加载失败" else _uiState.value.error,
                )
            }
        )
    }
}
