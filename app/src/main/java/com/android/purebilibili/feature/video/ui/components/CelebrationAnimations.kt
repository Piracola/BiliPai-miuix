package com.android.purebilibili.feature.video.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * 点赞、投币和三连的业务反馈由调用方的状态与 haptic 承担。
 * 这里仅消费一次性反馈事件，不再叠加粒子、缩放或补间动画。
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun LikeBurstAnimation(
    visible: Boolean,
    reducedMotion: Boolean = false,
    onAnimationEnd: () -> Unit = {},
) {
    if (visible) {
        LaunchedEffect(visible) { onAnimationEnd() }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun TripleSuccessAnimation(
    visible: Boolean,
    isCompact: Boolean = false,
    reducedMotion: Boolean = false,
    onAnimationEnd: () -> Unit = {},
) {
    if (visible) {
        LaunchedEffect(visible) { onAnimationEnd() }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun CoinSuccessAnimation(
    visible: Boolean,
    coinCount: Int = 1,
    onAnimationEnd: () -> Unit = {},
) {
    if (visible) {
        LaunchedEffect(visible) { onAnimationEnd() }
    }
}
