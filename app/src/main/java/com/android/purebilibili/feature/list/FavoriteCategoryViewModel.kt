// 文件路径: feature/list/FavoriteCategoryViewModel.kt
package com.android.purebilibili.feature.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.data.model.response.FavoritePgcStatus
import com.android.purebilibili.data.model.response.FavoriteSection
import com.android.purebilibili.data.repository.BangumiRepository
import com.android.purebilibili.data.repository.FavoriteCategoryItem
import com.android.purebilibili.data.repository.PersonalFavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteCategoryUiState(
    val section: FavoriteSection = FavoriteSection.BANGUMI,
    val items: List<FavoriteCategoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false,
    val pgcStatus: FavoritePgcStatus = FavoritePgcStatus.WANT,
    val publishedNotes: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
)

class FavoriteCategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(FavoriteCategoryUiState())
    val uiState = _uiState.asStateFlow()

    fun selectSection(section: FavoriteSection) {
        if (_uiState.value.section == section && _uiState.value.items.isNotEmpty()) return
        _uiState.value = FavoriteCategoryUiState(
            section = section,
            pgcStatus = _uiState.value.pgcStatus,
            publishedNotes = _uiState.value.publishedNotes,
            isLoading = true,
        )
        load(reset = true)
    }

    fun selectPgcStatus(status: FavoritePgcStatus) {
        if (_uiState.value.pgcStatus == status) return
        _uiState.update { it.copy(pgcStatus = status, selectedIds = emptySet()) }
        load(reset = true)
    }

    fun selectPublishedNotes(published: Boolean) {
        if (_uiState.value.publishedNotes == published) return
        _uiState.update { it.copy(publishedNotes = published, selectedIds = emptySet()) }
        load(reset = true)
    }

    fun retry() = load(reset = true)

    fun loadMore() {
        val state = _uiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        load(reset = false)
    }

    fun toggleSelection(id: Long) {
        if (id <= 0L) return
        _uiState.update { state ->
            state.copy(selectedIds = if (id in state.selectedIds) state.selectedIds - id else state.selectedIds + id)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedIds = emptySet()) }
    }

    fun remove(item: FavoriteCategoryItem) {
        viewModelScope.launch {
            val state = _uiState.value
            val result = when (item.section) {
                FavoriteSection.BANGUMI, FavoriteSection.CINEMA ->
                    BangumiRepository.unfollowBangumi(item.id).map { Unit }
                else -> PersonalFavoriteRepository.remove(item, state.publishedNotes)
            }
            if (result.isSuccess) {
                _uiState.update { it.copy(items = it.items.filterNot { candidate -> candidate.id == item.id }) }
            } else {
                _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "移除失败") }
            }
        }
    }

    fun removeSelected() {
        val state = _uiState.value
        if (state.selectedIds.isEmpty()) return
        viewModelScope.launch {
            val selected = state.items.filter { it.id in state.selectedIds }
            val failures = selected.map { item ->
                when (item.section) {
                    FavoriteSection.BANGUMI, FavoriteSection.CINEMA ->
                        BangumiRepository.unfollowBangumi(item.id).map { Unit }
                    else -> PersonalFavoriteRepository.remove(item, state.publishedNotes)
                }
            }.count { it.isFailure }
            if (failures == 0) {
                _uiState.update {
                    it.copy(
                        items = it.items.filterNot { item -> item.id in state.selectedIds },
                        selectedIds = emptySet(),
                    )
                }
            } else {
                _uiState.update { it.copy(error = "部分内容移除失败：${selected.size - failures}/${selected.size}") }
            }
        }
    }

    fun updateSelectedPgcStatus(status: FavoritePgcStatus) {
        val state = _uiState.value
        if (
            state.section !in setOf(FavoriteSection.BANGUMI, FavoriteSection.CINEMA) ||
            state.selectedIds.isEmpty()
        ) return
        viewModelScope.launch {
            val results = state.selectedIds.map { seasonId ->
                BangumiRepository.updateBangumiFollowStatus(seasonId, status.value)
            }
            if (results.all { it.isSuccess }) {
                _uiState.update { it.copy(selectedIds = emptySet()) }
                load(reset = true)
            } else {
                _uiState.update { it.copy(error = "部分状态迁移失败，请重试") }
            }
        }
    }

    fun updatePgcStatus(item: FavoriteCategoryItem, status: FavoritePgcStatus) {
        viewModelScope.launch {
            val result = BangumiRepository.updateBangumiFollowStatus(item.id, status.value)
            if (result.isSuccess) load(reset = true)
            else _uiState.update { it.copy(error = result.exceptionOrNull()?.message ?: "更新状态失败") }
        }
    }

    private fun load(reset: Boolean) {
        val requestState = _uiState.value
        val requestPage = if (reset) 1 else requestState.page + 1
        _uiState.update {
            if (reset) it.copy(isLoading = true, error = null, page = 1, hasMore = false)
            else it.copy(isLoadingMore = true, error = null)
        }
        viewModelScope.launch {
            val result = when (requestState.section) {
                FavoriteSection.BANGUMI, FavoriteSection.CINEMA -> loadPgc(
                    section = requestState.section,
                    status = requestState.pgcStatus,
                    page = requestPage,
                )
                FavoriteSection.ARTICLE -> PersonalFavoriteRepository.getArticles(requestPage)
                FavoriteSection.NOTE -> PersonalFavoriteRepository.getNotes(requestPage, requestState.publishedNotes)
                FavoriteSection.TOPIC -> PersonalFavoriteRepository.getTopics(requestPage)
                FavoriteSection.COURSE -> PersonalFavoriteRepository.getCourses(requestPage)
                FavoriteSection.VIDEO -> Result.success(
                    com.android.purebilibili.data.repository.FavoriteCategoryPage(emptyList(), false)
                )
            }
            if (_uiState.value.section != requestState.section) return@launch
            result.fold(
                onSuccess = { page ->
                    _uiState.update { current ->
                        current.copy(
                            items = if (reset) page.items else (current.items + page.items).distinctBy { it.id },
                            isLoading = false,
                            isLoadingMore = false,
                            page = requestPage,
                            hasMore = page.hasMore,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = error.message ?: "加载失败",
                        )
                    }
                },
            )
        }
    }

    private suspend fun loadPgc(
        section: FavoriteSection,
        status: FavoritePgcStatus,
        page: Int,
    ): Result<com.android.purebilibili.data.repository.FavoriteCategoryPage> {
        val pageSize = 30
        return BangumiRepository.getMyFollowBangumi(
            type = if (section == FavoriteSection.CINEMA) 2 else 1,
            page = page,
            pageSize = pageSize,
        ).map { data ->
            val items = data.list.orEmpty()
                .filter { it.followStatus == status.value || it.followStatus == 0 }
                .map { pgc ->
                    FavoriteCategoryItem(
                        id = pgc.seasonId,
                        title = pgc.title,
                        subtitle = pgc.progress.ifBlank { pgc.evaluate },
                        cover = pgc.cover,
                        url = pgc.url,
                        badge = listOf(pgc.badge, pgc.newEp?.indexShow.orEmpty())
                            .filter(String::isNotBlank)
                            .joinToString(" · "),
                        section = section,
                    )
                }
            com.android.purebilibili.data.repository.FavoriteCategoryPage(
                items = items,
                hasMore = page * pageSize < data.total,
            )
        }
    }
}
