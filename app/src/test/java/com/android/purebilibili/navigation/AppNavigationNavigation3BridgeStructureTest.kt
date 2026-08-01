package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppNavigationNavigation3BridgeStructureTest {
    @Test
    fun videoAndSettingsBacksUseTheNavigation3Dispatcher() {
        val source = appNavigationSource()

        assertTrue(source.contains("navigation3ProgrammaticBackDispatcher.dispatch()"))
        assertTrue(source.contains("val performSystemBackAction: () -> Unit"))
        assertTrue(source.contains("onBack = { commitSystemBackAction() }"))
        assertTrue(source.contains("isVisible = navigation3BackStack.lastOrNull() == videoKey"))
    }

    @Test
    fun navigationNoLongerCarriesVideoCardReturnState() {
        val source = appNavigationSource()
        val navigationRoot = File("src/main/java/com/android/purebilibili/navigation3")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .joinToString("\n") { it.readText() }

        assertFalse(source.contains("navigation3ReturnSession"))
        assertFalse(source.contains("shouldBindVideoDetailBackPreviewPlayer"))
        assertFalse(source.contains("predictiveBackCancelRecoveryGeneration"))
        assertFalse(navigationRoot.contains("BiliPaiNavSourceMetadata"))
        assertFalse(navigationRoot.contains("VideoCardTransitionSession"))
        assertFalse(navigationRoot.contains("CLASSIC_CARD"))
        assertTrue(navigationRoot.contains("BiliPaiNavRouteTransition.STACK"))
    }

    private fun appNavigationSource(): String = File(
        "src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"
    ).readText()
}
