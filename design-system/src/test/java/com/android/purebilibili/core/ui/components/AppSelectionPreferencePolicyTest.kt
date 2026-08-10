package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppSelectionPreferencePolicyTest {

    @Test
    fun `material3 uses list item dialog even when popup is the stored default`() {
        assertEquals(
            AppSingleChoiceRenderer.LIST_ITEM_DIALOG,
            resolveAppSingleChoiceRenderer(
                uiStyle = AppUiStyle.MATERIAL3,
                presentation = AppSingleChoicePresentation.WINDOW_POPUP,
            ),
        )
    }

    @Test
    fun `miuix keeps native window popup by default`() {
        assertEquals(
            AppSingleChoiceRenderer.MIUIX_WINDOW_POPUP,
            resolveAppSingleChoiceRenderer(
                uiStyle = AppUiStyle.MIUIX,
                presentation = AppSingleChoicePresentation.WINDOW_POPUP,
            ),
        )
    }

    @Test
    fun `centered dialog override remains available in both themes`() {
        listOf(AppUiStyle.MIUIX, AppUiStyle.MATERIAL3).forEach { uiStyle ->
            assertEquals(
                AppSingleChoiceRenderer.LIST_ITEM_DIALOG,
                resolveAppSingleChoiceRenderer(
                    uiStyle = uiStyle,
                    presentation = AppSingleChoicePresentation.CENTERED_DIALOG,
                ),
            )
        }
    }
}
