package com.android.purebilibili.feature.video.screen

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.transition.VideoSharedTransitionMotionSpec

@OptIn(ExperimentalSharedTransitionApi::class)
internal data class VideoDetailTransitionState(
    val animatedVisibilityScope: AnimatedVisibilityScope?,
    val sharedTransitionScope: SharedTransitionScope?,
    val isExitTransitionInProgress: Boolean,
    val detailShellSharedBoundsEnabled: Boolean,
    val suppressEnterFadeAfterBackPreview: Boolean,
    val progress: State<Float>,
    val detailChildTransitionEnabled: Boolean,
    val coverSharedBoundsActive: Boolean,
    val sharedBoundsActive: Boolean,
    val routeSheetFrameProvider: () -> VideoDetailRouteSheetFrame,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
@Suppress("UNUSED_PARAMETER")
internal fun rememberVideoDetailTransitionState(
    bvid: String,
    sourceRoute: String?,
    transitionEnabled: Boolean,
    keepLoadedContentForBackPreview: Boolean,
    motionSpec: VideoSharedTransitionMotionSpec,
    routeSheetMotion: VideoDetailRouteSheetMotion,
): VideoDetailTransitionState {
    val staticProgress = remember { mutableFloatStateOf(1f) }
    return VideoDetailTransitionState(
        animatedVisibilityScope = null,
        sharedTransitionScope = null,
        isExitTransitionInProgress = false,
        detailShellSharedBoundsEnabled = false,
        suppressEnterFadeAfterBackPreview = false,
        progress = staticProgress,
        detailChildTransitionEnabled = false,
        coverSharedBoundsActive = false,
        sharedBoundsActive = false,
        routeSheetFrameProvider = {
            VideoDetailRouteSheetFrame(
                scale = 1f,
                translationYDp = 0f,
                cornerDp = 0f,
                backgroundScrimAlpha = 0f,
                settleProgress = 0f,
            )
        },
    )

}

@Composable
internal fun VideoDetailRouteSheetHost(
    frameProvider: () -> VideoDetailRouteSheetFrame,
    motion: VideoDetailRouteSheetMotion,
    isFullscreenMode: Boolean,
    backgroundColor: Color,
    backgroundAlpha: Float = 1f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
    overlayContent: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isFullscreenMode) Color.Black.copy(alpha = backgroundAlpha)
                else backgroundColor.copy(alpha = backgroundAlpha)
            ),
    ) {
        content()
        overlayContent()
    }
}
