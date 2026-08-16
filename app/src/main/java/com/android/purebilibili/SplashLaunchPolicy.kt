// 文件路径: app/src/main/java/com/android/purebilibili/SplashLaunchPolicy.kt
package com.android.purebilibili

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Outline
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * 阶段 5 拆分：MainActivity 的启动图（Splash）动画参数与判定纯函数。
 * 全部与 Activity 生命周期解耦；渲染侧（flyout/realtime blur）由 MainActivity
 * 调用这些判定后执行。
 */
internal fun resolveDrawableAspectRatio(width: Int, height: Int): Float? {
    if (width <= 0 || height <= 0) return null
    return width.toFloat() / height.toFloat()
}

internal fun shouldUseRealtimeSplashBlur(sdkInt: Int): Boolean =
    sdkInt >= Build.VERSION_CODES.S && sdkInt < 36

/**
 * 极简版：启动器图标固定为默认「蓝雪女仆」，不再按启动 alias 类名切换多套图标。
 */
internal fun resolveSplashIconResIdForComponentClassName(className: String?): Int {
    return R.mipmap.ic_launcher_blue_snow_maid
}

@Suppress("DEPRECATION")
internal fun resolveLaunchIconResId(context: Context, launchIntent: Intent?): Int {
    resolveSplashIconResIdForComponentClassName(context::class.java.name)
        .takeIf { it != 0 }
        ?.let { return it }

    resolveSplashIconResIdForComponentClassName(launchIntent?.component?.className)
        .takeIf { it != 0 }
        ?.let { return it }

    val fromLaunchComponent = runCatching {
        launchIntent?.component
            ?.let { context.packageManager.getActivityInfo(it, 0).getIconResource() }
            ?: 0
    }.getOrDefault(0)
    if (fromLaunchComponent != 0) return fromLaunchComponent

    return context.applicationInfo.icon
}

internal fun shouldShowCustomSplashOverlay(
    customSplashEnabled: Boolean,
    splashUri: String
): Boolean {
    // Flyout animation and custom splash wallpaper can coexist:
    // system splash flyout exits first, then custom wallpaper overlay fades out.
    return customSplashEnabled && splashUri.isNotEmpty()
}

internal fun shouldReadCustomSplashPreferences(): Boolean {
    return true
}

internal fun resolveSplashWallpaperAlignmentBias(
    isTabletLayout: Boolean,
    mobileBias: Float,
    tabletBias: Float
): Float {
    return if (isTabletLayout) tabletBias else mobileBias
}

internal fun resolveSplashWallpaperUriForLaunch(
    randomEnabled: Boolean,
    fixedSplashUri: String,
    poolUris: List<String>,
    launchSeed: Long
): String {
    if (!randomEnabled) return fixedSplashUri
    val candidates = poolUris
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
    if (candidates.isEmpty()) return fixedSplashUri
    val index = Math.floorMod(launchSeed, candidates.size.toLong()).toInt()
    return candidates[index]
}

internal fun shouldStartLocalProxyOnAppLaunch(): Boolean = false

internal fun shouldEnableSplashFlyoutAnimation(
    sdkInt: Int,
    hasCompletedOnboarding: Boolean,
    hasAcceptedReleaseDisclaimer: Boolean,
    splashIconAnimationEnabled: Boolean
): Boolean {
    if (!splashIconAnimationEnabled) return false
    if (sdkInt < Build.VERSION_CODES.S) return false
    return hasCompletedOnboarding && hasAcceptedReleaseDisclaimer
}

internal fun shouldKeepSystemSplashForPreload(
    runColdStartSplash: Boolean
): Boolean {
    // The native splash background is still the cold-start handoff when both the optional
    // launcher icon animation and custom wallpaper are disabled.
    return runColdStartSplash
}

internal fun shouldApplySplashRealtimeBlur(
    useRealtimeBlur: Boolean,
    progress: Float
): Boolean = useRealtimeBlur && splashExitBlurProgress(progress) > 0f

internal fun shouldRunColdStartSplash(savedInstanceStatePresent: Boolean): Boolean = !savedInstanceStatePresent

internal fun splashExitDurationMs(): Long = 920L
internal fun splashExitTranslateYDp(): Float = 220f
internal fun splashExitScaleEnd(): Float = 1.12f
internal fun splashExitBlurRadiusEnd(): Float = 24f
internal fun splashMaxKeepOnScreenMs(): Long = 1000L
internal fun customSplashHoldDurationMs(): Long = 1900L
internal fun customSplashFadeDurationMs(): Int = 1450

internal fun customSplashShouldRender(
    showSplash: Boolean,
    overlayAlpha: Float
): Boolean = showSplash || overlayAlpha > 0.01f

internal fun customSplashFadeProgress(overlayAlpha: Float): Float {
    return (1f - overlayAlpha).coerceIn(0f, 1f)
}

internal fun customSplashOverlayScale(fadeProgress: Float): Float {
    val normalized = fadeProgress.coerceIn(0f, 1f)
    return 1f + (0.024f * normalized.pow(1.08f))
}

internal fun customSplashOverlayScrimAlpha(fadeProgress: Float): Float {
    val normalized = fadeProgress.coerceIn(0f, 1f)
    return (0.14f * normalized.pow(1.2f)).coerceIn(0f, 0.16f)
}

internal fun customSplashExtraBlurDp(fadeProgress: Float): Float {
    val normalized = fadeProgress.coerceIn(0f, 1f)
    return (14f * normalized.pow(1.1f)).coerceAtLeast(0f)
}

internal fun splashExitTravelDistancePx(
    splashHeightPx: Int,
    targetSizePx: Int,
    minTravelPx: Float
): Float {
    if (splashHeightPx <= 0) return minTravelPx
    // Center icon needs to pass the top edge and leave some margin to feel like a full fly-out.
    val dynamicTravel = (splashHeightPx / 2f) + targetSizePx + 24f
    return max(minTravelPx, dynamicTravel)
}

internal fun splashExitBlurProgress(progress: Float): Float {
    val normalized = progress.coerceIn(0f, 1f)
    val sharpHoldProgress = 0.10f
    if (normalized <= sharpHoldProgress) return 0f
    val blurProgress = ((normalized - sharpHoldProgress) / (1f - sharpHoldProgress))
        .coerceIn(0f, 1f)
    return blurProgress.pow(1.45f)
}

internal fun splashExitIconAlpha(progress: Float): Float {
    if (progress <= 0.12f) return 1f
    val normalized = ((progress - 0.12f) / 0.88f).coerceIn(0f, 1f)
    return (1f - normalized.pow(1.6f)).coerceIn(0f, 1f)
}

internal fun splashExitBackgroundAlpha(progress: Float): Float {
    if (progress <= 0.18f) return 1f
    val normalized = ((progress - 0.18f) / 0.82f).coerceIn(0f, 1f)
    return (1f - normalized.pow(1.1f)).coerceIn(0f, 1f)
}

internal fun splashFlyoutCornerRadiusPx(sizePx: Int): Float {
    return sizePx.coerceAtLeast(0) * 0.24f
}

internal fun resolveSplashFlyoutTargetSizePx(
    systemIconWidthPx: Int,
    systemIconHeightPx: Int,
    density: Float,
): Int {
    val safeDensity = density.coerceAtLeast(0.1f)
    val fallbackSizePx = (112f * safeDensity).roundToInt().coerceAtLeast(1)
    if (systemIconWidthPx <= 0 || systemIconHeightPx <= 0) return fallbackSizePx

    val shortSidePx = minOf(systemIconWidthPx, systemIconHeightPx)
    val longSidePx = maxOf(systemIconWidthPx, systemIconHeightPx)
    val isLauncherLikeSquare = longSidePx <= shortSidePx * 1.2f
    val shortSideDp = shortSidePx / safeDensity
    return if (isLauncherLikeSquare && shortSideDp in 72f..160f) {
        shortSidePx
    } else {
        fallbackSizePx
    }
}

internal fun applySplashFlyoutRoundedClip(view: View) {
    view.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val sizePx = minOf(view.width, view.height)
            outline.setRoundRect(
                0,
                0,
                view.width,
                view.height,
                splashFlyoutCornerRadiusPx(sizePx)
            )
        }
    }
    view.clipToOutline = true
    view.invalidateOutline()
}

internal fun splashTrailPrimaryAlpha(progress: Float): Float {
    val normalized = progress.coerceIn(0f, 1f)
    if (normalized <= 0.08f) return 0f
    val trailProgress = ((normalized - 0.08f) / 0.92f).coerceIn(0f, 1f)
    return (0.34f * (1f - trailProgress).pow(1.15f)).coerceIn(0f, 1f)
}

internal fun splashTrailSecondaryAlpha(progress: Float): Float {
    val normalized = progress.coerceIn(0f, 1f)
    if (normalized <= 0.16f) return 0f
    val trailProgress = ((normalized - 0.16f) / 0.84f).coerceIn(0f, 1f)
    return (0.2f * (1f - trailProgress).pow(1.22f)).coerceIn(0f, 1f)
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun applySplashRealtimeBlur(
    animatedTarget: View,
    primaryTrailView: View?,
    secondaryTrailView: View?,
    radius: Float
) {
    animatedTarget.setRenderEffect(
        RenderEffect.createBlurEffect(
            radius * 0.62f,
            radius * 0.62f,
            Shader.TileMode.CLAMP
        )
    )
    primaryTrailView?.setRenderEffect(
        RenderEffect.createBlurEffect(
            radius,
            radius,
            Shader.TileMode.CLAMP
        )
    )
    secondaryTrailView?.setRenderEffect(
        RenderEffect.createBlurEffect(
            radius * 1.2f,
            radius * 1.2f,
            Shader.TileMode.CLAMP
        )
    )
}

@RequiresApi(Build.VERSION_CODES.S)
internal fun clearSplashRealtimeBlur(
    animatedTarget: View,
    primaryTrailView: View?,
    secondaryTrailView: View?
) {
    animatedTarget.setRenderEffect(null)
    primaryTrailView?.setRenderEffect(null)
    secondaryTrailView?.setRenderEffect(null)
}

internal enum class SplashFlyoutTargetType {
    SYSTEM_ICON,
    FALLBACK_ICON,
    SPLASH_ROOT
}

internal fun resolveSplashFlyoutTargetType(
    hasSystemIcon: Boolean,
    hasFallbackIcon: Boolean
): SplashFlyoutTargetType {
    return when {
        hasSystemIcon -> SplashFlyoutTargetType.SYSTEM_ICON
        hasFallbackIcon -> SplashFlyoutTargetType.FALLBACK_ICON
        else -> SplashFlyoutTargetType.SPLASH_ROOT
    }
}

internal fun shouldLogWarmResume(
    hasCompletedInitialResume: Boolean,
    isChangingConfigurations: Boolean
): Boolean {
    return hasCompletedInitialResume && !isChangingConfigurations
}

internal fun resolveMainActivitySystemInDarkTheme(uiMode: Int): Boolean {
    return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

internal fun shouldRefreshMainActivitySystemThemeSnapshot(
    previousSystemInDark: Boolean,
    currentSystemInDark: Boolean
): Boolean {
    return previousSystemInDark != currentSystemInDark
}
