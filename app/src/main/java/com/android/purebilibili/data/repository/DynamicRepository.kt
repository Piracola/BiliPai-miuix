// 文件路径: data/repository/DynamicRepository.kt
package com.android.purebilibili.data.repository

import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.data.model.response.DynamicFeedResponse
import com.android.purebilibili.data.model.response.DynamicItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 *  动态数据仓库
 * 
 * 负责从 B站 API 获取动态 Feed 数据
 *
 * 分页语义对齐 bilibili-API-collect `docs/dynamic/all.md`：
 * - `offset`：翻页偏移，等于末条动态 id
 * - `update_baseline`：更新基线，等于首条动态 id；获取新动态时传入
 * - `update_num`：本次在更新基线以上的新动态条数
 */
object DynamicRepository {
    private val feedPagination = DynamicFeedPaginationRegistry()
    private val userFeedPagination = DynamicUserPaginationRegistry()
    
    /**
     * 获取动态列表
     * @param refresh 是否刷新 (重置分页)
     * @param incrementalRefresh 是否保留现有时间线，仅拉取更新基线之后的内容
     */
    suspend fun getDynamicFeed(
        refresh: Boolean = false,
        scope: DynamicFeedScope = DynamicFeedScope.DYNAMIC_SCREEN,
        type: String = "all",
        incrementalRefresh: Boolean = false
    ): Result<DynamicFeedFetchResult> = withContext(Dispatchers.IO) {
        try {
            val paginationBeforeRefresh = feedPagination.snapshot(scope, type)
            val useIncrementalRefresh = shouldUseDynamicIncrementalRefresh(
                refresh = refresh,
                incrementalRefreshEnabled = incrementalRefresh,
                updateBaseline = paginationBeforeRefresh.updateBaseline
            )
            if (refresh && !useIncrementalRefresh) {
                feedPagination.reset(scope, type)
            }
            val paginationForPageUpdate = if (refresh && !useIncrementalRefresh) {
                DynamicPaginationState()
            } else {
                paginationBeforeRefresh
            }
            if (!feedPagination.hasMore(scope, type) && !refresh) {
                return@withContext Result.success(
                    DynamicFeedFetchResult(
                        items = emptyList(),
                        updateNum = 0,
                        usedUpdateBaseline = false
                    )
                )
            }

            val visibleItems = mutableListOf<DynamicItem>()
            var pagesFetched = 0
            var fetchedItemCount = 0
            var reportedUpdateNum = 0
            var resolvedUpdateBaseline = paginationForPageUpdate.updateBaseline
            var requestOffset = if (refresh) "" else feedPagination.offset(scope, type)
            while (true) {
                val previousOffset = requestOffset
                val requestUpdateBaseline = if (previousOffset.isBlank() && useIncrementalRefresh) {
                    paginationBeforeRefresh.updateBaseline
                } else {
                    ""
                }
                val response = fetchDynamicFeedPageWithRetry {
                    NetworkModule.dynamicApi.getDynamicFeed(
                        type = type,
                        offset = previousOffset,
                        updateBaseline = requestUpdateBaseline
                    )
                }.getOrElse { error ->
                    return@withContext Result.failure(error)
                }

                val data = response.data
                if (data == null) {
                    feedPagination.updateState(
                        scope = scope,
                        type = type,
                        state = resolveDynamicPaginationStateAfterPage(
                            paginationBeforeRefresh = paginationForPageUpdate,
                            responseOffset = previousOffset,
                            responseUpdateBaseline = "",
                            responseHasMore = false,
                            preserveExistingPagination = useIncrementalRefresh
                        )
                    )
                    break
                }

                if (pagesFetched == 0) {
                    // 首包的 update_num 才是「相对 update_baseline 的新动态数」
                    reportedUpdateNum = data.update_num.coerceAtLeast(0)
                }
                resolvedUpdateBaseline = resolveDynamicFeedUpdateBaseline(
                    currentBaseline = resolvedUpdateBaseline,
                    responseBaseline = data.update_baseline,
                    pagesFetched = pagesFetched
                )

                // 更新分页状态
                requestOffset = data.offset
                feedPagination.updateState(
                    scope = scope,
                    type = type,
                    state = resolveDynamicPaginationStateAfterPage(
                        paginationBeforeRefresh = paginationForPageUpdate,
                        responseOffset = data.offset,
                        responseUpdateBaseline = resolvedUpdateBaseline,
                        responseHasMore = data.has_more,
                        preserveExistingPagination = useIncrementalRefresh
                    )
                )

                // 过滤不可见的动态
                visibleItems += data.items.filter { it.visible }
                fetchedItemCount += data.items.size
                pagesFetched += 1

                val shouldContinue = if (useIncrementalRefresh) {
                    shouldContinueDynamicIncrementalFetch(
                        accumulatedItemCount = fetchedItemCount,
                        updateNum = reportedUpdateNum,
                        hasMore = data.has_more,
                        previousOffset = previousOffset,
                        nextOffset = data.offset
                    )
                } else {
                    shouldContinueDynamicFetchAfterFilter(
                        accumulatedVisibleCount = visibleItems.size,
                        hasMore = data.has_more,
                        previousOffset = previousOffset,
                        nextOffset = data.offset,
                        pagesFetched = pagesFetched
                    )
                }
                if (!shouldContinue) {
                    break
                }
            }

            Result.success(
                DynamicFeedFetchResult(
                    items = visibleItems,
                    updateNum = reportedUpdateNum,
                    usedUpdateBaseline = useIncrementalRefresh
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun currentUpdateBaseline(
        scope: DynamicFeedScope = DynamicFeedScope.DYNAMIC_SCREEN,
        type: String = "all"
    ): String = feedPagination.updateBaseline(scope, type)
    
    /**
     *  [新增] 获取指定用户的动态列表
     * @param hostMid UP主 mid
     * @param refresh 是否刷新 (重置分页)
     */
    suspend fun getUserDynamicFeed(hostMid: Long, refresh: Boolean = false): Result<List<DynamicItem>> = withContext(Dispatchers.IO) {
        try {
            if (refresh) {
                userFeedPagination.reset(hostMid)
            }
            
            if (!userFeedPagination.hasMore(hostMid) && !refresh) {
                return@withContext Result.success(emptyList())
            }

            val visibleItems = mutableListOf<DynamicItem>()
            var pagesFetched = 0
            while (true) {
                val previousOffset = userFeedPagination.offset(hostMid)
                val response = fetchDynamicFeedPageWithRetry {
                    NetworkModule.dynamicApi.getUserDynamicFeed(
                        params = buildSelectedUserDynamicFeedParams(
                            hostMid = hostMid,
                            offset = previousOffset
                        )
                    )
                }.getOrElse { error ->
                    return@withContext Result.failure(error)
                }

                val data = response.data
                if (data == null) {
                    userFeedPagination.update(
                        hostMid = hostMid,
                        offset = previousOffset,
                        hasMore = false
                    )
                    break
                }

                // 更新分页状态
                userFeedPagination.update(
                    hostMid = hostMid,
                    offset = data.offset,
                    hasMore = data.has_more
                )

                // 过滤不可见的动态
                visibleItems += data.items.filter { it.visible }
                pagesFetched += 1

                if (!shouldContinueDynamicFetchAfterFilter(
                        accumulatedVisibleCount = visibleItems.size,
                        hasMore = data.has_more,
                        previousOffset = previousOffset,
                        nextOffset = data.offset,
                        pagesFetched = pagesFetched
                    )
                ) {
                    break
                }
            }

            Result.success(visibleItems)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     *  [新增] 获取单条动态详情（桌面端详情接口）
     */
    suspend fun getDynamicDetail(dynamicId: String): Result<DynamicItem> = withContext(Dispatchers.IO) {
        try {
            val cleanedId = dynamicId.trim()
            if (cleanedId.isEmpty()) {
                return@withContext Result.failure(IllegalArgumentException("dynamicId 不能为空"))
            }

            val desktopResponse = NetworkModule.dynamicApi.getDynamicDetail(id = cleanedId)
            if (desktopResponse.code == 0) {
                val item = desktopResponse.data?.item
                    ?: return@withContext Result.failure(Exception("动态详情为空"))
                if (shouldFetchOpusDetailForDynamicDetail(item)) {
                    fetchOpusDetailItem(cleanedId)?.let { return@withContext Result.success(it) }
                }
                if (shouldFetchStandardDetailForPlainTextDynamic(item)) {
                    fetchStandardDetailItem(cleanedId)?.let { standardItem ->
                        return@withContext Result.success(
                            mergeDynamicDetailWithLongerDesc(
                                desktopItem = item,
                                standardItem = standardItem,
                            )
                        )
                    }
                }
                if (!shouldFallbackForDynamicDetail(item)) {
                    return@withContext Result.success(item)
                }

                fetchOpusDetailItem(cleanedId)?.let { return@withContext Result.success(it) }

                val fallbackResponse = NetworkModule.dynamicApi.getDynamicDetailFallback(id = cleanedId)
                if (fallbackResponse.code == 0) {
                    val fallbackItem = fallbackResponse.data?.item
                    if (fallbackItem != null) {
                        return@withContext Result.success(fallbackItem)
                    }
                }
                // fallback 失败时保底返回 desktop 结果，避免直接报错
                return@withContext Result.success(item)
            }

            // desktop 接口失败时先走图文专用接口，再降级到 web 详情接口。
            fetchOpusDetailItem(cleanedId)?.let { return@withContext Result.success(it) }

            val fallbackResponse = NetworkModule.dynamicApi.getDynamicDetailFallback(id = cleanedId)
            if (fallbackResponse.code == 0) {
                val item = fallbackResponse.data?.item
                    ?: return@withContext Result.failure(Exception("动态详情为空"))
                return@withContext Result.success(item)
            }

            Result.failure(
                Exception(
                    "API error: ${desktopResponse.message.ifBlank { "desktop=${desktopResponse.code}" }}; " +
                        "fallback=${fallbackResponse.message.ifBlank { fallbackResponse.code.toString() }}"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun fetchOpusDetailItem(dynamicId: String): DynamicItem? {
        return runCatching {
            val response = NetworkModule.dynamicApi.getOpusDetail(id = dynamicId)
            response.data?.item?.takeIf { response.code == 0 }
        }.getOrNull()
    }

    private suspend fun fetchStandardDetailItem(dynamicId: String): DynamicItem? {
        return runCatching {
            val response = NetworkModule.dynamicApi.getDynamicDetailFallback(id = dynamicId)
            response.data?.item?.takeIf { response.code == 0 }
        }.getOrNull()
    }
    
    /**
     * 是否还有更多数据
     */
    fun hasMoreData(
        scope: DynamicFeedScope = DynamicFeedScope.DYNAMIC_SCREEN,
        type: String = "all"
    ): Boolean {
        return feedPagination.hasMore(scope, type)
    }

    suspend fun getDynamicUpdateCount(
        scope: DynamicFeedScope = DynamicFeedScope.DYNAMIC_SCREEN,
        type: String = "all",
        advanceBaseline: Boolean = true
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val updateBaseline = feedPagination.updateBaseline(scope, type)
            if (!advanceBaseline) {
                // 轻量未读接口：只返回新动态条数，避免轮询时拉全量 feed。
                val updateResponse = NetworkModule.dynamicApi.getDynamicUpdateCount(
                    type = type,
                    updateBaseline = updateBaseline
                )
                if (updateResponse.code != 0) {
                    return@withContext Result.failure(
                        Exception(
                            resolveDynamicFriendlyErrorMessage(
                                updateResponse.code,
                                updateResponse.message
                            )
                        )
                    )
                }
                val updateData = updateResponse.data
                    ?: return@withContext Result.failure(Exception("动态更新数为空"))
                return@withContext Result.success(updateData.update_num.coerceAtLeast(0))
            }
            val response = fetchDynamicFeedPageWithRetry {
                NetworkModule.dynamicApi.getDynamicFeed(
                    type = type,
                    offset = "",
                    updateBaseline = updateBaseline
                )
            }.getOrElse { error ->
                return@withContext Result.failure(error)
            }
            val data = response.data ?: return@withContext Result.failure(Exception("动态更新数为空"))
            val nextBaseline = resolveDynamicUpdateCountBaseline(
                currentBaseline = updateBaseline,
                responseBaseline = data.update_baseline,
                advanceBaseline = advanceBaseline
            )
            if (nextBaseline.isNotBlank() && nextBaseline != updateBaseline) {
                feedPagination.updateBaseline(
                    scope = scope,
                    type = type,
                    updateBaseline = nextBaseline
                )
            }
            Result.success(data.update_num.coerceAtLeast(0))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     *  [新增] 用户动态是否还有更多
     */
    fun userHasMoreData(hostMid: Long?): Boolean {
        if (hostMid == null || hostMid <= 0L) return true
        return userFeedPagination.hasMore(hostMid)
    }
    
    /**
     * 重置分页状态
     */
    fun resetPagination(
        scope: DynamicFeedScope = DynamicFeedScope.DYNAMIC_SCREEN,
        type: String = "all"
    ) {
        feedPagination.reset(scope, type)
    }
    
    /**
     *  [新增] 重置用户动态分页状态
     */
    fun resetUserPagination(hostMid: Long? = null) {
        if (hostMid == null || hostMid <= 0L) {
            userFeedPagination.resetAll()
        } else {
            userFeedPagination.reset(hostMid)
        }
    }

    private fun buildSelectedUserDynamicFeedParams(
        hostMid: Long,
        offset: String
    ): Map<String, String> {
        return mapOf(
            "host_mid" to hostMid.toString(),
            "offset" to offset,
            "page" to "1",
            "features" to "itemOpusStyle,listOnlyfans",
            "timezone_offset" to "-480",
            "platform" to "web",
            "web_location" to "333.1387"
        )
    }

    private suspend fun fetchDynamicFeedPageWithRetry(
        request: suspend () -> DynamicFeedResponse
    ): Result<DynamicFeedResponse> {
        var lastError: Throwable? = null
        for (attempt in 1..DYNAMIC_FETCH_MAX_ATTEMPTS) {
            try {
                val response = request()
                if (response.code == 0) {
                    return Result.success(response)
                }
                val shouldRetry = attempt < DYNAMIC_FETCH_MAX_ATTEMPTS &&
                    isRetryableDynamicApiError(response.code, response.message)
                if (shouldRetry) {
                    delay(resolveDynamicRetryDelayMs(attempt))
                    continue
                }
                val message = resolveDynamicFriendlyErrorMessage(response.code, response.message)
                return Result.failure(Exception(message))
            } catch (error: Exception) {
                lastError = error
                val shouldRetry = attempt < DYNAMIC_FETCH_MAX_ATTEMPTS &&
                    isRetryableDynamicException(error)
                if (shouldRetry) {
                    delay(resolveDynamicRetryDelayMs(attempt))
                    continue
                }
                val message = resolveDynamicFriendlyErrorMessage(code = -1, message = error.message.orEmpty())
                return Result.failure(Exception(message, error))
            }
        }
        val message = resolveDynamicFriendlyErrorMessage(code = -1, message = lastError?.message.orEmpty())
        return Result.failure(Exception(message, lastError))
    }
}

internal fun resolveDynamicUpdateCountBaseline(
    currentBaseline: String,
    responseBaseline: String,
    advanceBaseline: Boolean
): String {
    if (responseBaseline.isBlank()) return currentBaseline
    if (advanceBaseline) return responseBaseline
    return currentBaseline
}

internal fun shouldUseDynamicIncrementalRefresh(
    refresh: Boolean,
    incrementalRefreshEnabled: Boolean,
    updateBaseline: String
): Boolean {
    return refresh && incrementalRefreshEnabled && updateBaseline.isNotBlank()
}

internal fun resolveDynamicPaginationStateAfterPage(
    paginationBeforeRefresh: DynamicPaginationState,
    responseOffset: String,
    responseUpdateBaseline: String,
    responseHasMore: Boolean,
    preserveExistingPagination: Boolean
): DynamicPaginationState {
    val nextBaseline = responseUpdateBaseline.ifBlank {
        paginationBeforeRefresh.updateBaseline
    }
    return if (preserveExistingPagination) {
        paginationBeforeRefresh.copy(updateBaseline = nextBaseline)
    } else {
        DynamicPaginationState(
            offset = responseOffset,
            updateBaseline = nextBaseline,
            hasMore = responseHasMore
        )
    }
}

enum class DynamicFeedScope {
    DYNAMIC_SCREEN,
    HOME_FOLLOW
}

data class DynamicFeedFetchResult(
    val items: List<DynamicItem>,
    val updateNum: Int = 0,
    val usedUpdateBaseline: Boolean = false
)

internal data class DynamicPaginationState(
    var offset: String = "",
    var updateBaseline: String = "",
    var hasMore: Boolean = true
)

internal data class DynamicFeedPaginationKey(
    val scope: DynamicFeedScope,
    val type: String
)

internal class DynamicFeedPaginationRegistry {
    private val stateByScope = mutableMapOf<DynamicFeedPaginationKey, DynamicPaginationState>()

    fun reset(scope: DynamicFeedScope, type: String = "all") {
        stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)] = DynamicPaginationState()
    }

    fun update(
        scope: DynamicFeedScope,
        type: String = "all",
        offset: String,
        updateBaseline: String = "",
        hasMore: Boolean
    ) {
        stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)] =
            DynamicPaginationState(
                offset = offset,
                updateBaseline = updateBaseline,
                hasMore = hasMore
            )
    }

    fun updateState(
        scope: DynamicFeedScope,
        type: String = "all",
        state: DynamicPaginationState
    ) {
        stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)] = state.copy()
    }

    fun snapshot(
        scope: DynamicFeedScope,
        type: String = "all"
    ): DynamicPaginationState {
        return stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)]?.copy()
            ?: DynamicPaginationState()
    }

    fun offset(scope: DynamicFeedScope, type: String = "all"): String {
        return stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)]?.offset.orEmpty()
    }

    fun updateBaseline(scope: DynamicFeedScope, type: String = "all"): String {
        return stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)]?.updateBaseline.orEmpty()
    }

    fun updateBaseline(
        scope: DynamicFeedScope,
        type: String = "all",
        updateBaseline: String
    ) {
        val key = DynamicFeedPaginationKey(scope = scope, type = type)
        val current = stateByScope[key] ?: DynamicPaginationState()
        stateByScope[key] = current.copy(updateBaseline = updateBaseline)
    }

    fun hasMore(scope: DynamicFeedScope, type: String = "all"): Boolean {
        return stateByScope[DynamicFeedPaginationKey(scope = scope, type = type)]?.hasMore ?: true
    }
}

internal class DynamicUserPaginationRegistry {
    private val stateByUser = mutableMapOf<Long, DynamicPaginationState>()

    fun reset(hostMid: Long) {
        stateByUser[hostMid] = DynamicPaginationState()
    }

    fun resetAll() {
        stateByUser.clear()
    }

    fun update(hostMid: Long, offset: String, hasMore: Boolean) {
        stateByUser[hostMid] = DynamicPaginationState(offset = offset, hasMore = hasMore)
    }

    fun offset(hostMid: Long): String {
        return stateByUser[hostMid]?.offset.orEmpty()
    }

    fun hasMore(hostMid: Long): Boolean {
        return stateByUser[hostMid]?.hasMore ?: true
    }
}
