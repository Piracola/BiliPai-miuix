package com.android.purebilibili.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SettingsSearchFocusPolicyTest {

    @Test
    fun appearanceFocusKey_mapsToStableGroupKeys() {
        // P3：外观页搜索定位改为 focusId → 稳定组 key，不再维护 LazyColumn 索引表。
        assertEquals(
            AppearanceSettingsGroupKeys.UI_AND_DARK,
            resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_THEME),
        )
        assertEquals(
            AppearanceSettingsGroupKeys.TEXT_AND_DISPLAY,
            resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_DISPLAY),
        )
        assertEquals(
            AppearanceSettingsGroupKeys.SPLASH,
            resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_SPLASH),
        )
        assertEquals(
            AppearanceSettingsGroupKeys.PERSONALIZATION,
            resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_PERSONALIZATION),
        )
        assertNull(resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_TABLET))
        assertNull(resolveAppearanceSettingsFocusKey(SettingsSearchFocusIds.HOME_OVERVIEW))
        assertNull(resolveAppearanceSettingsFocusKey("unknown_focus_id"))
    }

    @Test
    fun homeFocusKey_mapsToStableGroupKey() {
        // HOME 内容模式复用同一 key 体系，与外观模式定位到同一语义目标。
        assertEquals(
            AppearanceSettingsGroupKeys.HOME_OVERVIEW,
            resolveHomeSettingsFocusKey(SettingsSearchFocusIds.HOME_OVERVIEW),
        )
        assertNull(resolveHomeSettingsFocusKey(SettingsSearchFocusIds.APPEARANCE_THEME))
        assertNull(resolveHomeSettingsFocusKey("unknown_focus_id"))
    }

    @Test
    fun appearanceGroupKeys_areStableStrings() {
        // 稳定 key 不得随组顺序或条件项显隐变化（三场景共享同一目标）。
        val keys = listOf(
            AppearanceSettingsGroupKeys.UI_AND_DARK,
            AppearanceSettingsGroupKeys.COLOR,
            AppearanceSettingsGroupKeys.SURFACE_AND_EFFECTS,
            AppearanceSettingsGroupKeys.TEXT_AND_DISPLAY,
            AppearanceSettingsGroupKeys.ADVANCED_OVERRIDES,
            AppearanceSettingsGroupKeys.SPLASH,
            AppearanceSettingsGroupKeys.PERSONALIZATION,
            AppearanceSettingsGroupKeys.HOME_OVERVIEW,
        )
        assertEquals(keys.size, keys.toSet().size, "Appearance group keys must be unique")
        assertTrue(keys.all { it.startsWith("appearance_group_") })
    }

    @Test
    fun playbackFocusIndex_mapsSectionsByActualLazyColumnOrder() {
        assertEquals(10, resolvePlaybackSettingsScrollIndex(SettingsSearchFocusIds.PLAYBACK_NETWORK))
        assertEquals(12, resolvePlaybackSettingsScrollIndex(SettingsSearchFocusIds.PLAYBACK_DATA_SAVER))
        assertEquals(14, resolvePlaybackSettingsScrollIndex(SettingsSearchFocusIds.PLAYBACK_INTERACTION))
        assertEquals(16, resolvePlaybackSettingsScrollIndex(SettingsSearchFocusIds.PLAYBACK_FULLSCREEN))
    }

    @Test
    fun bottomBarFocusIndex_mapsAvailableItemsSection() {
        assertEquals(0, resolveBottomBarSettingsScrollIndex(SettingsSearchFocusIds.BOTTOM_BAR_START))
        assertEquals(1, resolveBottomBarSettingsScrollIndex(SettingsSearchFocusIds.BOTTOM_BAR_BEHAVIOR))
        assertEquals(11, resolveBottomBarSettingsScrollIndex(SettingsSearchFocusIds.BOTTOM_BAR_AVAILABLE))
        assertEquals(7, resolveBottomBarSettingsScrollIndex(SettingsSearchFocusIds.BOTTOM_BAR_TABLET))
    }

    @Test
    fun animationFocusIndex_mapsVisualEffectsSection() {
        assertEquals(0, resolveAnimationSettingsScrollIndex(SettingsSearchFocusIds.ANIMATION_START))
        assertEquals(
            4,
            resolveAnimationSettingsScrollIndex(SettingsSearchFocusIds.ANIMATION_VISUAL_EFFECTS)
        )
    }

    @Test
    fun functionLevelSearchResult_carriesFocusId() {
        val results = resolveSettingsSearchResults("画中画")

        assertTrue(
            results.any {
                it.target == SettingsSearchTarget.PLAYBACK &&
                    it.focusId == SettingsSearchFocusIds.PLAYBACK_MINI_PLAYER
            }
        )
    }

    @Test
    fun sceneSearchTargetsResolveToExistingDetailFocus() {
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.HOME_FEED,
                focusId = SettingsSearchFocusIds.HOME_OVERVIEW
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.HOME_FEED)
        )
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.BOTTOM_BAR,
                focusId = SettingsSearchFocusIds.BOTTOM_BAR_TOP_TABS
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.NAVIGATION)
        )
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.PLAYBACK,
                focusId = SettingsSearchFocusIds.PLAYBACK_NETWORK
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.PLAYBACK_QUALITY)
        )
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.PLAYBACK,
                focusId = SettingsSearchFocusIds.PLAYBACK_FULLSCREEN
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.FULLSCREEN_GESTURE)
        )
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.PLAYBACK,
                focusId = SettingsSearchFocusIds.PLAYBACK_INTERACTION
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.INTERACTION_COMMENT)
        )
        assertEquals(
            SettingsSceneDetailFocus(
                target = SettingsSearchTarget.PLAYBACK,
                focusId = SettingsSearchFocusIds.PLAYBACK_DEBUG
            ),
            resolveSettingsSceneDetailFocus(SettingsSearchTarget.DIAGNOSTICS)
        )
    }
}
