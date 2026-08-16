// 文件路径: feature/live/LiveAreaViewModel.kt
package com.android.purebilibili.feature.live

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.data.model.response.LiveAreaParent
import com.android.purebilibili.data.model.response.LiveFavoriteTagEntry
import com.android.purebilibili.data.repository.LiveRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LiveAreaViewModel(application: Application) : AndroidViewModel(application) {

    // 阶段 4 切片：直播标签页设置直读收拢到 VM（UI 层不得直接访问 SettingsManager）
    val favoriteTags: StateFlow<List<LiveFavoriteTagEntry>> =
        SettingsManager.getLiveFavoriteTags(getApplication())
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList(),
            )

    private val _areas = MutableStateFlow<List<LiveAreaParent>>(emptyList())
    val areas: StateFlow<List<LiveAreaParent>> = _areas.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _reloadKey = MutableStateFlow(0)
    val reloadKey: StateFlow<Int> = _reloadKey.asStateFlow()

    init {
        loadAreas()
    }

    fun loadAreas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            LiveRepository.getLiveAreaIndex()
                .onSuccess {
                    _areas.value = it
                    _isLoading.value = false
                }
                .onFailure {
                    _error.value = it.message ?: "加载标签失败"
                    _isLoading.value = false
                }
        }
    }

    fun reload() {
        _reloadKey.value += 1
        loadAreas()
    }

    fun removeFavoriteTag(entry: LiveFavoriteTagEntry) {
        viewModelScope.launch {
            SettingsManager.setLiveFavoriteTags(
                getApplication(),
                favoriteTags.value.filterNot {
                    it.parentAreaId == entry.parentAreaId && it.areaId == entry.areaId
                },
            )
        }
    }

    fun toggleFavoriteTag(entry: LiveFavoriteTagEntry) {
        viewModelScope.launch {
            SettingsManager.setLiveFavoriteTags(
                getApplication(),
                toggleLiveFavoriteTag(current = favoriteTags.value, entry = entry),
            )
        }
    }
}
