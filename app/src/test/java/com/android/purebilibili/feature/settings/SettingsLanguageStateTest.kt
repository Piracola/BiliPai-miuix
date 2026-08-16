package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.theme.persistAndApplyAppLanguageBeforeRestart
import com.android.purebilibili.core.store.theme.resolveAppLanguageLocaleTags
import com.android.purebilibili.core.store.theme.shouldPromptAppRestartForLanguageChange

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

class SettingsLanguageStateTest {

    @Test
    fun settingsUiState_defaultsToFollowSystemLanguage() {
        assertEquals(
            AppLanguage.FOLLOW_SYSTEM,
            SettingsUiState().appLanguage
        )
    }

    @Test
    fun settingsUiState_preservesExplicitLanguageSelection() {
        val state = SettingsUiState(appLanguage = AppLanguage.ENGLISH)

        assertEquals(AppLanguage.ENGLISH, state.appLanguage)
    }
}
