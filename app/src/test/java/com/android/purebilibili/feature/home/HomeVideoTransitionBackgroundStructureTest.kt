package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeVideoTransitionBackgroundStructureTest {

    @Test
    fun homeRootNoLongerOwnsVideoTransitionBackgroundBlurAndScrim() {
        val source = homeScreenSource()

        assertFalse(source.contains("homeVideoTransitionBackgroundProgress"))
        assertFalse(source.contains("HomeVideoTransitionBackgroundPhase"))
        assertFalse(source.contains("homeVideoTransitionBackgroundEffect"))
    }

    @Test
    fun navigationHostDoesNotProvideVideoTransitionBackgroundState() {
        val source = navDisplayHostSource()

        assertTrue(source.contains("NavDisplay("))
        assertFalse(source.contains("VideoCardTransitionBackgroundPhase"))
        assertFalse(source.contains("LocalVideoCardTransitionBackgroundState provides"))
        assertFalse(source.contains("resolveVideoCardTransitionMotionTier"))
        assertFalse(source.contains("videoCardTransitionBackgroundEffect("))
    }

    @Test
    fun appNavigationAppliesVideoTransitionBackgroundToRealRouteContent() {
        val source = appNavigationSource()

        assertTrue(source.contains("VideoCardTransitionBackgroundRouteContent("))
        assertTrue(source.contains("videoCardTransitionBackgroundEffect("))
        assertTrue(source.contains("realtimeBlurEnabledProvider"))
        assertTrue(source.contains("shouldApplyVideoCardTransitionBackgroundToRoute("))
        assertTrue(source.contains("RenderNavigationContent(key)"))
        assertTrue(source.contains("sourceRoute = backgroundState.sourceRouteProvider()"))
        assertFalse(source.contains("val predictiveBlurProgress = predictiveBackState.progressProvider()"))
    }

    @Test
    fun mainHostOwnsExactlyOneVideoTransitionBackgroundLayer() {
        val source = appNavigationSource()
        val mainHostBranch = source
            .substringAfter("BiliPaiNavEntryContentRole.MAIN_HOST -> {")
            .substringBefore("BiliPaiNavEntryContentRole.HOME ->")

        assertFalse(
            mainHostBranch.contains("VideoCardTransitionBackgroundRouteContent("),
            "MainHost Pager must not recursively record the root snapshot GraphicsLayer",
        )
        assertTrue(
            source.substringAfter("BiliPaiNavDisplayHost(")
                .contains("VideoCardTransitionBackgroundRouteContent(key)"),
            "NavDisplay entry shell must remain the single transition background owner",
        )
        assertTrue(
            source.contains("val activeMainHostRoute = currentBottomNavItem.route"),
            "MainHost transition matching must retain the selected pager route while VideoDetail is top-most",
        )
        assertFalse(
            source.contains("activeMainHostRoute = activeBottomTabRoute"),
            "The top-most video route cannot be reused as the retained MainHost page identity",
        )
    }

    @Test
    fun videoCardTransitionBackgroundUsesFrozenSnapshotLayerForDynamicBlur() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/transition/VideoCardTransitionBackgroundPolicy.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/transition/VideoCardTransitionBackgroundPolicy.kt"),
        ).first { it.exists() }.readText()

        assertTrue(source.contains("rememberGraphicsLayer()"))
        assertTrue(source.contains("shouldUseVideoCardTransitionSnapshotBlur"))
        assertTrue(source.contains("freezeRecording"))
        assertTrue(source.contains("contentLayer.record"))
        assertTrue(source.contains("BlurEffect("))
        assertTrue(source.contains("DisposableEffect(snapshotState, contentLayer, isHostOwnedSnapshot)"))
        assertTrue(source.contains("shouldInvalidateSnapshotOnSourceDispose"))
        assertTrue(source.contains("snapshotState.invalidateRecordedContent()"))
        // 冻结后不得每帧对 live content 再挂 android RenderEffect
        assertFalse(source.contains("android.graphics.RenderEffect"))
        assertFalse(source.contains("createBlurEffect("))
    }

    @Test
    fun navDisplayHostDoesNotOwnSessionDepthLayer() {
        val source = navDisplayHostSource()
        assertFalse(source.contains("VideoCardTransitionHostDepthLayer("))
        assertFalse(source.contains("shouldReleaseHostOwnedDepthLayer("))
        assertFalse(source.contains("videoCardSnapshotHandle"))
    }

    private fun homeScreenSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        ).first { it.exists() }.readText()
    }

    private fun navDisplayHostSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
            File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt")
        ).first { it.exists() }.readText()
    }

    private fun appNavigationSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"),
            File("src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")
        ).first { it.exists() }.readText()
    }
}
