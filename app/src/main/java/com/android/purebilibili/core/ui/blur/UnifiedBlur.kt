// 文件路径: core/ui/blur/UnifiedBlur.kt
package com.android.purebilibili.core.ui.blur

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.LocalAppThemeConfig
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.adaptive.minMotionTier
import com.android.purebilibili.core.ui.performance.LocalRuntimeVisualGuard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.blur.blurEffect
import dev.chrisbanes.haze.hazeEffect

private val LocalUnifiedBlurIntensity = staticCompositionLocalOf<BlurIntensity?> { null }

internal fun resolveUnifiedBlurIntensity(
    provided: BlurIntensity?,
    fallback: BlurIntensity
): BlurIntensity {
    return provided ?: fallback
}

internal fun resolveUnifiedBlurredEdgeTreatment(shape: Shape?): BlurredEdgeTreatment {
    return if (shape != null) {
        BlurredEdgeTreatment(shape)
    } else {
        BlurredEdgeTreatment.Rectangle
    }
}

@Composable
fun ProvideUnifiedBlurIntensity(
    blurIntensity: BlurIntensity = LocalAppThemeConfig.current.blurIntensity,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalUnifiedBlurIntensity provides blurIntensity,
        content = content
    )
}

@Composable
fun currentUnifiedBlurIntensity(): BlurIntensity {
    val providedBlurIntensity = LocalUnifiedBlurIntensity.current
    return providedBlurIntensity ?: LocalAppThemeConfig.current.blurIntensity
}

/**
 *  统一的模糊Modifier
 * 
 * 自动根据用户设置选择模糊强度
 * 
 * @param hazeState Haze状态
 * @param enabled 是否启用模糊
 * @return 应用了用户偏好模糊的Modifier
 */
@Composable
fun Modifier.unifiedBlur(
    hazeState: HazeState,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape? = null,
    surfaceType: BlurSurfaceType = BlurSurfaceType.GENERIC,
    motionTier: MotionTier = MotionTier.Normal,
    isScrolling: Boolean = false,
    isTransitionRunning: Boolean = false,
    forceLowBudget: Boolean = false
): Modifier {
    if (!enabled) return this
    if (!shouldAllowRenderEffectBackedHazeEffect(Build.VERSION.SDK_INT)) return this

    val blurIntensity = currentUnifiedBlurIntensity()
    // 运行时视觉守卫：连续掉帧时把毛玻璃/液态玻璃一并降级。调用点自带的
    // motionTier / forceLowBudget 语义正交，这里取更保守者而非覆盖。
    val guard = LocalRuntimeVisualGuard.current.value
    val effectiveMotionTier = minMotionTier(motionTier, guard.effectiveMotionTier)
    val effectiveForceLowBudget = forceLowBudget || guard.forceLowBlurBudget

    val budget = remember(
        surfaceType,
        effectiveMotionTier,
        isScrolling,
        isTransitionRunning,
        effectiveForceLowBudget,
    ) {
        resolveBlurBudget(
            surfaceType = surfaceType,
            motionTier = effectiveMotionTier,
            isScrolling = isScrolling,
            isTransitionRunning = isTransitionRunning,
            forceLowBudget = effectiveForceLowBudget
        )
    }

    // 根据用户选择获取对应的模糊样式（getBlurStyle 自身是 @Composable，内部读主题色）
    val blurStyle = BlurStyles.getBlurStyle(blurIntensity, budget)
    val edgeTreatment = remember(shape) { resolveUnifiedBlurredEdgeTreatment(shape) }
    val inputScaleFactor = remember(budget, surfaceType) {
        resolveBlurInputScale(budget = budget, surfaceType = surfaceType)
    }

    // Haze 2: style/blurEnabled/blurredEdgeTreatment live on BlurVisualEffect via blurEffect {}.
    // Shape still applied with clip; recoverable background gate is per-effect blurEnabled.
    val recoverableEnabled = recoverableBlurEnabled(hazeState)
    return (if (shape != null) this.clip(shape) else this).hazeEffect(
        state = hazeState,
    ) {
        blurEffect {
            style = blurStyle
            blurEnabled = recoverableEnabled
            blurredEdgeTreatment = edgeTreatment
        }
        @OptIn(ExperimentalHazeApi::class)
        run {
            inputScale = if (inputScaleFactor >= 1f) {
                HazeInputScale.None
            } else {
                HazeInputScale.Fixed(inputScaleFactor)
            }
        }
    }
}
