package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec

import androidx.navigationevent.NavigationEventTransitionState
import com.android.purebilibili.navigation3.BiliPaiNavKey

internal class BiliPaiDefaultPredictiveBackAnimation : BiliPaiPredictiveBackAnimationHandler {
    override suspend fun onBackPressed(
        transitionState: NavigationEventTransitionState?,
        currentPageKey: BiliPaiNavKey?,
    ) = Unit

    @Composable
    override fun Modifier.predictiveBackAnimationDecorator(
        transitionState: NavigationEventTransitionState?,
        contentPageKey: Any,
        currentPageKey: BiliPaiNavKey?,
    ): Modifier = this

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onPredictivePopTransitionSpec(
        swipeEdge: Int,
    ): ContentTransform = EnterTransition.None togetherWith ExitTransition.None

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onPopTransitionSpec(): ContentTransform =
        EnterTransition.None togetherWith ExitTransition.None

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onTransitionSpec(): ContentTransform =
        EnterTransition.None togetherWith ExitTransition.None
}
