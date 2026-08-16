package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptivePullToRefreshPolicyTest {

    @Test
    fun `miuix refresh texts use localized home hints`() {
        assertEquals(
            listOf("下拉刷新...", "松手刷新", "正在刷新...", "刷新完成"),
            resolveMiuixPullToRefreshTexts()
        )
    }

    @Test
    fun `pull refresh indicator top inset clamps overlay chrome height`() {
        assertEquals(0f, resolvePullRefreshIndicatorTopInsetDp(-8f), 0.001f)
        assertEquals(0f, resolvePullRefreshIndicatorTopInsetDp(0f), 0.001f)
        assertEquals(96f, resolvePullRefreshIndicatorTopInsetDp(96f), 0.001f)
        assertEquals(0f, resolveScaffoldedPullRefreshIndicatorTopInsetDp(), 0.001f)
    }
}
