package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.theme.AppLanguage
import com.android.purebilibili.core.store.theme.AppThemeMode
import com.android.purebilibili.core.store.theme.DarkThemeStyle
import com.android.purebilibili.core.store.theme.ThemePreferenceState
import com.android.purebilibili.core.store.theme.resolveThemePreferenceState
import com.android.purebilibili.core.store.theme.resolveAppLanguagePreference
import com.android.purebilibili.core.store.theme.resolveThemeModePreference
import com.android.purebilibili.core.store.theme.resolveDarkThemeStylePreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemePreferencePolicyTest {

    @Test
    fun legacyAmoledThemeMode_migratesToDarkModeAndAmoledStyle() {
        val themeMode = resolveThemeModePreference(themeModeValue = 3)
        val darkThemeStyle = resolveDarkThemeStylePreference(
            darkThemeStyleValue = null,
            legacyThemeModeValue = 3
        )

        assertEquals(AppThemeMode.DARK, themeMode)
        assertEquals(DarkThemeStyle.AMOLED, darkThemeStyle)
    }

    @Test
    fun explicitDarkThemeStyle_preventsLegacyFallbackFromOverridingSelection() {
        val darkThemeStyle = resolveDarkThemeStylePreference(
            darkThemeStyleValue = DarkThemeStyle.DEFAULT.value,
            legacyThemeModeValue = 3
        )

        assertEquals(DarkThemeStyle.DEFAULT, darkThemeStyle)
    }

    @Test
    fun followSystem_withAmoledStyle_usesPureBlackOnlyWhenSystemIsDark() {
        val darkState = resolveThemePreferenceState(
            themeMode = AppThemeMode.FOLLOW_SYSTEM,
            darkThemeStyle = DarkThemeStyle.AMOLED,
            systemInDark = true
        )
        val lightState = resolveThemePreferenceState(
            themeMode = AppThemeMode.FOLLOW_SYSTEM,
            darkThemeStyle = DarkThemeStyle.AMOLED,
            systemInDark = false
        )

        assertTrue(darkState.useDarkTheme)
        assertTrue(darkState.useAmoledDarkTheme)
        assertFalse(lightState.useDarkTheme)
        assertFalse(lightState.useAmoledDarkTheme)
    }

    @Test
    fun lightMode_disablesAmoledEvenWhenAmoledStyleIsSelected() {
        val state = resolveThemePreferenceState(
            themeMode = AppThemeMode.LIGHT,
            darkThemeStyle = DarkThemeStyle.AMOLED,
            systemInDark = true
        )

        assertFalse(state.useDarkTheme)
        assertFalse(state.useAmoledDarkTheme)
    }
}
