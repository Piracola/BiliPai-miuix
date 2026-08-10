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
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeReturnAlpha
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope

internal enum class MiuixVideoCardContentScale {
    FillWidthTop,
    CropCenter,
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

private const val MIUIX_WIDE_VIDEO_CARD_MIN_ASPECT_RATIO = 1.45f

/** Top entry depth is 0 at rest and moves toward -1 while returning. */
internal fun resolveMiuixVideoCardDepthProgress(relativeDepth: Float): Float =
    topProgress(relativeDepth)

/**
 * Keeps the corner circular in screen space while the outer card layer scales non-uniformly.
 * A regular RoundedCornerShape is scaled together with the layer and becomes too small on the
 * compressed axis, which exposes the retained source card at the end of a 16:9 return.
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

/**
 * Wide/16:9 shells cannot geometrically reproduce their source content by scaling the whole
 * detail page. During the final return segment, reveal the real retained source card using the
 * same 68%–94% settle window as source-card chrome.
 *
 * [isGestureSeeking]：预测返回手势 seek 中（未松手提交）保持整层不透明，
 * 避免 morph 接近列表时内容提前淡出、实时画面被源卡色块替换；
 * 松手进入 commit settle 后才走淡出交接。
 */
internal fun resolveMiuixVideoCardReturnContentAlpha(
    sourceBounds: Rect,
    morphProgress: Float,
    isReturning: Boolean,
    isGestureSeeking: Boolean = false,
): Float {
    if (!isReturning) return 1f
    if (isGestureSeeking) return 1f
    val aspectRatio = sourceBounds.width / sourceBounds.height.coerceAtLeast(1f)
    if (aspectRatio < MIUIX_WIDE_VIDEO_CARD_MIN_ASPECT_RATIO) return 1f
    return 1f - resolveVideoCardSourceChromeReturnAlpha(morphProgress)
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
                    // Keep ordinary/vertical cards fully opaque. Wide shells hand off during the
                    // shared 68%–94% source-chrome window, before the card reaches its final slot.
                    // 预测返回手势 seek 中（未松手）不淡出，避免画面提前消失被源卡色块替换。
                    alpha = resolveMiuixVideoCardReturnContentAlpha(
                        sourceBounds = bounds,
                        morphProgress = morph,
                        isReturning = scope.role == NavRole.Outgoing,
                        isGestureSeeking = scope.gesture != null && scope.settle == null,
                    )
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
