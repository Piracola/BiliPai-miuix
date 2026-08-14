package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class AppTopChromePolicyTest {
    @Test
    fun singleMiuixStyleMapsToSemanticTopChromeTreatment() {
        val policy = resolveAppTopChromePolicy()

        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, policy.tabPresentation)
        assertEquals(AppSemanticIconFamily.MATERIAL, policy.iconFamily)
    }
}
