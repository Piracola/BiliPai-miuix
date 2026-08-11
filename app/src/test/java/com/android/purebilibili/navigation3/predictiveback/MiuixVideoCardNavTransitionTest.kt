package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.ui.graphics.TransformOrigin
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class MiuixVideoCardNavTransitionTest {
    @Test
    fun transitionKeepsOneOpaqueFlyingCardWithoutStationaryRevealMask() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation3/predictiveback/MiuixVideoCardNavTransition.kt"
        ).readText()
        val transform = source.substringAfter("override fun Modifier.transformEntry")

        assertEquals(true, transform.contains("alpha = 1f"))
        assertEquals(false, transform.contains("visibleHeightFraction"))
        assertEquals(false, transform.contains("outgoingClipFraction"))
        assertEquals(false, transform.contains("alpha = resolveMiuixVideoCard"))
    }

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

    @Test
    fun sideBySideAndStackedUseFillWidthTopForLandingAnchors() {
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.CropCenter,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
                fullscreen = true,
            ),
        )
    }
}
