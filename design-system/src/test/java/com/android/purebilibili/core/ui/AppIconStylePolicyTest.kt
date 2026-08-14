package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppIconStylePolicyTest {

    @Test
    fun `auto style keeps miuix unchanged`() {
        // 单值 MIUIX 预设保持现状(设置图标多彩色等),不引入容器化/单色化
        assertEquals(
            AppIconStyle.AUTO,
            resolveAppIconStyle(AppIconStyle.AUTO),
        )
    }

    @Test
    fun `explicit style wins over runtime theme`() {
        assertEquals(
            AppIconStyle.MD3_STANDARD,
            resolveAppIconStyle(AppIconStyle.MD3_STANDARD),
        )
        assertEquals(
            AppIconStyle.THEME_CONTAINER,
            resolveAppIconStyle(AppIconStyle.THEME_CONTAINER),
        )
    }

    @Test
    fun `preference parsing falls back to auto`() {
        assertEquals(AppIconStyle.THEME_CONTAINER, resolveAppIconStylePreference("THEME_CONTAINER"))
        assertEquals(AppIconStyle.MD3_STANDARD, resolveAppIconStylePreference("MD3_STANDARD"))
        assertEquals(AppIconStyle.AUTO, resolveAppIconStylePreference(null))
        assertEquals(AppIconStyle.AUTO, resolveAppIconStylePreference("UNKNOWN_VALUE"))
    }

    @Test
    fun `md3 standard style forces material glyph family`() {
        val materialPolicy = AppSemanticVisualPolicy.material(
            AppSemanticAccentPalette(
                primary = androidx.compose.ui.graphics.Color(0xFF112233),
                secondary = androidx.compose.ui.graphics.Color(0xFF223344),
                tertiary = androidx.compose.ui.graphics.Color(0xFF334455),
                error = androidx.compose.ui.graphics.Color(0xFF445566),
            )
        ).copy(iconStyle = AppIconStyle.MD3_STANDARD)
        assertEquals(AppSemanticIconFamily.MATERIAL, materialPolicy.effectiveIconFamily)

        val themeContainerPolicy = materialPolicy.copy(iconStyle = AppIconStyle.THEME_CONTAINER)
        assertEquals(AppSemanticIconFamily.MATERIAL, themeContainerPolicy.effectiveIconFamily)
        assertEquals(AppSemanticIconFamily.MATERIAL, materialPolicy.iconFamily)
    }

    @Test
    fun `top chrome policy resolves from runtime theme with icon style`() {
        val material3Chrome = resolveAppTopChromePolicy(
            iconStyle = AppIconStyle.MD3_STANDARD,
        )
        assertEquals(AppSemanticIconFamily.MATERIAL, material3Chrome.effectiveIconFamily)
        assertEquals(AppIconStyle.MD3_STANDARD, material3Chrome.iconStyle)
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, material3Chrome.tabPresentation)

        val miuixChrome = resolveAppTopChromePolicy()
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, miuixChrome.tabPresentation)
        assertEquals(AppIconStyle.AUTO, miuixChrome.iconStyle)
    }
}
