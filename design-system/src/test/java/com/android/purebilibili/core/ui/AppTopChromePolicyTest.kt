package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppTopChromePolicyTest {
    @Test
    fun singleMiuixStyleMapsToSemanticTopChromeTreatment() {
        val policy = resolveAppTopChromePolicy()

        assertEquals(AppTopTabPresentation.MOVING_CAPSULE, policy.tabPresentation)
        assertEquals(AppSemanticIconFamily.MIUIX, policy.iconFamily)
    }
}
