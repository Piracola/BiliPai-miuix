package com.android.purebilibili.navigation3

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

/**
 * Navigation3 页面栈的唯一转场入口。
 *
 * 前进与返回都只使用平移：详情页不再尝试回到某张信息流卡片，也不叠加缩放、
 * 模糊或透明度，避免播放器和信息流同时执行多层动画。
 */
private const val NAVIGATION_PUSH_DURATION_MILLIS = 280
private const val NAVIGATION_POP_DURATION_MILLIS = 240
private const val NAVIGATION_BACKGROUND_PARALLAX_RATIO = 0.2f

internal fun resolveBiliPaiNavContentTransform(
    routeTransition: BiliPaiNavRouteTransition,
    layoutDirection: LayoutDirection,
): ContentTransform {
    if (routeTransition == BiliPaiNavRouteTransition.REDUCED_MOTION) {
        return EnterTransition.None togetherWith ExitTransition.None
    }

    val direction = layoutDirection.navigationDirectionSign()
    return (slideInHorizontally(
        animationSpec = tween(NAVIGATION_PUSH_DURATION_MILLIS, easing = FastOutSlowInEasing),
        initialOffsetX = { width -> width * direction },
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(NAVIGATION_PUSH_DURATION_MILLIS, easing = FastOutSlowInEasing),
        targetOffsetX = { width ->
            (-width * NAVIGATION_BACKGROUND_PARALLAX_RATIO * direction).roundToInt()
        },
    )).apply {
        // Push 时新页面必须位于旧页面之上，避免旧页面背景穿透到新页面内容中。
        targetContentZIndex = 1f
    }
}

internal fun resolveBiliPaiNavPopContentTransform(
    routeTransition: BiliPaiNavRouteTransition,
    layoutDirection: LayoutDirection,
): ContentTransform {
    if (routeTransition == BiliPaiNavRouteTransition.REDUCED_MOTION) {
        return EnterTransition.None togetherWith ExitTransition.None
    }

    val direction = layoutDirection.navigationDirectionSign()
    return (slideInHorizontally(
        animationSpec = tween(NAVIGATION_POP_DURATION_MILLIS, easing = FastOutSlowInEasing),
        initialOffsetX = { width ->
            (-width * NAVIGATION_BACKGROUND_PARALLAX_RATIO * direction).roundToInt()
        },
    ) togetherWith slideOutHorizontally(
        animationSpec = tween(NAVIGATION_POP_DURATION_MILLIS, easing = FastOutSlowInEasing),
        targetOffsetX = { width -> width * direction },
    )).apply {
        // Pop 时退出中的详情/设置页必须留在上层，下面的目标页只从左侧露出。
        targetContentZIndex = -1f
    }
}

private fun LayoutDirection.navigationDirectionSign(): Int {
    return if (this == LayoutDirection.Ltr) 1 else -1
}
