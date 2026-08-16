// 文件路径: feature/following/FollowingListViewModel.kt
package com.android.purebilibili.feature.following

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.store.FollowingCacheStore
import com.android.purebilibili.data.model.response.FollowingUser
import com.android.purebilibili.data.model.response.RelationTagItem
import com.android.purebilibili.data.repository.ActionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FollowingListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<FollowingListUiState>(FollowingListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _isBatchUnfollowing = MutableStateFlow(false)
    val isBatchUnfollowing = _isBatchUnfollowing.asStateFlow()

    private val _followGroupTags = MutableStateFlow<List<RelationTagItem>>(emptyList())
    val followGroupTags = _followGroupTags.asStateFlow()

    private val _userFollowGroupIds = MutableStateFlow<Map<Long, Set<Long>>>(emptyMap())
    val userFollowGroupIds = _userFollowGroupIds.asStateFlow()

    private val _isFollowGroupMetaLoading = MutableStateFlow(false)
    val isFollowGroupMetaLoading = _isFollowGroupMetaLoading.asStateFlow()

    private var currentMid: Long = 0
    private val removedUserMids = mutableSetOf<Long>()
    private var followGroupRefreshJob: Job? = null
    private var loadRemainingPagesJob: Job? = null
    private var followingCacheSaveJob: Job? = null
    
    fun loadFollowingList(mid: Long, forceRefresh: Boolean = false) {
        if (mid <= 0) return
        if (shouldSkipFollowingReload(currentMid, mid, _uiState.value, forceRefresh)) return

        currentMid = mid
        removedUserMids.clear()
        followGroupRefreshJob?.cancel()
        followGroupRefreshJob = null
        loadRemainingPagesJob?.cancel()
        loadRemainingPagesJob = null
        followingCacheSaveJob?.cancel()
        followingCacheSaveJob = null
        _userFollowGroupIds.value = emptyMap()
        _isFollowGroupMetaLoading.value = false

        val restoredFromCache = restoreFollowingListFromPersistentCache(mid, forceRefresh)
        
        viewModelScope.launch {
            if (!restoredFromCache) {
                _uiState.value = FollowingListUiState.Loading
            }
            
            try {
                // 缓存只负责首屏快速展示，服务器第一页始终作为最新关注关系的真源。
                val response = NetworkModule.api.getFollowings(mid, pn = 1, ps = 50)
                if (response.code == 0 && response.data != null) {
                    val initialUsers = response.data.list.orEmpty()
                        .filterNot { removedUserMids.contains(it.mid) }
                    val total = response.data.total
                    
                    _uiState.value = FollowingListUiState.Success(
                        users = initialUsers,
                        total = total,
                        hasMore = initialUsers.size < total // 还有更多数据需要加载
                    )
                    persistFollowingCache(mid = mid, total = total, users = initialUsers)
                    refreshFollowGroupMetadata(initialUsers)
                    
                    // 2. 如果还有更多数据，自动在后台加载剩余所有页面 (为了支持全量搜索)
                    if (initialUsers.size < total) {
                        loadAllRemainingPages(mid, total, initialUsers)
                    }
                } else {
                    _uiState.update { current ->
                        resolveFollowingRefreshFailureState(
                            currentState = current,
                            message = "加载失败: ${response.message}"
                        )
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { current ->
                    resolveFollowingRefreshFailureState(
                        currentState = current,
                        message = e.message ?: "网络错误"
                    )
                }
            }
        }
    }

    private fun restoreFollowingListFromPersistentCache(mid: Long, forceRefresh: Boolean): Boolean {
        val context = NetworkModule.appContext ?: return false
        val snapshot = FollowingCacheStore.getSnapshot(context, mid) ?: return false
        if (!shouldUseFollowingPersistentCache(
                forceRefresh = forceRefresh,
                requestMid = mid,
                cachedMid = snapshot.mid,
                cachedUsersCount = snapshot.users.size
            )
        ) {
            return false
        }

        val users = snapshot.users
            .filterNot { removedUserMids.contains(it.mid) }
            .distinctBy { it.mid }
        if (users.isEmpty()) return false

        _uiState.value = FollowingListUiState.Success(
            users = users,
            total = snapshot.total.coerceAtLeast(users.size),
            isLoadingMore = false,
            hasMore = false
        )
        refreshFollowGroupMetadata(users)
        return true
    }
    
    // 自动加载剩余所有页面
    private fun loadAllRemainingPages(mid: Long, total: Int, initialUsers: List<FollowingUser>) {
        loadRemainingPagesJob?.cancel()
        loadRemainingPagesJob = viewModelScope.launch {
            try {
                var currentUsers = initialUsers.toMutableList()
                val pageSize = 50
                // 计算需要加载的总页数
                val totalPages = (total + pageSize - 1) / pageSize
                val startPage = (currentUsers.size / pageSize + 1).coerceAtLeast(2)
                var pagesSinceLastPublish = 0

                (_uiState.value as? FollowingListUiState.Success)?.let { current ->
                    if (current.users.size < total) {
                        _uiState.value = current.copy(isLoadingMore = true)
                    }
                }
                
                for (page in startPage..totalPages) {
                    if (mid != currentMid) break // 如果用户切换了查看的 UP 主，停止加载
                    
                    // 延迟一点时间，避免请求过于频繁触发风控
                    delay(300)
                    
                    val response = NetworkModule.api.getFollowings(mid, pn = page, ps = pageSize)
                    if (response.code == 0 && response.data != null) {
                        val newUsers = response.data.list.orEmpty()
                            .filterNot { removedUserMids.contains(it.mid) }
                        if (newUsers.isNotEmpty()) {
                            currentUsers = mergeFollowingUsersOffMain(currentUsers, newUsers)
                            pagesSinceLastPublish += 1

                            if (shouldPublishFollowingLoadBatch(
                                    loadedCount = currentUsers.size,
                                    total = total,
                                    pagesSinceLastPublish = pagesSinceLastPublish,
                                    publishIntervalPages = FOLLOWING_PAGE_PUBLISH_INTERVAL_PAGES
                                )
                            ) {
                                publishFollowingUsers(mid, total, currentUsers, isLoadingMore = true)
                                pagesSinceLastPublish = 0
                            }
                        } else {
                            break
                        }
                    } else {
                        break // 出错停止加载
                    }
                }

                if (mid == currentMid && isFollowingListIncomplete(currentUsers.size, total)) {
                    ActionRepository.getAllFollowGroupUsers().onSuccess { allUsers ->
                        val mergedUsers = mergeFollowingUsersOffMain(currentUsers, allUsers)
                        if (mergedUsers.size > currentUsers.size) {
                            currentUsers = mergedUsers
                            publishFollowingUsers(mid, total, currentUsers, isLoadingMore = true)
                        }
                    }.onFailure { error ->
                        com.android.purebilibili.core.util.Logger.w(
                            "FollowingListVM",
                            "fallback all follow group failed: ${error.message}"
                        )
                    }
                }
                
                // 加载完成
                val current = _uiState.value
                if (current is FollowingListUiState.Success) {
                    _uiState.value = current.copy(
                        isLoadingMore = false,
                        hasMore = isFollowingListIncomplete(current.users.size, current.total)
                    )
                    refreshFollowGroupMetadata(current.users)
                }
            } catch (e: Exception) {
                // 后台加载失败暂不干扰主流程
                val current = _uiState.value
                if (current is FollowingListUiState.Success) {
                    _uiState.value = current.copy(isLoadingMore = false)
                }
            } finally {
                loadRemainingPagesJob = null
            }
        }
    }
    
    fun loadMore() {
        val current = _uiState.value as? FollowingListUiState.Success ?: return
        if (current.isLoadingMore || !current.hasMore || currentMid <= 0L) return
        loadAllRemainingPages(currentMid, current.total, current.users)
    }

    private suspend fun mergeFollowingUsersOffMain(
        currentUsers: List<FollowingUser>,
        incomingUsers: List<FollowingUser>
    ): MutableList<FollowingUser> {
        val removedMids = removedUserMids.toSet()
        return withContext(Dispatchers.Default) {
            mergeFollowingUsersDistinct(
                currentUsers = currentUsers,
                incomingUsers = incomingUsers,
                removedUserMids = removedMids
            ).toMutableList()
        }
    }

    private fun publishFollowingUsers(
        mid: Long,
        total: Int,
        users: List<FollowingUser>,
        isLoadingMore: Boolean
    ) {
        val safeTotal = total.coerceAtLeast(users.size)
        _uiState.value = FollowingListUiState.Success(
            users = users.toList(),
            total = safeTotal,
            hasMore = isFollowingListIncomplete(users.size, safeTotal),
            isLoadingMore = isLoadingMore
        )
        persistFollowingCache(mid = mid, total = safeTotal, users = users)
    }

    suspend fun batchUnfollow(targetUsers: List<FollowingUser>): BatchUnfollowResult {
        if (targetUsers.isEmpty()) {
            return BatchUnfollowResult(successCount = 0, failedCount = 0)
        }
        if (_isBatchUnfollowing.value) {
            return BatchUnfollowResult(successCount = 0, failedCount = targetUsers.size)
        }

        _isBatchUnfollowing.value = true
        val successMids = mutableSetOf<Long>()
        var failedCount = 0
        try {
            targetUsers.forEachIndexed { index, user ->
                val success = unfollowWithRetry(user.mid)
                if (success) {
                    successMids.add(user.mid)
                } else {
                    failedCount += 1
                }
                if (index < targetUsers.lastIndex) {
                    delay(BATCH_UNFOLLOW_INTERVAL_MS)
                }
            }
            if (successMids.isNotEmpty()) {
                removedUserMids.addAll(successMids)
                applyRemovedUsers(successMids)
            }
            return BatchUnfollowResult(
                successCount = successMids.size,
                failedCount = failedCount,
                succeededMids = successMids
            )
        } finally {
            _isBatchUnfollowing.value = false
        }
    }

    private suspend fun unfollowWithRetry(mid: Long): Boolean {
        repeat(BATCH_UNFOLLOW_MAX_ATTEMPTS) { attempt ->
            val result = ActionRepository.followUser(mid, follow = false)
            if (result.isSuccess) return true

            val message = result.exceptionOrNull()?.message
            val retryable = isRetryableBatchOperationError(message)
            if (!retryable || attempt >= BATCH_UNFOLLOW_MAX_ATTEMPTS - 1) {
                return false
            }

            val backoffMs = BATCH_UNFOLLOW_RETRY_BASE_DELAY_MS * (attempt + 1)
            delay(backoffMs)
        }
        return false
    }

    private fun applyRemovedUsers(removedMids: Set<Long>) {
        val current = _uiState.value as? FollowingListUiState.Success ?: return
        val remainingUsers = current.users.filterNot { removedMids.contains(it.mid) }
        val reducedTotal = (current.total - removedMids.size).coerceAtLeast(remainingUsers.size)
        _userFollowGroupIds.update { currentMap ->
            currentMap - removedMids
        }
        _uiState.value = current.copy(
            users = remainingUsers,
            total = reducedTotal
        )
        persistFollowingCache(mid = currentMid, total = reducedTotal, users = remainingUsers)
    }

    private fun persistFollowingCache(mid: Long, total: Int, users: List<FollowingUser>) {
        if (mid <= 0L) return
        val context = NetworkModule.appContext ?: return
        val snapshotUsers = users.toList()

        followingCacheSaveJob?.cancel()
        followingCacheSaveJob = viewModelScope.launch(Dispatchers.IO) {
            if (snapshotUsers.isEmpty()) {
                FollowingCacheStore.clear(context)
            } else {
                FollowingCacheStore.saveSnapshot(
                    context = context,
                    mid = mid,
                    total = total,
                    users = snapshotUsers
                )
            }
        }
    }

    private fun refreshFollowGroupMetadata(users: List<FollowingUser>) {
        if (users.isEmpty()) return

        followGroupRefreshJob?.cancel()
        followGroupRefreshJob = viewModelScope.launch {
            if (_followGroupTags.value.isEmpty()) {
                ActionRepository.getFollowGroupTags().onSuccess { tags ->
                    _followGroupTags.value = tags
                        .filter { it.tagid != DEFAULT_FOLLOW_TAG_ID }
                        .sortedBy { it.tagid != SPECIAL_FOLLOW_TAG_ID }
                }
            }

            _isFollowGroupMetaLoading.value = true
            try {
                val userMidSet = users.asSequence().map { it.mid }.toSet()
                val orderedTagIds = buildList {
                    add(SPECIAL_FOLLOW_TAG_ID)
                    addAll(_followGroupTags.value.map { it.tagid })
                }.distinct().filter { it != DEFAULT_FOLLOW_TAG_ID }

                // 每轮重建当前列表的“已命中分组”映射，避免旧结果残留。
                _userFollowGroupIds.update { currentMap ->
                    currentMap - userMidSet
                }

                var allTagsFetched = true
                orderedTagIds.forEachIndexed { index, tagId ->
                    val result = ActionRepository.getFollowGroupMemberMids(
                        tagId = tagId,
                        targetMids = userMidSet
                    )
                    result.onSuccess { mids ->
                        if (mids.isEmpty()) return@onSuccess
                        _userFollowGroupIds.update { currentMap ->
                            val mutable = currentMap.toMutableMap()
                            mids.forEach { mid ->
                                val existing = mutable[mid].orEmpty().toMutableSet()
                                existing.add(tagId)
                                mutable[mid] = existing
                            }
                            mutable
                        }
                    }.onFailure { error ->
                        allTagsFetched = false
                        com.android.purebilibili.core.util.Logger.w(
                            "FollowingListVM",
                            "skip tag=$tagId group mapping: ${error.message}"
                        )
                    }

                    if (index < orderedTagIds.lastIndex) {
                        delay(FOLLOW_GROUP_META_FETCH_INTERVAL_MS)
                    }
                }

                // 只有全部分组都成功时，才把“未命中任何分组”的用户标记为默认分组。
                if (allTagsFetched) {
                    _userFollowGroupIds.update { currentMap ->
                        val mutable = currentMap
                            .filterKeys { userMidSet.contains(it) }
                            .toMutableMap()
                        users.forEach { user ->
                            if (!mutable.containsKey(user.mid)) {
                                mutable[user.mid] = emptySet()
                            }
                        }
                        mutable
                    }
                }
            } finally {
                _isFollowGroupMetaLoading.value = false
                followGroupRefreshJob = null
            }
        }
    }

    suspend fun prepareBatchGroupDialogData(targetMids: List<Long>): Result<BatchFollowGroupDialogData> {
        return runCatching {
            val mids = targetMids.toSet().toList()
            if (mids.isEmpty()) {
                return@runCatching BatchFollowGroupDialogData(
                    tags = emptyList(),
                    initialSelection = emptySet(),
                    hasMixedSelection = false
                )
            }

            val tags = ActionRepository.getFollowGroupTags().getOrThrow()
                .filter { it.tagid != 0L }
                .sortedBy { it.tagid != -10L }

            val currentGroups = _userFollowGroupIds.value
            val missingMids = mids.filterNot { currentGroups.containsKey(it) }
            val fetched = linkedMapOf<Long, Set<Long>>()
            missingMids.forEachIndexed { index, mid ->
                val error = addFollowGroupMappingIfSuccess(
                    target = fetched,
                    userMid = mid,
                    result = ActionRepository.getUserFollowGroupIds(mid)
                )
                if (error != null) {
                    com.android.purebilibili.core.util.Logger.w(
                        "FollowingListVM",
                        "skip group mapping for batch mid=$mid: ${error.message}"
                    )
                }
                if (index < missingMids.lastIndex) {
                    delay(FOLLOW_GROUP_META_FETCH_INTERVAL_MS)
                }
            }
            if (fetched.isNotEmpty()) {
                _userFollowGroupIds.update { it + fetched }
            }
            val mergedGroups = _userFollowGroupIds.value + fetched
            val groupSets = mids.map { mid -> mergedGroups[mid] ?: emptySet() }

            BatchFollowGroupDialogData(
                tags = tags,
                initialSelection = resolveFollowGroupInitialSelection(groupSets),
                hasMixedSelection = hasMixedFollowGroupSelection(groupSets)
            )
        }
    }

    suspend fun saveBatchGroupSelection(targetMids: List<Long>, selectedTagIds: Set<Long>): Result<Boolean> {
        return ActionRepository.overwriteFollowGroupIds(targetMids.toSet(), selectedTagIds)
    }
}
