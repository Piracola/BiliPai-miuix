package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation.AppSystemBackAction
import com.android.purebilibili.navigation.shouldInterceptSystemBackForAppAction

/** The only page-stack visual modes supported by Navigation3. */
internal enum class BiliPaiNavRouteTransition {
    STACK,
    REDUCED_MOTION,
}

internal data class BiliPaiBackGestureDecision(
    val routeTransition: BiliPaiNavRouteTransition,
    val interceptSystemBack: Boolean,
)

internal fun resolveBiliPaiBackGestureDecision(
    systemBackAction: AppSystemBackAction,
): BiliPaiBackGestureDecision = BiliPaiBackGestureDecision(
    routeTransition = BiliPaiNavRouteTransition.STACK,
    interceptSystemBack = shouldInterceptSystemBackForAppAction(systemBackAction),
)
