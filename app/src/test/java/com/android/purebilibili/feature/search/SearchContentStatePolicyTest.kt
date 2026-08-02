package com.android.purebilibili.feature.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun `load more failure preserves existing results and retry metadata`() {
        val original = SearchUiState(
            query = "kotlin",
            searchResults = listOf(),
            currentPage = 3,
            totalPages = 8,
            hasMoreResults = true,
            isLoadingMore = true,
        )

        val failed = original.withLoadMoreFailure("network")

        assertEquals(original.searchResults, failed.searchResults)
        assertEquals(original.currentPage, failed.currentPage)
        assertEquals(original.totalPages, failed.totalPages)
        assertTrue(failed.hasMoreResults)
        assertFalse(failed.isLoadingMore)
        assertEquals("network", failed.loadMoreError)
    }

    @Test
    fun `starting load more clears only the previous pagination error`() {
        val started = SearchUiState(
            query = "kotlin",
            error = "blocking error",
            loadMoreError = "old page error",
            isLoadingMore = false,
        ).withLoadMoreStarted()

        assertTrue(started.isLoadingMore)
        assertEquals(null, started.loadMoreError)
        assertEquals("blocking error", started.error)
    }
}
