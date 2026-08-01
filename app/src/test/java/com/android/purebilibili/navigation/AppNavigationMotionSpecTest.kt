package com.android.purebilibili.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigationMotionSpecTest {

    @Test
    fun `shared navigation chrome uses a single timing spec`() {
        val spec = resolveAppNavigationMotionSpec()

        assertEquals(120, spec.fastFadeDurationMillis)
        assertEquals(190, spec.slowFadeDurationMillis)
    }
}
