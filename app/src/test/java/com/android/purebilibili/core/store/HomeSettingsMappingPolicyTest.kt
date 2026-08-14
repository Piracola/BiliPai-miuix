package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_DEFAULT_MILLIS
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_MAX_MILLIS
import com.android.purebilibili.core.ui.transition.VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionSpeed
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeSettingsMappingPolicyTest {

    @Test
    fun emptyPreferences_useExpectedRuntimeDefaults() {
        val prefs = mutablePreferencesOf()

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(0, result.displayMode)
        assertTrue(result.isBottomBarFloating)
        assertEquals(0, result.bottomBarLabelMode)
        assertEquals(SettingsManager.TopTabLabelMode.TEXT_ONLY, result.topTabLabelMode)
        assertEquals(HomeTopRightAction.SETTINGS, result.homeTopRightAction)
        assertEquals(HomeTopLayoutOrder.SEARCH_THEN_TABS, result.homeTopLayoutOrder)
        assertTrue(result.isHeaderBlurEnabled)
        assertEquals(HomeHeaderBlurMode.FOLLOW_PRESET, result.headerBlurMode)
        assertEquals(HomeHeaderCollapseMode.BOTH, result.homeHeaderCollapseMode)
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
            result.commonListHeaderCollapseMode
        )
        assertTrue(result.isHeaderCollapseEnabled)
        assertFalse(result.isBottomBarBlurEnabled)
        assertFalse(result.isBottomBarSearchEnabled)
        assertEquals(BottomBarSearchAutoExpandMode.EXPAND_AT_HOME_TOP, result.bottomBarSearchAutoExpandMode)
        assertEquals(BottomBarSearchLayoutMode.FULL_DOCK, result.bottomBarSearchLayoutMode)
        assertEquals(0, result.gridColumnCount)
        assertEquals(HomeFeedCardWidthPreset.AUTO, result.homeFeedCardWidthPreset)
        assertFalse(result.cardAnimationEnabled)
        assertTrue(result.cardTransitionEnabled)
        assertEquals(VideoSharedTransitionSpeed.STANDARD, result.videoSharedTransitionSpeed)
        assertEquals(
            VIDEO_SHARED_TRANSITION_CUSTOM_DEFAULT_MILLIS,
            result.videoSharedTransitionCustomDurationMillis
        )
        assertFalse(result.smartVisualGuardEnabled)
        assertTrue(result.compactVideoStatsOnCover)
        assertEquals(HomeFeedCardStyle.CURRENT, result.homeFeedCardStyle)
        assertEquals(HomeDurationStyle.OUTSIDE_COVER, result.homeDurationStyle)
        assertEquals(HomeWallpaperEffectMode.SOFT_BLUR, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.HOME_ONLY, result.homeWallpaperEffectScope)
        assertFalse(result.lowQualityHomeCoverInDataSaver)
        assertFalse(result.showHomeUpBadges)
        assertFalse(result.showHomeUpAvatars)
        assertFalse(result.crashTrackingConsentShown)
    }

    @Test
    fun populatedPreferences_mapToHomeSettingsCorrectly() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("display_mode") to 1,
            booleanPreferencesKey("bottom_bar_floating") to false,
            intPreferencesKey("bottom_bar_label_mode") to 2,
            intPreferencesKey("top_tab_label_mode") to 1,
            intPreferencesKey("home_top_right_action") to HomeTopRightAction.INBOX.value,
            intPreferencesKey("home_top_layout_order") to HomeTopLayoutOrder.TABS_THEN_SEARCH.value,
            booleanPreferencesKey("header_blur_enabled") to false,
            booleanPreferencesKey("header_collapse_enabled") to false,
            intPreferencesKey("home_header_collapse_mode") to HomeHeaderCollapseMode.TABS_ONLY.value,
            intPreferencesKey("common_list_header_collapse_mode") to
                CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY.value,
            booleanPreferencesKey("bottom_bar_blur_enabled") to false,
            booleanPreferencesKey("bottom_bar_search_enabled") to true,
            intPreferencesKey("bottom_bar_search_auto_expand_mode") to BottomBarSearchAutoExpandMode.DISABLED.value,
            intPreferencesKey("bottom_bar_search_layout_mode") to BottomBarSearchLayoutMode.HOME_AND_SEARCH.value,
            intPreferencesKey("grid_column_count") to 4,
            intPreferencesKey("home_feed_card_width_preset") to HomeFeedCardWidthPreset.WIDE.value,
            intPreferencesKey("home_feed_card_style") to HomeFeedCardStyle.OFFICIAL.value,
            booleanPreferencesKey("card_animation_enabled") to true,
            booleanPreferencesKey("card_transition_enabled") to false,
            intPreferencesKey("video_shared_transition_speed") to VideoSharedTransitionSpeed.CUSTOM.value,
            intPreferencesKey("video_shared_transition_custom_duration_millis") to 620,
            booleanPreferencesKey("smart_visual_guard_enabled") to false,
            booleanPreferencesKey("compact_video_stats_on_cover") to false,
            booleanPreferencesKey("home_video_duration_badges_visible") to false,
            intPreferencesKey("home_wallpaper_effect_mode") to HomeWallpaperEffectMode.STRONG_BLUR.value,
            intPreferencesKey("home_wallpaper_effect_scope") to HomeWallpaperEffectScope.GLOBAL.value,
            booleanPreferencesKey("low_quality_home_cover_in_data_saver") to true,
            booleanPreferencesKey("home_up_badges_visible") to false,
            booleanPreferencesKey("home_up_avatars_visible") to false,
            booleanPreferencesKey("crash_tracking_consent_shown") to true
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(1, result.displayMode)
        assertFalse(result.isBottomBarFloating)
        assertEquals(2, result.bottomBarLabelMode)
        assertEquals(1, result.topTabLabelMode)
        assertEquals(HomeTopRightAction.INBOX, result.homeTopRightAction)
        assertEquals(HomeTopLayoutOrder.TABS_THEN_SEARCH, result.homeTopLayoutOrder)
        assertFalse(result.isHeaderBlurEnabled)
        assertEquals(HomeHeaderBlurMode.ALWAYS_OFF, result.headerBlurMode)
        assertEquals(HomeHeaderCollapseMode.TABS_ONLY, result.homeHeaderCollapseMode)
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY,
            result.commonListHeaderCollapseMode
        )
        assertTrue(result.isHeaderCollapseEnabled)
        assertFalse(result.isBottomBarBlurEnabled)
        assertTrue(result.isBottomBarSearchEnabled)
        assertEquals(BottomBarSearchAutoExpandMode.DISABLED, result.bottomBarSearchAutoExpandMode)
        assertEquals(BottomBarSearchLayoutMode.HOME_AND_SEARCH, result.bottomBarSearchLayoutMode)
        assertEquals(4, result.gridColumnCount)
        assertEquals(HomeFeedCardWidthPreset.WIDE, result.homeFeedCardWidthPreset)
        assertTrue(result.cardAnimationEnabled)
        assertFalse(result.cardTransitionEnabled)
        assertEquals(VideoSharedTransitionSpeed.CUSTOM, result.videoSharedTransitionSpeed)
        assertEquals(620, result.videoSharedTransitionCustomDurationMillis)
        assertFalse(result.smartVisualGuardEnabled)
        assertFalse(result.compactVideoStatsOnCover)
        assertEquals(HomeFeedCardStyle.OFFICIAL, result.homeFeedCardStyle)
        assertEquals(HomeDurationStyle.HIDDEN, result.homeDurationStyle)
        assertEquals(HomeWallpaperEffectMode.STRONG_BLUR, result.homeWallpaperEffectMode)
        assertEquals(HomeWallpaperEffectScope.GLOBAL, result.homeWallpaperEffectScope)
        assertTrue(result.lowQualityHomeCoverInDataSaver)
        assertFalse(result.showHomeUpBadges)
        assertFalse(result.showHomeUpAvatars)
        assertTrue(result.crashTrackingConsentShown)
    }

    @Test
    fun invalidHomeFeedCardWidthPresetFallsBackToAuto() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_feed_card_width_preset") to 99
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeFeedCardWidthPreset.AUTO, result.homeFeedCardWidthPreset)
    }

    @Test
    fun invalidVideoSharedTransitionSpeedFallsBackToStandard() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("video_shared_transition_speed") to 99
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(VideoSharedTransitionSpeed.STANDARD, result.videoSharedTransitionSpeed)
    }

    @Test
    fun videoSharedTransitionCustomDurationIsClamped() {
        val lowPrefs = mutablePreferencesOf(
            intPreferencesKey("video_shared_transition_custom_duration_millis") to 120
        )
        val highPrefs = mutablePreferencesOf(
            intPreferencesKey("video_shared_transition_custom_duration_millis") to 1200
        )

        assertEquals(
            VIDEO_SHARED_TRANSITION_CUSTOM_MIN_MILLIS,
            mapHomeSettingsFromPreferences(lowPrefs).videoSharedTransitionCustomDurationMillis
        )
        assertEquals(
            VIDEO_SHARED_TRANSITION_CUSTOM_MAX_MILLIS,
            mapHomeSettingsFromPreferences(highPrefs).videoSharedTransitionCustomDurationMillis
        )
    }

    @Test
    fun invalidHomeTopRightActionFallsBackToSettings() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_top_right_action") to 99
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeTopRightAction.SETTINGS, result.homeTopRightAction)
    }

    @Test
    fun invalidHomeTopLayoutOrderFallsBackToSearchThenTabs() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_top_layout_order") to 99
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeTopLayoutOrder.SEARCH_THEN_TABS, result.homeTopLayoutOrder)
    }

    @Test
    fun legacyHeaderCollapseBoolean_mapsToEquivalentCollapseMode() {
        val disabledPrefs = mutablePreferencesOf(
            booleanPreferencesKey("header_collapse_enabled") to false
        )
        val enabledPrefs = mutablePreferencesOf(
            booleanPreferencesKey("header_collapse_enabled") to true
        )

        val disabled = mapHomeSettingsFromPreferences(disabledPrefs)
        val enabled = mapHomeSettingsFromPreferences(enabledPrefs)

        assertEquals(HomeHeaderCollapseMode.OFF, disabled.homeHeaderCollapseMode)
        assertFalse(disabled.isHeaderCollapseEnabled)
        assertEquals(HomeHeaderCollapseMode.BOTH, enabled.homeHeaderCollapseMode)
        assertTrue(enabled.isHeaderCollapseEnabled)
    }

    @Test
    fun invalidHomeHeaderCollapseModeFallsBackToBoth() {
        val prefs = mutablePreferencesOf(
            intPreferencesKey("home_header_collapse_mode") to 99
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeHeaderCollapseMode.BOTH, result.homeHeaderCollapseMode)
        assertTrue(result.isHeaderCollapseEnabled)
    }

    @Test
    fun searchCollapseSelectionPreservesCurrentTopTabBehavior() {
        assertEquals(
            HomeHeaderCollapseMode.BOTH,
            resolveHomeHeaderCollapseModeForSearch(
                currentMode = HomeHeaderCollapseMode.TABS_ONLY,
                collapseSearch = true
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.TABS_ONLY,
            resolveHomeHeaderCollapseModeForSearch(
                currentMode = HomeHeaderCollapseMode.BOTH,
                collapseSearch = false
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.SEARCH_ONLY,
            resolveHomeHeaderCollapseModeForSearch(
                currentMode = HomeHeaderCollapseMode.OFF,
                collapseSearch = true
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.OFF,
            resolveHomeHeaderCollapseModeForSearch(
                currentMode = HomeHeaderCollapseMode.SEARCH_ONLY,
                collapseSearch = false
            )
        )
    }

    @Test
    fun topTabCollapseSelectionPreservesCurrentSearchBehavior() {
        assertEquals(
            HomeHeaderCollapseMode.BOTH,
            resolveHomeHeaderCollapseModeForTopTabs(
                currentMode = HomeHeaderCollapseMode.SEARCH_ONLY,
                collapseTabs = true
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.SEARCH_ONLY,
            resolveHomeHeaderCollapseModeForTopTabs(
                currentMode = HomeHeaderCollapseMode.BOTH,
                collapseTabs = false
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.TABS_ONLY,
            resolveHomeHeaderCollapseModeForTopTabs(
                currentMode = HomeHeaderCollapseMode.OFF,
                collapseTabs = true
            )
        )
        assertEquals(
            HomeHeaderCollapseMode.OFF,
            resolveHomeHeaderCollapseModeForTopTabs(
                currentMode = HomeHeaderCollapseMode.TABS_ONLY,
                collapseTabs = false
            )
        )
    }

    @Test
    fun explicitHeaderBlurMode_overridesLegacyBoolean() {
        val prefs = mutablePreferencesOf(
            booleanPreferencesKey("header_blur_enabled") to false,
            intPreferencesKey("home_header_blur_mode") to HomeHeaderBlurMode.ALWAYS_ON.value
        )

        val result = mapHomeSettingsFromPreferences(prefs)

        assertEquals(HomeHeaderBlurMode.ALWAYS_ON, result.headerBlurMode)
        assertTrue(result.isHeaderBlurEnabled)
    }

    @Test
    fun followPresetHeaderBlur_keepsHeaderBlurOnForIosAndMd3() {
        assertTrue(
            resolveHomeHeaderBlurEnabled(
                mode = HomeHeaderBlurMode.FOLLOW_PRESET,
            )
        )
        assertTrue(
            resolveHomeHeaderBlurEnabled(
                mode = HomeHeaderBlurMode.FOLLOW_PRESET,
            )
        )
    }

    @Test
    fun homeDurationStyle_explicitValueWinsAndInvalidEnumsFallback() {
        val explicit = mapHomeSettingsFromPreferences(
            mutablePreferencesOf(
                booleanPreferencesKey("home_video_duration_badges_visible") to false,
                intPreferencesKey("home_duration_style") to HomeDurationStyle.OVERLAY_TEXT_ONLY.value,
                intPreferencesKey("home_feed_card_style") to 99
            )
        )

        assertEquals(HomeDurationStyle.OVERLAY_TEXT_ONLY, explicit.homeDurationStyle)
        assertEquals(HomeFeedCardStyle.CURRENT, explicit.homeFeedCardStyle)
    }

    @Test
    fun normalizeHomeRefreshCount_clampsToSupportedRange() {
        assertEquals(10, normalizeHomeRefreshCount(1))
        assertEquals(30, normalizeHomeRefreshCount(30))
        assertEquals(20, DEFAULT_HOME_REFRESH_COUNT)
        assertEquals(30, MAX_HOME_REFRESH_COUNT)
        assertEquals(30, normalizeHomeRefreshCount(999))
    }
}
