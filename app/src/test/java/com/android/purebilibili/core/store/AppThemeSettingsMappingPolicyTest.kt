package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.android.purebilibili.core.ui.AppIconStyle
import com.android.purebilibili.core.ui.AppListItemStyle
import com.android.purebilibili.core.theme.AppFontSizePreset
import com.android.purebilibili.core.theme.AppUiScalePreset
import com.android.purebilibili.feature.screenshot.AppScreenshotCaptureMode
import com.android.purebilibili.feature.screenshot.AppScreenshotGestureMode
import com.android.purebilibili.feature.settings.AppLanguage
import com.android.purebilibili.feature.settings.AppThemeMode
import com.android.purebilibili.feature.settings.DarkThemeStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppThemeSettingsMappingPolicyTest {

    @Test
    fun emptyPreferences_useStartupThemeDefaults() {
        val result = mapAppThemeSettingsFromPreferences(mutablePreferencesOf())

        assertEquals(AppThemeMode.FOLLOW_SYSTEM, result.themeMode)
        assertEquals(DarkThemeStyle.DEFAULT, result.darkThemeStyle)
        assertEquals(AppLanguage.FOLLOW_SYSTEM, result.appLanguage)
        assertEquals(AppFontSizePreset.DEFAULT, result.appFontSizePreset)
        assertEquals("", result.appFontFileName)
        assertEquals(AppUiScalePreset.STANDARD, result.appUiScalePreset)
        assertEquals(0, result.appDpiOverridePercent)
        assertFalse(result.appGestureScreenshotEnabled)
        assertEquals(
            AppScreenshotGestureMode.TOP_RIGHT_TWO_FINGER_LONG_PRESS,
            result.appScreenshotGestureMode
        )
        assertEquals(AppScreenshotCaptureMode.FULL_WINDOW, result.appScreenshotCaptureMode)
        assertEquals(AppIconStyle.AUTO, result.appIconStyle)
        assertEquals(AppListItemStyle.AUTO, result.appListItemStyle)
    }

    @Test
    fun preferences_mapAllStartupThemeFieldsFromOneSnapshot() {
        val result = mapAppThemeSettingsFromPreferences(
            mutablePreferencesOf(
                intPreferencesKey("theme_mode_v2") to AppThemeMode.DARK.value,
                intPreferencesKey("dark_theme_style_v1") to DarkThemeStyle.AMOLED.value,
                intPreferencesKey("app_language_v1") to AppLanguage.ENGLISH.value,
                intPreferencesKey("app_font_size_preset") to AppFontSizePreset.LARGE.value,
                stringPreferencesKey("app_font_file_name") to "demo.ttf",
                intPreferencesKey("app_ui_scale_preset") to AppUiScalePreset.LARGE.value,
                intPreferencesKey("app_dpi_override_percent") to 200,
                booleanPreferencesKey("app_gesture_screenshot_enabled") to true,
                intPreferencesKey("app_screenshot_gesture_mode") to
                    AppScreenshotGestureMode.THREE_FINGER_SWIPE_DOWN.value,
                intPreferencesKey("app_screenshot_capture_mode") to
                    AppScreenshotCaptureMode.SELECT_REGION.value,
                stringPreferencesKey("app_icon_style") to AppIconStyle.THEME_CONTAINER.name,
                stringPreferencesKey("app_list_item_style") to AppListItemStyle.NATIVE.name
            )
        )

        assertEquals(AppThemeMode.DARK, result.themeMode)
        assertEquals(DarkThemeStyle.AMOLED, result.darkThemeStyle)
        assertEquals(AppLanguage.ENGLISH, result.appLanguage)
        assertEquals(AppFontSizePreset.LARGE, result.appFontSizePreset)
        assertEquals("demo.ttf", result.appFontFileName)
        assertEquals(AppUiScalePreset.LARGE, result.appUiScalePreset)
        assertEquals(115, result.appDpiOverridePercent)
        assertEquals(AppIconStyle.THEME_CONTAINER, result.appIconStyle)
        assertEquals(AppListItemStyle.NATIVE, result.appListItemStyle)
        assertEquals(true, result.appGestureScreenshotEnabled)
        assertEquals(AppScreenshotGestureMode.THREE_FINGER_SWIPE_DOWN, result.appScreenshotGestureMode)
        assertEquals(AppScreenshotCaptureMode.SELECT_REGION, result.appScreenshotCaptureMode)
    }
}
