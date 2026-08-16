// 文件路径: feature/live/LiveFollowingViewModel.kt
package com.android.purebilibili.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.data.model.response.LiveRoom
import com.android.purebilibili.data.repository.LiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveFollowingUiState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasMore: Boolean = false,
    val nextPage: Int = 1,
    val error: String? = null,
    val items: List<LiveRoom> = emptyList(),
)

class LiveFollowingViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveFollowingUiState())
    val uiState = _uiState.asStateFlow()

    private fun mergeRooms(current: List<LiveRoom>, next: List<LiveRoom>, refresh: Boolean): List<LiveRoom> {
        return if (refresh) {
            next.distinctBy { it.roomid }
        } else {
            (current + next).distinctBy { it.roomid }
        }
    }

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            LiveRepository.getFollowedLivePage(page = 1)
                .onSuccess { page ->
                    _uiState.value = _uiState.value.copy(
                        items = mergeRooms(emptyList(), page.items, refresh = true),
                        hasMore = page.hasMore,
                        nextPage = page.nextPage,
                        isLoading = false,
                        error = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "加载关注直播失败",
                        isLoading = false,
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            LiveRepository.getFollowedLivePage(page = 1)
                .onSuccess { page ->
                    _uiState.value = _uiState.value.copy(
                        items = mergeRooms(emptyList(), page.items, refresh = true),
                        hasMore = page.hasMore,
                        nextPage = page.nextPage,
                        isRefreshing = false,
                        isLoading = false,
                        error = null,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        error = it.message ?: "加载关注直播失败",
                        isRefreshing = false,
                        isLoading = false,
                    )
                }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoadingMore || state.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMore = true)
            LiveRepository.getFollowedLivePage(page = state.nextPage)
                .onSuccess { page ->
                    _uiState.value = _uiState.value.copy(
                        items = mergeRooms(state.items, page.items, refresh = false),
                        hasMore = page.hasMore,
                        nextPage = page.nextPage,
                        isLoadingMore = false,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoadingMore = false)
                }
        }
    }
}
