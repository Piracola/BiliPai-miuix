// 文件路径: feature/live/LiveAreaDetailViewModel.kt
package com.android.purebilibili.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.data.model.response.LiveAreaChild
import com.android.purebilibili.data.model.response.LiveRoom
import com.android.purebilibili.data.repository.LiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveAreaDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val rooms: List<LiveRoom> = emptyList(),
    val siblings: List<LiveAreaChild> = emptyList(),
    val sortType: String = "online",
    val page: Int = 1,
    val hasMore: Boolean = false,
    val totalCount: Int = 0,
    val isLoadingMore: Boolean = false,
)

class LiveAreaDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveAreaDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun load(parentAreaId: Int, areaId: Int, title: String, reset: Boolean) {
        val state = _uiState.value
        if (!reset && (state.isLoadingMore || !state.hasMore)) return
        viewModelScope.launch {
            if (reset) {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    page = 1,
                    rooms = emptyList(),
                    hasMore = false,
                    totalCount = 0,
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }
            val nextPage = if (reset) 1 else _uiState.value.page + 1
            LiveRepository.getAreaRoomsPage(
                parentAreaId = parentAreaId,
                areaId = areaId,
                page = nextPage,
                sortType = _uiState.value.sortType,
                areaTitle = title,
            ).onSuccess { result ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    rooms = if (reset) result.rooms else current.rooms + result.rooms,
                    page = nextPage,
                    hasMore = result.hasMore,
                    totalCount = result.totalCount,
                    isLoading = false,
                    isLoadingMore = false,
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    error = it.message ?: "加载分区直播失败",
                    isLoading = false,
                    isLoadingMore = false,
                )
            }
        }
    }

    fun loadSiblings(parentAreaId: Int) {
        viewModelScope.launch {
            LiveRepository.getLiveAreaIndex().onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    siblings = list.firstOrNull { it.id == parentAreaId }?.list.orEmpty()
                )
            }
        }
    }

    fun selectSortType(value: String) {
        if (value == _uiState.value.sortType) return
        _uiState.value = _uiState.value.copy(sortType = value)
    }
}
