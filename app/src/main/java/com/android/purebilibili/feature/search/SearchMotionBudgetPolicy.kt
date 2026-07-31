package com.android.purebilibili.feature.search

internal enum class SearchMotionBudget {
    FULL,
    REDUCED
}

internal fun resolveSearchMotionBudget(
    hasQuery: Boolean,
    isSearching: Boolean,
    isScrolling: Boolean
): SearchMotionBudget {
    return if (isSearching || (hasQuery && isScrolling)) {
        SearchMotionBudget.REDUCED
    } else {
        SearchMotionBudget.FULL
    }
}

internal fun shouldEnableSearchHazeSource(
    isSearching: Boolean,
    startupSettled: Boolean = true
): Boolean = startupSettled && !isSearching

internal fun resolveEffectiveSearchMotionBudget(
    startupSettled: Boolean,
    baseBudget: SearchMotionBudget
): SearchMotionBudget {
    return if (startupSettled) baseBudget else SearchMotionBudget.REDUCED
}

/** 搜索结果进入详情始终直接切换，旧设置值只保留兼容读取。 */
internal fun resolveEffectiveSearchCardTransitionEnabled(
    @Suppress("UNUSED_PARAMETER") cardTransitionEnabled: Boolean,
    @Suppress("UNUSED_PARAMETER") motionBudget: SearchMotionBudget,
    @Suppress("UNUSED_PARAMETER") isReturningFromVideoDetail: Boolean,
): Boolean {
    return false
}

internal fun shouldBootstrapSearchLandingData(
    startupSettled: Boolean,
    showResults: Boolean,
    query: String
): Boolean {
    return startupSettled && !showResults && query.isBlank()
}

internal fun shouldAutoFocusSearchField(
    startupSettled: Boolean,
    query: String
): Boolean {
    return startupSettled && query.isBlank()
}

internal fun shouldForceLowBudgetSearchHeaderBlur(
    isSearching: Boolean,
    isScrollingResults: Boolean
): Boolean {
    return isSearching && !isScrollingResults
}
