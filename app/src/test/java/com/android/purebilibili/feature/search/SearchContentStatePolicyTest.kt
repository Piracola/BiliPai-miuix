package com.android.purebilibili.feature.search

import kotlin.test.Test
import kotlin.test.assertEquals

class SearchContentStatePolicyTest {

    @Test
    fun `initial search shows loading before any result exists`() {
        assertEquals(
            SearchResultPresentation(
                body = SearchResultBodyMode.LOADING,
                footer = SearchResultFooterMode.NONE,
            ),
            resolveSearchResultPresentation(
                itemCount = 0,
                isSearching = true,
                error = null,
                emptyStateReason = SearchEmptyStateReason.NONE,
                isLoadingMore = false,
                loadMoreError = null,
                hasMoreResults = false,
            )
        )
    }

    @Test
    fun `blocking failure wins over empty state`() {
        assertEquals(
            SearchResultBodyMode.ERROR,
            resolveSearchResultPresentation(
                itemCount = 0,
                isSearching = false,
                error = "network",
                emptyStateReason = SearchEmptyStateReason.NO_RESULTS,
                isLoadingMore = false,
                loadMoreError = null,
                hasMoreResults = false,
            ).body
        )
    }

    @Test
    fun `empty result renders empty state`() {
        assertEquals(
            SearchResultBodyMode.EMPTY,
            resolveSearchResultPresentation(
                itemCount = 0,
                isSearching = false,
                error = null,
                emptyStateReason = SearchEmptyStateReason.NO_RESULTS,
                isLoadingMore = false,
                loadMoreError = null,
                hasMoreResults = false,
            ).body
        )
    }

    @Test
    fun `pagination failure stays at the content footer`() {
        assertEquals(
            SearchResultFooterMode.ERROR,
            resolveSearchResultPresentation(
                itemCount = 12,
                isSearching = false,
                error = null,
                emptyStateReason = SearchEmptyStateReason.NONE,
                isLoadingMore = false,
                loadMoreError = "page two failed",
                hasMoreResults = true,
            ).footer
        )
    }

    @Test
    fun `pagination end is only shown after content`() {
        assertEquals(
            SearchResultFooterMode.END,
            resolveSearchResultPresentation(
                itemCount = 12,
                isSearching = false,
                error = null,
                emptyStateReason = SearchEmptyStateReason.NONE,
                isLoadingMore = false,
                loadMoreError = null,
                hasMoreResults = false,
            ).footer
        )
    }

    @Test
    fun `landing failure is local when old data exists`() {
        assertEquals(
            SearchLandingSectionMode.CONTENT,
            resolveSearchLandingSectionMode(
                enabled = true,
                itemCount = 5,
                isRefreshing = false,
                error = "network",
            )
        )
    }

    @Test
    fun `disabled landing section is hidden`() {
        assertEquals(
            SearchLandingSectionMode.HIDDEN,
            resolveSearchLandingSectionMode(
                enabled = false,
                itemCount = 0,
                isRefreshing = false,
                error = null,
            )
        )
    }
}
