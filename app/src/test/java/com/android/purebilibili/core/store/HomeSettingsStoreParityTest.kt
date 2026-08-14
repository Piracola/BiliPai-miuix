package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.android.purebilibili.core.store.home.HomeSettingsStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeSettingsStoreParityTest {

    @Test
    fun `home store maps defaults the same way as settings manager policy`() {
        val prefs = mutablePreferencesOf()

        assertEquals(
            mapHomeSettingsFromPreferences(prefs),
            HomeSettingsStore.mapFromPreferences(prefs)
        )
    }

    @Test
    fun `home store maps populated preferences the same way as settings manager policy`() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("display_mode") to 1,
            booleanPreferencesKey("bottom_bar_floating") to false,
            intPreferencesKey("bottom_bar_label_mode") to 2
        )

        assertEquals(
            mapHomeSettingsFromPreferences(prefs),
            HomeSettingsStore.mapFromPreferences(prefs)
        )
    }

    @Test
    fun `home settings keep retired card surface effects disabled`() {
        val result = mapHomeSettingsFromPreferences(mutablePreferencesOf())

        assertEquals(HomeCardBadgeEffectMode.OFF, result.homeCardBadgeEffectMode)
        assertEquals(HomeWallpaperEffectMode.SOFT_BLUR, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.HOME_ONLY, result.homeWallpaperEffectScope)
        assertFalse(result.showHomeUpBadges)
        assertFalse(result.showHomeUpAvatars)
        assertEquals(HomeDurationStyle.OUTSIDE_COVER, result.homeDurationStyle)
    }

    @Test
    fun `home settings ignore saved retired card surface effects`() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_card_badge_effect_mode") to HomeCardBadgeEffectMode.LIGHT_BLUR.value,
            intPreferencesKey("home_wallpaper_effect_mode") to HomeWallpaperEffectMode.OFF.value,
            intPreferencesKey("home_wallpaper_effect_scope") to HomeWallpaperEffectScope.GLOBAL.value,
            booleanPreferencesKey("home_up_badges_visible") to false,
            booleanPreferencesKey("home_up_avatars_visible") to false,
            booleanPreferencesKey("home_video_duration_badges_visible") to false
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeCardBadgeEffectMode.OFF, result.homeCardBadgeEffectMode)
        assertEquals(HomeWallpaperEffectMode.OFF, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.GLOBAL, result.homeWallpaperEffectScope)
        assertEquals(false, result.showHomeUpBadges)
        assertEquals(false, result.showHomeUpAvatars)
        assertEquals(HomeDurationStyle.HIDDEN, result.homeDurationStyle)
    }

    @Test
    fun `home settings ignore retired badge mode`() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_card_badge_effect_mode") to HomeCardBadgeEffectMode.LIGHT_BLUR.value
        )
        val result = mapHomeSettingsFromPreferences(prefs)
        assertEquals(HomeCardBadgeEffectMode.OFF, result.homeCardBadgeEffectMode)
    }
}
