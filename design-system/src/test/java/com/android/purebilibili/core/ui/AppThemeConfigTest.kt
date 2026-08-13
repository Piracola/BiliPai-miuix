package com.android.purebilibili.core.ui

import com.android.purebilibili.core.ui.blur.BlurIntensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppThemeConfigTest {

    @Test
    fun `defaults keep standalone UI hosts functional`() {
        val config = AppThemeConfig()

        assertEquals(BlurIntensity.THIN, config.blurIntensity)
        assertTrue(config.hapticFeedbackEnabled)
        assertTrue(config.runtimeVisualGuardEnabled)
    }
}
