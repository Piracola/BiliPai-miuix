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
    fun appNavigationDoesNotApplyVideoTransitionBackgroundToRouteContent() {
        val source = appNavigationSource()

        assertFalse(source.contains("VideoCardTransitionBackgroundRouteContent("))
        assertFalse(source.contains("videoCardTransitionBackgroundEffect("))
        assertFalse(source.contains("predictiveBackBackgroundEffect("))
        assertTrue(source.contains("RenderNavigationContent(key)"))
    }

    @Test
    fun mainHostRendersWithoutVideoTransitionBackgroundLayer() {
        val source = appNavigationSource()
        val mainHostBranch = source
            .substringAfter("BiliPaiNavEntryContentRole.MAIN_HOST -> {")
            .substringBefore("BiliPaiNavEntryContentRole.HOME ->")

        assertFalse(mainHostBranch.contains("VideoCardTransitionBackgroundRouteContent("))
        assertFalse(source.contains("VideoCardTransitionBackgroundRouteContent("))
        assertTrue(source.substringAfter("BiliPaiNavDisplayHost(").contains("RenderNavigationContent(key)"))
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
    fun globalWallpaperDoesNotAttachVideoTransitionBlurOrSnapshot() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/home/HomeWallpaperBackdrop.kt"),
            File("src/main/java/com/android/purebilibili/feature/home/HomeWallpaperBackdrop.kt"),
        ).first { it.exists() }.readText()

        assertTrue(source.contains("GlobalHomeWallpaperBackdrop("))
        assertFalse(source.contains("videoCardTransitionBackgroundEffect("))
        assertFalse(source.contains("depthProgressProvider"))
        assertFalse(source.contains("rememberGraphicsLayer()"))
        assertFalse(source.contains("BlurEffect("))
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
