package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BiliPaiNavDisplayHostStructureTest {

    @Test
    fun navDisplayHost_keepsNavigation3StateAndBusinessBackCallbacks() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("rememberDecoratedNavEntries("))
        assertTrue(source.contains("rememberSaveableStateHolderNavEntryDecorator"))
        assertTrue(source.contains("rememberViewModelStoreNavEntryDecorator"))
        assertTrue(source.contains("rememberSceneState("))
        assertTrue(source.contains("rememberNavigationEventState(sceneState)"))
        assertTrue(source.contains("NavigationBackHandler("))
        assertTrue(source.contains("onBackCompleted = performBack"))
        assertFalse(source.contains("onNativeVideoBackCancelled"))
        assertTrue(source.contains("programmaticBackDispatcher.register(callback)"))
    }

    @Test
    fun navDisplayHost_usesStackTransformExceptTabletSettingsDetailPane() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("BiliPaiNavRouteTransition.STACK"))
        assertTrue(source.contains("LocalLayoutDirection.current"))
        assertTrue(source.contains("resolveBiliPaiNavContentTransform(fallbackRouteTransition, layoutDirection)"))
        assertTrue(source.contains("resolveBiliPaiNavPopContentTransform(fallbackRouteTransition, layoutDirection)"))
        assertTrue(source.contains("isTabletSettingsInternalTransition"))
        assertTrue(source.contains("settingsInternalTransform"))
        assertTrue(source.contains("if (isTabletSettingsInternalTransition) settingsInternalTransform else fallbackForwardTransform"))
        assertTrue(source.contains("if (isTabletSettingsInternalTransition) settingsInternalTransform else fallbackPopTransform"))
        assertTrue(source.contains("reportPredictiveProgress = predictiveBackEnabled"))
    }

    @Test
    fun navDisplayHost_doesNotCreateVideoTransitionVisualPipeline() {
        val source = navDisplayHostSource()

        listOf(
            "rememberVideoCardTransitionSnapshotHandle",
            "VideoCardTransitionHostDepthLayer",
            "VideoCardTransitionNavBackdrop",
            "LocalVideoCardTransitionClock provides",
            "LocalVideoCardMorphProgressReporter provides",
            "LocalVideoCardTransitionBackgroundState provides",
            "LocalPredictiveBackBackgroundState provides",
            "Animatable(",
            "animateFallbackTo(",
            "beginGestureRestore(",
            "resolvePredictiveBackGestureBlurProgress",
        ).forEach { forbidden ->
            assertFalse(source.contains(forbidden), "forbidden visual pipeline: $forbidden")
        }
    }

    @Test
    fun navDisplayHost_preservesApplicationExtrasForEntryViewModels() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("ProvideNavigation3ViewModelApplicationExtras("))
        assertTrue(source.contains("LocalViewModelStoreOwner provides patchedOwner"))
        assertTrue(source.contains("APPLICATION_KEY"))
    }

    private fun navDisplayHostSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
            File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
        ).first { it.exists() }.readText()
    }
}
