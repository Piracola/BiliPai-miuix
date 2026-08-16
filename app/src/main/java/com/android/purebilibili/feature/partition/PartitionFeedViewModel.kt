// 文件路径: feature/partition/PartitionFeedViewModel.kt
package com.android.purebilibili.feature.partition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.store.HomeFeedCardStyle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.util.resolveReplaceRefreshPage
import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

internal val partitionTabs = listOf(
    PartitionCategory(0, "全站")
) + allPartitions

data class PartitionFeedUiState(
    val selectedPartition: PartitionCategory = partitionTabs.first(),
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

/**
 * 分区 feed 的 Coordinator（阶段 4 垂直切片）。
 *
 * 设置读取（TopTab 标签模式、首页卡片样式）由本层以 Flow 暴露给 UI，
 * UI 不再直接访问 SettingsManager；数据获取走 VideoRepository，UI 不触碰
 * Repository 单例。
 */
class PartitionFeedViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(PartitionFeedUiState())
    val uiState = _uiState.asStateFlow()

    val topTabLabelMode: StateFlow<Int> = SettingsManager
        .getHomeSettings(getApplication())
        .map { it.topTabLabelMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeSettings().topTabLabelMode)

    val homeFeedCardStyle: StateFlow<HomeFeedCardStyle> = SettingsManager
        .getHomeFeedCardStyle(getApplication())
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeFeedCardStyle.CURRENT)

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
