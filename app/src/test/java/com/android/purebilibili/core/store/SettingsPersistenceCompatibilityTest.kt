package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.android.purebilibili.core.theme.AppFontSizePreset
import com.android.purebilibili.core.theme.AppUiScalePreset
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.feature.settings.AppLanguage
import com.android.purebilibili.feature.settings.AppThemeMode
import com.android.purebilibili.feature.settings.DarkThemeStyle
import com.android.purebilibili.feature.settings.resolveDarkThemeStylePreference
import com.android.purebilibili.feature.settings.resolveThemeModePreference
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 阶段 2：设置持久化兼容测试。
 *
 * 覆盖 FULL_REFACTOR_PLAN.md 阶段 2 的验收点——「旧 prefs schema 可读、默认值
 * 正确」。策略：**只测解析层**（Preferences 纯对象 → 值），不触碰 Context /
 * DataStore I/O（仓库无 Robolectric 先例，真值读写是不可测的进程级 delegate）。
 *
 * 测试原则：
 * 1. 默认值正确：空 Preferences 快照 → map* 输出必须与 data class 默认值一致。
 * 2. 旧 schema 可读：只塞旧 key（legacy 字段名）不塞新 key → legacy 回退分支生效，
 *    这是 schema 冻结期（新 key 只增不改）的守护：任何破坏旧数据读取的改动都会红。
 */
class SettingsPersistenceCompatibilityTest {

    // ── 1. 默认值正确 ────────────────────────────────────────────────

    @Test
    fun emptyPreferences_produceThemeDefaults() {
        val mapped = mapAppThemeSettingsFromPreferences(mutablePreferencesOf())
        assertEquals(AppThemeMode.FOLLOW_SYSTEM, mapped.themeMode)
        assertEquals(DarkThemeStyle.DEFAULT, mapped.darkThemeStyle)
        assertEquals(AppLanguage.FOLLOW_SYSTEM, mapped.appLanguage)
        assertEquals(AppFontSizePreset.DEFAULT, mapped.appFontSizePreset)
        assertEquals(AppUiScalePreset.STANDARD, mapped.appUiScalePreset)
        assertEquals(AppListItemStyle.AUTO, mapped.appListItemStyle)
    }

    @Test
    fun emptyPreferences_produceHomeDefaults() {
        val mapped = mapHomeSettingsFromPreferences(mutablePreferencesOf())
        assertEquals(true, mapped.isBottomBarFloating)
        assertEquals(HomeHeaderBlurMode.FOLLOW_PRESET, mapped.headerBlurMode)
        assertEquals(true, mapped.isHeaderBlurEnabled)
        assertEquals(false, mapped.cardAnimationEnabled)
        assertEquals(true, mapped.cardTransitionEnabled)
        assertEquals(HomeDurationStyle.OUTSIDE_COVER, mapped.homeDurationStyle)
        assertEquals(HomeWallpaperEffectMode.SOFT_BLUR, mapped.homeWallpaperEffectMode)
        assertEquals(0, mapped.gridColumnCount)
    }

    @Test
    fun emptyPreferences_produceHomeTopTabDefaults() {
        val mapped = mapHomeTopTabSettingsFromPreferences(mutablePreferencesOf())
        assertEquals(false, mapped.visibleIds.isEmpty())
        assertEquals(true, mapped.orderIds.contains("RECOMMEND"))
    }

    @Test
    fun emptyPreferences_producePlayerInteractionDefaults() {
        val mapped = mapPlayerInteractionSettingsFromPreferences(mutablePreferencesOf())
        assertEquals(1.0f, mapped.gestureSensitivity)
        assertEquals(true, mapped.doubleTapLikeEnabled)
        assertEquals(false, mapped.doubleTapSeekEnabled)
        assertEquals(10, mapped.seekForwardSeconds)
        assertEquals(10, mapped.seekBackwardSeconds)
    }

    @Test
    fun emptyPreferences_produceDanmakuDefaults() {
        val mapped = mapDanmakuSettingsFromPreferences(
            mutablePreferencesOf(),
            com.android.purebilibili.core.store.DanmakuSettingsScope.PORTRAIT
        )
        assertEquals(true, mapped.enabled)
        assertEquals(0.85f, mapped.opacity)
        assertEquals(1.0f, mapped.fontScale)
    }

    // ── 2. 旧 schema 可读（legacy 回退）──────────────────────────────

    @Test
    fun legacyThemeModeAmoled_readsAsDark() {
        // 旧版 `theme_mode = 3`（AMOLED）→ 新枚举 DARK
        assertEquals(AppThemeMode.DARK, resolveThemeModePreference(3))
        // 非法值 → FOLLOW_SYSTEM
        assertEquals(AppThemeMode.FOLLOW_SYSTEM, resolveThemeModePreference(999))
    }

    @Test
    fun legacyAmoledThemeMode_producesAmoledDarkStyle() {
        // dark_theme_style 缺失时，legacy theme_mode=3 → AMOLED
        assertEquals(
            DarkThemeStyle.AMOLED,
            resolveDarkThemeStylePreference(darkThemeStyleValue = null, legacyThemeModeValue = 3)
        )
        // legacy 无 AMOLED → DEFAULT
        assertEquals(
            DarkThemeStyle.DEFAULT,
            resolveDarkThemeStylePreference(darkThemeStyleValue = null, legacyThemeModeValue = null)
        )
    }

    @Test
    fun legacyHeaderBlurDisabled_readsThroughNewMode() {
        // 旧 key `header_blur_enabled=false`（无新 key）→ ALWAYS_OFF
        val mapped = mapHomeSettingsFromPreferences(
            mutablePreferencesOf(booleanPreferencesKey("header_blur_enabled") to false)
        )
        assertEquals(HomeHeaderBlurMode.ALWAYS_OFF, mapped.headerBlurMode)
        assertEquals(false, mapped.isHeaderBlurEnabled)
    }

    @Test
    fun legacyHeaderCollapseDisabled_readsAsOff() {
        val mapped = mapHomeSettingsFromPreferences(
            mutablePreferencesOf(booleanPreferencesKey("header_collapse_enabled") to false)
        )
        assertEquals(HomeHeaderCollapseMode.OFF, mapped.homeHeaderCollapseMode)
        assertEquals(false, mapped.isHeaderCollapseEnabled)
    }

    @Test
    fun legacyDurationBadgesHidden_readsAsHidden() {
        // 旧 key `home_video_duration_badges_visible=false` → HIDDEN
        val mapped = mapHomeSettingsFromPreferences(
            mutablePreferencesOf(booleanPreferencesKey("home_video_duration_badges_visible") to false)
        )
        assertEquals(HomeDurationStyle.HIDDEN, mapped.homeDurationStyle)
    }

    @Test
    fun scopedDanmakuKeyFallsBackToLegacyGlobalKey() {
        // 只有旧全局 key `danmaku_enabled=false`，无 scoped key → 回退全局
        val mapped = mapDanmakuSettingsFromPreferences(
            mutablePreferencesOf(booleanPreferencesKey("danmaku_enabled") to false),
            com.android.purebilibili.core.store.DanmakuSettingsScope.LANDSCAPE
        )
        assertEquals(false, mapped.enabled)
    }

    // ── 3. 编码格式冻结（字符串 schema 兼容）──────────────────────────

    @Test
    fun collectionSubscriptionCsvRoundTrip() {
        assertEquals(emptySet(), decodeCollectionSubscriptionIds(null))
        assertEquals(emptySet(), decodeCollectionSubscriptionIds(""))
        assertEquals(setOf("123", "456"), decodeCollectionSubscriptionIds("123, 456"))
        assertEquals("123,456", encodeCollectionSubscriptionIds(setOf("456", "123")))
    }

    @Test
    fun collectionSortPreferencesJsonRoundTrip() {
        assertEquals(emptyMap(), decodeCollectionSortPreferences(null))
        assertEquals(emptyMap(), decodeCollectionSortPreferences(""))
        // 旧格式 JSON：id → CollectionSortMode 枚举名
        val decoded = decodeCollectionSortPreferences("""{"123":"ASCENDING","456":"RECENT"}""")
        assertEquals(2, decoded.size)
        // 非法枚举名被忽略
        assertEquals(emptyMap(), decodeCollectionSortPreferences("""{"1":"NOT_A_MODE"}"""))
    }

    @Test
    fun preferenceValueClampingIsApplied() {
        // 越界存量值被夹取（旧数据可能存超界值）
        val mapped = mapPlayerInteractionSettingsFromPreferences(
            mutablePreferencesOf(floatPreferencesKey("gesture_sensitivity") to 99f)
        )
        assertEquals(2.0f, mapped.gestureSensitivity)
        val clampedSeek = mapPlayerInteractionSettingsFromPreferences(
            mutablePreferencesOf(intPreferencesKey("seek_forward_seconds") to 0)
        )
        assertEquals(1, clampedSeek.seekForwardSeconds)
    }
}
