package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppAdaptiveSwitchPolicyTest {

    @Test
    fun `material3 style uses material switch treatment`() {
        assertEquals(
            AppAdaptiveSwitchTreatment.MATERIAL,
            resolveAppAdaptiveSwitchTreatment(uiStyle = AppUiStyle.MATERIAL3)
        )
    }

    @Test
    fun `miuix style uses miuix switch treatment`() {
        assertEquals(
            AppAdaptiveSwitchTreatment.MIUIX,
            resolveAppAdaptiveSwitchTreatment(uiStyle = AppUiStyle.MIUIX)
        )
    }
}
