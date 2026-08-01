package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiDefaultPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiDisabledPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BiliPaiPredictiveBackAnimationPolicyTest {

    @Test
    fun enabledPredictiveBackAlwaysUsesPlatformDefaultHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.STACK,
            predictiveBackEnabled = true,
        )

        assertTrue(handler is BiliPaiDefaultPredictiveBackAnimation)
    }

    @Test
    fun disabledPredictiveBackDoesNotInstallPreviewHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.STACK,
            predictiveBackEnabled = false,
        )

        assertTrue(handler is BiliPaiDisabledPredictiveBackAnimation)
    }

    @Test
    fun policyDoesNotSelectProjectSpecificHandlers() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation3/predictiveback/" +
                "BiliPaiPredictiveBackAnimationPolicy.kt"
        ).readText()

        assertTrue(source.contains("return BiliPaiDefaultPredictiveBackAnimation()"))
        assertTrue(!source.contains("return BiliPaiSharedElementPredictiveBackAnimation()"))
        assertTrue(!source.contains("return BiliPaiSettingsIosPredictiveBackAnimation()"))
    }
}
