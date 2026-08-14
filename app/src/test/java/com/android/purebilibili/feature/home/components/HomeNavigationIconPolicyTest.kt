package com.android.purebilibili.feature.home.components

import kotlin.test.Test
import kotlin.test.assertEquals

class HomeNavigationIconPolicyTest {

    @Test
    fun `uses Miuix icons where the library has an equivalent symbol`() {
        listOf(
            "HOME",
            "HISTORY",
            "FAVORITE",
            "WATCHLATER",
            "SETTINGS",
            "PARTITION",
        ).forEach { tabId ->
            assertEquals(
                HomeNavigationIconSource.MIUIX,
                resolveMiuixPreferredHomeNavigationIconSource(tabId),
            )
        }
    }

    @Test
    fun `uses user supplied SVG vectors for missing navigation symbols`() {
        assertEquals(HomeNavigationIconSource.LOCAL_DYNAMIC, resolveMiuixPreferredHomeNavigationIconSource("DYNAMIC"))
        assertEquals(HomeNavigationIconSource.LOCAL_LIVE, resolveMiuixPreferredHomeNavigationIconSource("LIVE"))
        assertEquals(HomeNavigationIconSource.LOCAL_GAME, resolveMiuixPreferredHomeNavigationIconSource("GAME"))
    }

    @Test
    fun `uses Miuix for roles that previously depended on the active theme`() {
        listOf("PROFILE", "PLUGINS", "FOLLOW", "POPULAR", "ANIME", "KNOWLEDGE", "TECH").forEach { tabId ->
            assertEquals(
                HomeNavigationIconSource.MIUIX,
                resolveMiuixPreferredHomeNavigationIconSource(tabId),
            )
        }
    }
}
