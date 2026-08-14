package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveScaffoldPolicyTest {

    @Test
    fun singleMiuixStyle_routesToMiuixScaffoldWithPopupHost() {
        assertEquals(
            AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST,
            resolveAdaptiveScaffoldRenderer()
        )
        assertTrue(
            shouldMountMiuixPopupHostOnAdaptiveScaffold()
        )
    }
}
