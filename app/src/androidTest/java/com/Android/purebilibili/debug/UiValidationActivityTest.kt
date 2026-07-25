package com.android.purebilibili.debug

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiValidationActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<UiValidationActivity>()

    @Test
    fun validationScreenRendersAndSwitchesTheme() {
        composeRule.onNodeWithText("UI 验证台").assertIsDisplayed()
        composeRule.onNodeWithTag("theme_light").assertIsSelected()

        composeRule.onNodeWithTag("theme_dark").performClick().assertIsSelected()
    }
}
