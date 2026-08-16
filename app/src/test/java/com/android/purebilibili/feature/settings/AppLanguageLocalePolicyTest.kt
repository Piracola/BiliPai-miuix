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
import kotlin.test.assertTrue

class AppLanguageLocalePolicyTest {

    @Test
    fun followSystem_resolvesToEmptyLocaleList() {
        val locales = resolveAppLanguageLocaleList(AppLanguage.FOLLOW_SYSTEM)

        assertTrue(locales.isEmpty)
        assertEquals("", locales.toLanguageTags())
    }

    @Test
    fun explicitLanguages_resolveToExpectedLocaleList() {
        assertEquals(
            "zh-CN",
            resolveAppLanguageLocaleList(AppLanguage.SIMPLIFIED_CHINESE).toLanguageTags()
        )
        assertEquals(
            "zh-TW",
            resolveAppLanguageLocaleList(AppLanguage.TRADITIONAL_CHINESE_TAIWAN).toLanguageTags()
        )
        assertEquals(
            "en",
            resolveAppLanguageLocaleList(AppLanguage.ENGLISH).toLanguageTags()
        )
    }
}
