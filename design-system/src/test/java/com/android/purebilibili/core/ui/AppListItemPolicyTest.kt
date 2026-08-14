package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppListItemPolicyTest {

    @Test
    fun `auto style follows runtime theme`() {
        // 单值 MIUIX 默认原生 Miuix 条目(现状)
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveAppListItemStyle(AppListItemStyle.AUTO),
        )
    }

    @Test
    fun `explicit style applies`() {
        assertEquals(
            AppListItemStyle.NATIVE,
            resolveAppListItemStyle(AppListItemStyle.NATIVE),
        )
        assertEquals(
            AppListItemStyle.CUSTOM,
            resolveAppListItemStyle(AppListItemStyle.CUSTOM),
        )
    }

    @Test
    fun `preference parsing falls back to auto`() {
        assertEquals(AppListItemStyle.CUSTOM, resolveAppListItemStylePreference("CUSTOM"))
        assertEquals(AppListItemStyle.NATIVE, resolveAppListItemStylePreference("NATIVE"))
        assertEquals(AppListItemStyle.AUTO, resolveAppListItemStylePreference(null))
        assertEquals(AppListItemStyle.AUTO, resolveAppListItemStylePreference("UNKNOWN_VALUE"))
    }
}
