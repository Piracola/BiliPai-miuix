package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppPullRefreshIndicatorPolicyTest {

    @Test
    fun refreshIndicatorRenderer_isSingleMiuixValue() {
        assertEquals(
            AppPullRefreshIndicatorRenderer.MIUIX,
            resolveAppPullRefreshIndicatorRenderer(),
        )
    }
}
