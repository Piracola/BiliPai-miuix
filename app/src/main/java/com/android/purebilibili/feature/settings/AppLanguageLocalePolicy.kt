package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.theme.AppLanguage
import com.android.purebilibili.core.store.theme.AppThemeMode
import com.android.purebilibili.core.store.theme.DarkThemeStyle
import com.android.purebilibili.core.store.theme.persistAndApplyAppLanguageBeforeRestart
import com.android.purebilibili.core.store.theme.resolveAppLanguageLocaleTags
import com.android.purebilibili.core.store.theme.shouldPromptAppRestartForLanguageChange

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

internal fun resolveAppLanguageLocaleList(appLanguage: AppLanguage): LocaleListCompat {
    val languageTags = resolveAppLanguageLocaleTags(appLanguage)
    return if (languageTags.isEmpty()) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageTags.joinToString(","))
    }
}

internal fun applyAppLanguage(appLanguage: AppLanguage) {
    AppCompatDelegate.setApplicationLocales(resolveAppLanguageLocaleList(appLanguage))
}
