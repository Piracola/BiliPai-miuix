package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily
import kotlin.test.Test
import kotlin.test.assertEquals

class BottomBarSettingsScreenIconPolicyTest {

    @Test
    fun bottomBarIconPolicy_usesSemanticIconsForSecondaryTabs() {
        assertSameVectorAsset(Icons.Outlined.NotificationsNone, resolveBottomBarTabIcon("DYNAMIC", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.CollectionsBookmark, resolveBottomBarTabIcon("FAVORITE", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.WatchLater, resolveBottomBarTabIcon("WATCHLATER", AppSemanticIconFamily.MATERIAL))
    }

    @Test
    fun bottomBarIconPolicy_usesMaterialIconFamily() {
        assertSameVectorAsset(Icons.Outlined.Home, resolveBottomBarTabIcon("HOME", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.NotificationsNone, resolveBottomBarTabIcon("DYNAMIC", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.CollectionsBookmark, resolveBottomBarTabIcon("FAVORITE", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.WatchLater, resolveBottomBarTabIcon("WATCHLATER", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.LiveTv, resolveBottomBarTabIcon("LIVE", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.Settings, resolveBottomBarTabIcon("SETTINGS", AppSemanticIconFamily.MATERIAL))
    }

    @Test
    fun topTabIconPolicy_usesSemanticIconsForContentCategories() {
        assertSameVectorAsset(Icons.Outlined.Person, resolveTopTabIcon("FOLLOW", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.AutoMirrored.Outlined.TrendingUp, resolveTopTabIcon("POPULAR", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.GridView, resolveTopTabIcon("PARTITION", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.Lightbulb, resolveTopTabIcon("KNOWLEDGE", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.SmartToy, resolveTopTabIcon("TECH", AppSemanticIconFamily.MATERIAL))
    }

    @Test
    fun topTabIconPolicy_usesMaterialIconFamily() {
        assertSameVectorAsset(Icons.Outlined.Person, resolveTopTabIcon("FOLLOW", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.AutoMirrored.Outlined.TrendingUp, resolveTopTabIcon("POPULAR", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.GridView, resolveTopTabIcon("PARTITION", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.Lightbulb, resolveTopTabIcon("KNOWLEDGE", AppSemanticIconFamily.MATERIAL))
        assertSameVectorAsset(Icons.Outlined.SmartToy, resolveTopTabIcon("TECH", AppSemanticIconFamily.MATERIAL))
    }

    @Test
    fun settingsPreviewIconPolicy_matchesHomeNavigationPolicy() {
        listOf(
            "HOME",
            "DYNAMIC",
            "HISTORY",
            "PROFILE",
            "FAVORITE",
            "LIVE",
            "WATCHLATER",
            "SETTINGS",
            "PLUGINS",
        ).forEach { tabId ->
            AppSemanticIconFamily.entries.forEach { family ->
                assertSameVectorAsset(
                    resolveSettingsNavigationPreviewIcon(tabId, family, selected = false),
                    resolveBottomBarTabIcon(tabId, family),
                )
            }
        }
    }

    private fun assertSameVectorAsset(expected: ImageVector, actual: ImageVector) {
        assertEquals(expected.name, actual.name)
        assertEquals(expected.defaultWidth, actual.defaultWidth)
        assertEquals(expected.defaultHeight, actual.defaultHeight)
        assertEquals(expected.viewportWidth, actual.viewportWidth)
        assertEquals(expected.viewportHeight, actual.viewportHeight)
    }
}
