package com.android.purebilibili.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals

class BiliPaiVideoSourcePolicyTest {

    @Test
    fun homeCategoryRouteKeepsItsCategoryQuery() {
        assertEquals(
            "home?category=POPULAR",
            normalizeBiliPaiVideoSourceRoute("home?category=POPULAR"),
        )
    }

    @Test
    fun ordinaryRouteDropsItsQuery() {
        assertEquals(
            "search",
            normalizeBiliPaiVideoSourceRoute("search?keyword=test"),
        )
    }

    @Test
    fun blankRouteHasNoSourceMetadata() {
        assertEquals(null, normalizeBiliPaiVideoSourceRoute("  "))
    }
}
