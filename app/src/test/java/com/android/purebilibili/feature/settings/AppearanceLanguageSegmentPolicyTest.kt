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

class AppearanceLanguageSegmentPolicyTest {

    @Test
    fun languageSegmentOptions_exposeAllLanguageModesInStableOrder_andUseProvidedLabels() {
        val options = resolveAppLanguageSegmentOptions(
            followSystemLabel = "Follow System",
            simplifiedChineseLabel = "Simplified",
            traditionalChineseLabel = "Traditional",
            englishLabel = "English"
        )

        assertEquals(
            listOf(
                AppLanguage.FOLLOW_SYSTEM,
                AppLanguage.SIMPLIFIED_CHINESE,
                AppLanguage.TRADITIONAL_CHINESE_TAIWAN,
                AppLanguage.ENGLISH
            ),
            options.map { it.value }
        )
        assertEquals(
            listOf("Follow System", "Simplified", "Traditional", "English"),
            options.map { it.label }
        )
    }
}
