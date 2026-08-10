package com.android.purebilibili.feature.list

import kotlin.test.Test
import kotlin.test.assertEquals

class FavoritePersonalCardPolicyTest {

    @Test
    fun favoriteDateLabel_addsFavoriteContext() {
        assertEquals("", resolveFavoriteDateLabel(0))
        assertEquals("刚刚收藏", resolveFavoriteDateLabel(1_700_000_000, nowMs = 1_700_000_000_000L))
    }
}
