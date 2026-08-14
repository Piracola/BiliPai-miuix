package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertTrue

class AppSquircleModifiersTest {

    @Test
    fun squircleBackground_appliesOnSingleMiuixTheme() {
        assertTrue(shouldApplyMiuixSquircleBackground())
    }
}
