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
        assertTrue(source.contains("onNativeVideoBackCancelled(currentBackKey, targetBackKey)"))
        assertTrue(source.contains("programmaticBackDispatcher.register(callback)"))
    }

    @Test
    fun navDisplayHost_usesNoOpTransformsForEveryNavigationPath() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("transitionSpec = { EnterTransition.None togetherWith ExitTransition.None }"))
        assertTrue(source.contains("popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None }"))
        assertTrue(source.contains("predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None }"))
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
