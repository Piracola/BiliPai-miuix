package com.android.purebilibili.navigation3

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith

/**
 * Navigation is deliberately a hard cut. Keeping the policy here means legacy route metadata
 * can be retired incrementally without giving individual destinations a way to add motion.
 */
internal fun resolveBiliPaiNavContentTransform(
    routeTransition: BiliPaiNavRouteTransition
): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

internal fun resolveBiliPaiNavPopContentTransform(
    routeTransition: BiliPaiNavRouteTransition
): ContentTransform = EnterTransition.None togetherWith ExitTransition.None
