package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.ui.AppTopTabPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TopTabStylePolicyTest {

    @Test
    fun `tonal capsule spec keeps compact floating geometry`() {
        val spec = resolveMd3TopTabVisualSpec(
            isFloatingStyle = true,
            presentation = AppTopTabPresentation.TONAL_CAPSULE,
            labelMode = 2,
        )

        assertEquals(40, spec.rowHeight.value.toInt())
        assertEquals(30, spec.selectedCapsuleHeight.value.toInt())
        assertEquals(15, spec.labelTextSize.value.toInt())
        assertEquals(20, spec.labelLineHeight.value.toInt())
    }

    @Test
    fun `docked capsule spec stays compact for the home dock`() {
        val spec = resolveMd3TopTabVisualSpec(
            isFloatingStyle = false,
            presentation = AppTopTabPresentation.TONAL_CAPSULE,
            labelMode = 2,
        )

        assertEquals(36, spec.rowHeight.value.toInt())
        assertEquals(30, spec.selectedCapsuleHeight.value.toInt())
    }

    @Test
    fun `icon label mode keeps capsule size while reserving icon gap`() {
        val floating = resolveMd3TopTabVisualSpec(
            isFloatingStyle = true,
            presentation = AppTopTabPresentation.TONAL_CAPSULE,
            labelMode = 0,
        )

        assertTrue(floating.iconLabelSpacing.value.toInt() > 0)
        assertEquals(18, floating.iconSize.value.toInt())
    }
}
