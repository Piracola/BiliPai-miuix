package com.android.purebilibili.feature.settings

import com.android.purebilibili.navigation3.BiliPaiNavKey
import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsNavHierarchyPolicyTest {
    @Test
    fun settingsHierarchyIsRecognized() {
        assertTrue(isSettingsSubtreeRoute("settings"))
        assertTrue(isSettingsSubtreeRoute("animation_settings"))
        assertFalse(isSettingsSubtreeRoute("home"))
        assertEquals("appearance_settings", resolveSettingsNavParentRoute("animation_settings"))
    }

    @Test
    fun settingsPagesUseTheStandardPageStack() {
        assertEquals(
            BiliPaiNavRouteTransition.STACK,
            resolveSettingsNavRouteTransition("settings", "settings_category", forward = true),
        )
        assertEquals(
            BiliPaiNavRouteTransition.STACK,
            resolveSettingsNavPopTransition(
                fromKey = BiliPaiNavKey.AppearanceSettings,
                toKey = BiliPaiNavKey.MainHost,
                activeMainHostRoute = "settings",
            ),
        )
    }

    @Test
    fun unrelatedRoutesDoNotGetSettingsHierarchyTransition() {
        assertNull(resolveSettingsNavRouteTransition("home", "settings", forward = true))
        assertNull(
            resolveSettingsNavPopTransition(
                fromKey = BiliPaiNavKey.AppearanceSettings,
                toKey = BiliPaiNavKey.MainHost,
                activeMainHostRoute = "home",
            )
        )
    }
}
