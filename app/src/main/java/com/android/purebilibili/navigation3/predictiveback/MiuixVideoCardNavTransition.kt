package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope

internal enum class MiuixVideoCardContentScale {
    /** Home dual-column / stacked cards: media fills card width, pinned to top. */
    FillWidthTop,
    /** Fullscreen, side-by-side, or square-ish morphs: cover scale about center. */
    CropCenter,
}

/**
 * Pick entry content compensation from the frozen source layout.
 * Side-by-side related/home single-column rows avoid FillWidthTop's vertical stretch
 * (which reads as "whole page crushed into a thin strip").
 */
internal fun resolveMiuixVideoCardContentScaleForSourceLayout(
    sourceLayout: VideoCardSourceLayout,
    fullscreen: Boolean = false,
): MiuixVideoCardContentScale {
    if (fullscreen) return MiuixVideoCardContentScale.CropCenter
    // FillWidthTop for both STACKED and SIDE_BY_SIDE so inverse-scale landing
    // (1/sourceScale) + outer non-uniform scale maps to the frozen card bounds.
    // CropCenter shifts top-left landing anchors and turns horizontal cards into black strips.
    return when (sourceLayout) {
        VideoCardSourceLayout.SIDE_BY_SIDE,
        VideoCardSourceLayout.STACKED,
        VideoCardSourceLayout.COVER_ONLY,
        -> MiuixVideoCardContentScale.FillWidthTop
    }
}

internal data class MiuixVideoCardContentCompensation(
    val scaleX: Float,
    val scaleY: Float,
    val transformOrigin: TransformOrigin,
)

internal data class MiuixVideoCardClipRadii(
    val radiusX: Float,
    val radiusY: Float,
)

/** Top entry depth is 0 at rest and moves toward -1 while returning. */
internal fun resolveMiuixVideoCardDepthProgress(relativeDepth: Float): Float =
    topProgress(relativeDepth)

/**
 * Keeps the corner circular in screen space while the outer card layer scales non-uniformly.
 * A regular RoundedCornerShape is scaled together with the layer and becomes too small on the
 * compressed axis, which exposes the retained source card near the end of a card return.
 */
internal fun resolveMiuixVideoCardClipRadii(
    sourceCornerPx: Float,
    outerScaleX: Float,
    outerScaleY: Float,
): MiuixVideoCardClipRadii {
    val physicalRadius = sourceCornerPx.coerceAtLeast(0f)
    return MiuixVideoCardClipRadii(
        radiusX = physicalRadius / outerScaleX.coerceAtLeast(0.01f),
        radiusY = physicalRadius / outerScaleY.coerceAtLeast(0.01f),
    )
}

private data class MiuixVideoCardClipShape(
    val radiusX: Float,
    val radiusY: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Rounded(
            RoundRect(
                // The flying entry remains a complete opaque card. Player → cover and the
                // detail-owned source-card chrome are transformed inside this moving boundary.
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = CornerRadius(
                    x = radiusX.coerceIn(0f, size.width / 2f),
                    y = radiusY.coerceIn(0f, size.height / 2f),
                ),
            ),
        )
    }
}

internal fun resolveMiuixVideoCardContentCompensation(
    outerScaleX: Float,
    outerScaleY: Float,
    contentScale: MiuixVideoCardContentScale,
): MiuixVideoCardContentCompensation {
    val safeOuterScaleX = outerScaleX.coerceAtLeast(0.01f)
    val safeOuterScaleY = outerScaleY.coerceAtLeast(0.01f)
    val uniformScale = when (contentScale) {
        MiuixVideoCardContentScale.FillWidthTop -> safeOuterScaleX
        MiuixVideoCardContentScale.CropCenter -> maxOf(safeOuterScaleX, safeOuterScaleY)
    }
    return MiuixVideoCardContentCompensation(
        scaleX = uniformScale / safeOuterScaleX,
        scaleY = uniformScale / safeOuterScaleY,
        transformOrigin = when (contentScale) {
            MiuixVideoCardContentScale.FillWidthTop -> TransformOrigin(0.5f, 0f)
            MiuixVideoCardContentScale.CropCenter -> TransformOrigin.Center
        },
    )
}

/** Deferred bridge to the top video entry's live Miuix driver. */
internal class MiuixVideoCardTransitionProgress {
    private var topScope: NavTransitionScope? = null

    fun bind(scope: NavTransitionScope) {
        when (scope.role) {
            NavRole.Incoming,
            NavRole.Outgoing,
            -> topScope = scope
            NavRole.Top -> if (topScope == null || topScope?.role == NavRole.Covered) {
                topScope = scope
            }
            NavRole.Covered -> Unit
        }
    }

    fun depthOr(fallback: Float): Float = topScope
        ?.let { resolveMiuixVideoCardDepthProgress(it.relativeDepth) }
        ?: fallback.coerceIn(0f, 1f)

    fun isGestureInProgress(): Boolean = topScope?.gesture != null

    /**
     * Host layout width used by outer morph (`bounds.width / layoutSize.width`).
     * Landing inverse scale must use the same width or land size drifts from the list card.
     */
    fun layoutWidthOr(fallback: Float): Float {
        val w = topScope?.layoutSize?.width?.toFloat() ?: return fallback.coerceAtLeast(1f)
        return w.coerceAtLeast(1f)
    }

    /**
     * 预测返回手势进度（0=开始 → 1=完全提交），无手势时为 null。
     * 供预测返回背景模糊（predictiveBackBackgroundEffect）随手势映射，恢复 0.2.2 链路。
     */
    fun gestureBackProgress(): Float? = topScope?.gesture?.progress
}

/**
 * Video-card morph authored directly against Miuix's shared navigation driver.
 *
 * The video entry is transformed from the click-time card rectangle to the navigation host. The
 * same [NavTransitionScope.relativeDepth] drives push, programmatic pop, predictive back, commit,
 * and cancellation, so there is no AndroidX Navigation3 or AnimatedVisibility compatibility path.
 */
internal fun miuixVideoCardNavTransition(
    sourceBounds: Rect?,
    sourceCornerDp: Int?,
    durationMillis: Int,
    fallback: NavTransition,
    progress: MiuixVideoCardTransitionProgress,
    contentScale: MiuixVideoCardContentScale = MiuixVideoCardContentScale.FillWidthTop,
): NavTransition {
    val bounds = sourceBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return fallback
    val motion = NavMotion(
        commit = NavSettleSpec.Tween(
            durationMillis = durationMillis.coerceAtLeast(1),
            easing = FastOutExtraSlowIn,
        ),
        cancel = NavSettleSpec.Spring(stiffness = 1500f),
        programmatic = NavSettleSpec.Tween(
            durationMillis = durationMillis.coerceAtLeast(1),
            easing = FastOutExtraSlowIn,
        ),
    )
    val corner = sourceCornerDp?.coerceAtLeast(0) ?: 16

    return object : NavTransition {
        override val opaqueDepth: Float = fallback.opaqueDepth
        override val motion: NavMotion = motion

        // Source-page scrim and blur are rendered by the existing depth layer from this same
        // transition's deferred progress. Do not add Miuix's generic dim on top of it.
        override fun scrimFraction(scope: NavTransitionScope): Float = 0f

        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier {
            progress.bind(scope)
            return graphicsLayer {
                val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                val depth = scope.relativeDepth
                if (depth <= 0f) {
                    val morph = resolveMiuixVideoCardDepthProgress(depth)
                    val sourceScaleX = (bounds.width / width).coerceIn(0.05f, 1f)
                    val sourceScaleY = (bounds.height / height).coerceIn(0.05f, 1f)
                    val outerScaleX = sourceScaleX + (1f - sourceScaleX) * morph
                    val outerScaleY = sourceScaleY + (1f - sourceScaleY) * morph
                    scaleX = outerScaleX
                    scaleY = outerScaleY
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = bounds.left.coerceIn(-width, width) * (1f - morph)
                    translationY = bounds.top.coerceIn(-height, height) * (1f - morph)
                    // Geometry belongs to this one flying entry. Never reveal the retained card
                    // at its stationary list position by fading or clipping the navigation entry.
                    // Source-card text is composed by the outgoing detail entry itself; the
                    // retained list card is never lifted from its stationary page.
                    alpha = 1f
                    clip = morph < 0.999f
                    val clipRadii = resolveMiuixVideoCardClipRadii(
                        sourceCornerPx = corner.dp.toPx(),
                        outerScaleX = outerScaleX,
                        outerScaleY = outerScaleY,
                    )
                    shape = MiuixVideoCardClipShape(
                        radiusX = clipRadii.radiusX,
                        radiusY = clipRadii.radiusY,
                    )
                }
            }.graphicsLayer {
                val depth = scope.relativeDepth
                if (depth <= 0f) {
                    val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                    val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                    val morph = resolveMiuixVideoCardDepthProgress(depth)
                    val outerScaleX = (bounds.width / width).coerceIn(0.05f, 1f) +
                        (1f - (bounds.width / width).coerceIn(0.05f, 1f)) * morph
                    val outerScaleY = (bounds.height / height).coerceIn(0.05f, 1f) +
                        (1f - (bounds.height / height).coerceIn(0.05f, 1f)) * morph
                    val compensation = resolveMiuixVideoCardContentCompensation(
                        outerScaleX = outerScaleX,
                        outerScaleY = outerScaleY,
                        contentScale = contentScale,
                    )
                    scaleX = compensation.scaleX
                    scaleY = compensation.scaleY
                    transformOrigin = compensation.transformOrigin
                }
            }.zIndex(1f)
        }
    }
}
