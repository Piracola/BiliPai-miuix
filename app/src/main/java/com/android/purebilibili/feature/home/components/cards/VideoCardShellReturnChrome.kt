package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.AppSpacingTokens

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.util.CardPositionManager

/**
 * 源卡信息区（标题/UP 等）在 shell morph 时的 chrome 视觉。
 * 返回末段在封面像素交接前开始淡入，并在落位前完成，避免收尾只剩封面；
 * 横卡可选择随主进度短距离移动；快速返回若仍保留 live surface 也走同一交接。
 * 所有进度都在绘制阶段读取，避免整卡重组。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.videoCardShellReturnChromeAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
    isQuickReturnFromDetail: Boolean = false,
    followShellMotion: Boolean = false,
): Modifier {
    if (!enabled || bvid.isBlank()) return this
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val bgState = LocalVideoCardTransitionBackgroundState.current
    val followDistancePx = with(LocalDensity.current) { AppSpacingTokens.Small.toPx() }
    val isSharedMorphSourceCard = remember(
        bvid,
        sourceRoute,
        CardPositionManager.lastClickedVideoSourceKey,
    ) {
        isVideoCardSharedReturnTarget(
            bvid = bvid,
            sourceRoute = sourceRoute,
            lastClickedVideoSourceKey = CardPositionManager.lastClickedVideoSourceKey,
        )
    }
    return graphicsLayer {
        val phase = bgState.phaseProvider()
        val returnGestureInProgress = bgState.isReturnGestureInProgressProvider()
        val transitionActive = sharedTransitionScope?.isTransitionActive == true
        val progress = bgState.progressProvider()
        val quickReturn = isQuickReturnFromDetail ||
            bgState.isQuickReturnFromDetailProvider()
        val preferWholeCardReturn = bgState.preferWholeCardReturnProvider()
        if (followShellMotion) {
            val frame = resolveHorizontalCardChromeMotionFrame(
                useCardContainerSharedBounds = enabled,
                isSharedMorphSourceCard = isSharedMorphSourceCard,
                isReturningFromDetail = isReturningFromDetail,
                transitionBackgroundPhase = phase,
                isVideoCardReturnGestureInProgress = returnGestureInProgress,
                isSharedTransitionActive = transitionActive,
                transitionBackgroundProgress = progress,
                isQuickReturnFromDetail = quickReturn,
                preferWholeCardReturn = preferWholeCardReturn,
            )
            alpha = frame.alpha
            translationY = if (bgState.motionTierProvider() == MotionTier.Reduced) {
                0f
            } else {
                -followDistancePx * frame.translationProgress
            }
        } else {
            alpha = resolveHomeCardChromeAlphaDuringShellReturnMorph(
                useCardContainerSharedBounds = enabled,
                isSharedMorphSourceCard = isSharedMorphSourceCard,
                isReturningFromDetail = isReturningFromDetail,
                transitionBackgroundPhase = phase,
                isVideoCardReturnGestureInProgress = returnGestureInProgress,
                isSharedTransitionActive = transitionActive,
                transitionBackgroundProgress = progress,
                isQuickReturnFromDetail = quickReturn,
                preferWholeCardReturn = preferWholeCardReturn,
            )
            translationY = 0f
        }
    }
}

/**
 * 保留来源卡封面资源，但让其像素与详情 live surface 使用同一返回交接窗口。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.videoCardShellReturnCoverAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
): Modifier {
    if (!enabled || bvid.isBlank()) return this
    val bgState = LocalVideoCardTransitionBackgroundState.current
    val isSharedMorphSourceCard = remember(
        bvid,
        sourceRoute,
        CardPositionManager.lastClickedVideoSourceKey,
    ) {
        isVideoCardSharedReturnTarget(
            bvid = bvid,
            sourceRoute = sourceRoute,
            lastClickedVideoSourceKey = CardPositionManager.lastClickedVideoSourceKey,
        )
    }
    return graphicsLayer {
        alpha = resolveHomeCardReturnSourceVisualAlpha(
            useCardContainerSharedBounds = enabled,
            isSharedMorphSourceCard = isSharedMorphSourceCard,
            isReturningFromDetail = isReturningFromDetail,
            transitionBackgroundPhase = bgState.phaseProvider(),
            isVideoCardReturnGestureInProgress = bgState.isReturnGestureInProgressProvider(),
            transitionBackgroundProgress = bgState.progressProvider(),
            // 快速返回仍可能是 LIVE surface；只有显式整卡回退才允许提前全显。
            preferWholeCardReturn = bgState.preferWholeCardReturnProvider(),
        )
    }
}
