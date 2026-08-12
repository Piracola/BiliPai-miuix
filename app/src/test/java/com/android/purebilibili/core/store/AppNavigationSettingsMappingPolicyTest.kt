package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppNavigationSettingsMappingPolicyTest {

    @Test
    fun emptyPreferences_useExpectedNavigationDefaults() {
        val prefs = mutablePreferencesOf()

        val result = mapAppNavigationSettingsFromPreferences(prefs)

        assertEquals(
            SettingsManager.BottomBarVisibilityMode.ALWAYS_VISIBLE,
            result.bottomBarVisibilityMode
        )
        assertEquals(
            listOf("HOME", "DYNAMIC", "HISTORY", "LISTEN_VIDEO", "PROFILE"),
            result.orderedVisibleTabIds
        )
        assertEquals(emptyMap(), result.bottomBarItemColors)
        assertFalse(result.tabletUseSidebar)
        assertTrue(result.sidebarAccountSwitcherEnabled)
        assertTrue(result.predictiveBackEnabled)
        assertEquals("scale", result.predictiveBackAnimationStyle)
        assertEquals("auto", result.predictiveBackExitDirection)
        assertTrue(result.miuixTransitionBlurEnabled)
    }

    @Test
    fun emptyPreferences_onTablet_defaultSidebarEnabled() {
        val prefs = mutablePreferencesOf()

        val result = mapAppNavigationSettingsFromPreferences(
            preferences = prefs,
            defaultTabletUseSidebar = true
        )

        assertTrue(result.tabletUseSidebar)
        assertTrue(defaultTabletUseSidebar(isTabletDevice = true))
        assertFalse(defaultTabletUseSidebar(isTabletDevice = false))
    }

    @Test
    fun populatedPreferences_mapToNavigationSettingsCorrectly() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("bottom_bar_visibility_mode") to SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE.value,
            stringPreferencesKey("bottom_bar_order") to "PROFILE,HOME,DYNAMIC,HISTORY",
            stringPreferencesKey("bottom_bar_visible_tabs") to "HOME,PROFILE,HISTORY",
            stringPreferencesKey("bottom_bar_item_colors") to "HOME:2,PROFILE:4,INVALID:x,NO_COLON",
            booleanPreferencesKey("tablet_use_sidebar") to true,
            booleanPreferencesKey("sidebar_account_switcher_enabled") to false,
            booleanPreferencesKey("miuix_transition_blur_enabled") to false,
        )

        val result = mapAppNavigationSettingsFromPreferences(prefs)

        assertEquals(SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE, result.bottomBarVisibilityMode)
        assertEquals(listOf("PROFILE", "HOME", "HISTORY"), result.orderedVisibleTabIds)
        assertEquals(mapOf("HOME" to 2, "PROFILE" to 4, "INVALID" to 0), result.bottomBarItemColors)
        assertTrue(result.tabletUseSidebar)
        assertFalse(result.sidebarAccountSwitcherEnabled)
        assertFalse(result.miuixTransitionBlurEnabled)
    }

    @Test
    fun visibleBottomTabs_surviveWhenSavedOrderIsMissing() {
        val prefs = mutablePreferencesOf(
            stringPreferencesKey("bottom_bar_order") to "",
            stringPreferencesKey("bottom_bar_visible_tabs") to "HOME,DYNAMIC,HISTORY,PROFILE"
        )

        val result = mapAppNavigationSettingsFromPreferences(prefs)

        assertEquals(listOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"), result.orderedVisibleTabIds)
    }

    @Test
    fun listenVideoMigration_insertsBeforeProfileWhenCapacityRemains() {
        val result = resolveListenVideoBottomTabMigration(
            order = listOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
            visible = setOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
            migrationComplete = false
        )

        assertEquals(
            listOf("HOME", "DYNAMIC", "HISTORY", "LISTEN_VIDEO", "PROFILE"),
            result.order
        )
        assertEquals(
            setOf("HOME", "DYNAMIC", "HISTORY", "LISTEN_VIDEO", "PROFILE"),
            result.visible
        )
        assertTrue(result.markComplete)
    }

    @Test
    fun listenVideoMigration_preservesExistingFiveItemCustomization() {
        val original = listOf("HOME", "STORY", "FAVORITE", "LIVE", "PROFILE")

        val result = resolveListenVideoBottomTabMigration(
            order = original,
            visible = original.toSet(),
            migrationComplete = false
        )

        assertEquals(original, result.order)
        assertEquals(original.toSet(), result.visible)
        assertTrue(result.markComplete)
    }

    @Test
    fun listenVideoMigration_doesNotReinsertAfterUserHidesIt() {
        val original = listOf("HOME", "DYNAMIC", "HISTORY", "PROFILE", "LISTEN_VIDEO")

        val result = resolveListenVideoBottomTabMigration(
            order = original,
            visible = setOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"),
            migrationComplete = true
        )

        assertEquals(original, result.order)
        assertEquals(setOf("HOME", "DYNAMIC", "HISTORY", "PROFILE"), result.visible)
        assertFalse(result.markComplete)
    }
}
