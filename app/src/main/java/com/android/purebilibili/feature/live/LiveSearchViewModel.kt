// 文件路径: feature/live/LiveSearchViewModel.kt
package com.android.purebilibili.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.data.model.response.LiveRoomSearchItem
import com.android.purebilibili.data.model.response.SearchUpItem
import com.android.purebilibili.data.repository.SearchLiveOrder
import com.android.purebilibili.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveSearchUiState(
    val hasSubmitted: Boolean = false,
    val isLoading: Boolean = false,
    val liveLoadingMore: Boolean = false,
    val userLoadingMore: Boolean = false,
    val liveHasMore: Boolean = false,
    val userHasMore: Boolean = false,
    val liveNextPage: Int = 1,
    val userNextPage: Int = 1,
    val activeKeyword: String = "",
    val error: String? = null,
    val liveResults: List<LiveRoomSearchItem> = emptyList(),
    val userResults: List<SearchUpItem> = emptyList(),
)

class LiveSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveSearchUiState())
    val uiState = _uiState.asStateFlow()

    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            hasSubmitted = false,
            error = null,
            liveResults = emptyList(),
            userResults = emptyList(),
        )
    }

    fun submit(keyword: String) {
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                hasSubmitted = true,
                isLoading = true,
                error = null,
                activeKeyword = normalized,
                liveResults = emptyList(),
                userResults = emptyList(),
                liveHasMore = false,
                userHasMore = false,
                liveNextPage = 1,
                userNextPage = 1,
            )
            SearchRepository.searchLive(normalized, 1, SearchLiveOrder.ONLINE)
                .onSuccess { (rooms, pageInfo) ->
                    _uiState.value = _uiState.value.copy(
                        liveResults = rooms.distinctBy { it.roomid },
                        liveHasMore = pageInfo.hasMore,
                        liveNextPage = pageInfo.currentPage + 1,
                    )
                }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message ?: "直播搜索失败") }
            SearchRepository.searchUp(normalized, 1)
                .onSuccess { (ups, pageInfo) ->
                    _uiState.value = _uiState.value.copy(
                        userResults = ups.distinctBy { it.mid },
                        userHasMore = pageInfo.hasMore,
                        userNextPage = pageInfo.currentPage + 1,
                    )
                }
                .onFailure {
                    val current = _uiState.value
                    if (current.error == null) {
                        _uiState.value = current.copy(error = it.message ?: "主播搜索失败")
                    }
                }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun loadMoreLive() {
        val state = _uiState.value
        if (state.activeKeyword.isBlank() || state.liveLoadingMore || !state.liveHasMore) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(liveLoadingMore = true)
            SearchRepository.searchLive(state.activeKeyword, state.liveNextPage, SearchLiveOrder.ONLINE)
                .onSuccess { (rooms, pageInfo) ->
                    val currentIds = _uiState.value.liveResults.map { it.roomid }.toSet()
                    _uiState.value = _uiState.value.copy(
                        liveResults = _uiState.value.liveResults + rooms.filterNot { it.roomid in currentIds },
                        liveHasMore = pageInfo.hasMore,
                        liveNextPage = pageInfo.currentPage + 1,
                        liveLoadingMore = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "直播加载更多失败",
                        liveLoadingMore = false,
                    )
                }
        }
    }

    fun loadMoreUser() {
        val state = _uiState.value
        if (state.activeKeyword.isBlank() || state.userLoadingMore || !state.userHasMore) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(userLoadingMore = true)
            SearchRepository.searchUp(state.activeKeyword, state.userNextPage)
                .onSuccess { (ups, pageInfo) ->
                    val currentIds = _uiState.value.userResults.map { it.mid }.toSet()
                    _uiState.value = _uiState.value.copy(
                        userResults = _uiState.value.userResults + ups.filterNot { it.mid in currentIds },
                        userHasMore = pageInfo.hasMore,
                        userNextPage = pageInfo.currentPage + 1,
                        userLoadingMore = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "主播加载更多失败",
                        userLoadingMore = false,
                    )
                }
        }
    }
}
