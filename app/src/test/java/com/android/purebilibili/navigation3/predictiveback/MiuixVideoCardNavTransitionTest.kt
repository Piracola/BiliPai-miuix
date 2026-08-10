package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.test.Test
import kotlin.test.assertEquals

class MiuixVideoCardNavTransitionTest {
    @Test
    fun returnDepthClearsBlurInsteadOfReversingIt() {
        assertEquals(
            1f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = 0f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0.5f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -0.5f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -1f),
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun cardClipKeepsPhysicalCornerRadiusAcrossNonUniformScale() {
        val radii = resolveMiuixVideoCardClipRadii(
            sourceCornerPx = 12f,
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
        )

        assertEquals(12f, radii.radiusX * 0.5f, absoluteTolerance = 0.0001f)
        assertEquals(12f, radii.radiusY * 0.25f, absoluteTolerance = 0.0001f)
    }

    @Test
    fun wideCardHandsContentBackWithSourceChromeBeforeLanding() {
        val wideBounds = Rect(0f, 0f, 320f, 180f)

        assertEquals(
            1f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = wideBounds,
                morphProgress = 0f,
                isReturning = false,
            ),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0.5f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = wideBounds,
                morphProgress = 0.19f,
                isReturning = true,
            ),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            1f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = wideBounds,
                morphProgress = 0.32f,
                isReturning = true,
            ),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = wideBounds,
                morphProgress = 0.06f,
                isReturning = true,
            ),
            absoluteTolerance = 0.0001f,
        )
        // 预测返回手势 seek 中（未松手提交）：整层保持不透明，画面不提前消失。
        assertEquals(
            1f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = wideBounds,
                morphProgress = 0.06f,
                isReturning = true,
                isGestureSeeking = true,
            ),
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun verticalCardKeepsLiveDetailOpaqueDuringReturn() {
        assertEquals(
            1f,
            resolveMiuixVideoCardReturnContentAlpha(
                sourceBounds = Rect(0f, 0f, 180f, 280f),
                morphProgress = 0.05f,
                isReturning = true,
            ),
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun fillWidthTopPreservesAspectRatioAndTopAlignment() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
            contentScale = MiuixVideoCardContentScale.FillWidthTop,
        )

        assertEquals(0.5f, 0.5f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.5f, 0.25f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin(0.5f, 0f), compensation.transformOrigin)
    }

    @Test
    fun cropCenterPreservesAspectRatioUsingCoverScale() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.35f,
            outerScaleY = 0.6f,
            contentScale = MiuixVideoCardContentScale.CropCenter,
        )

        assertEquals(0.6f, 0.35f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.6f, 0.6f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin.Center, compensation.transformOrigin)
    }
}
