package com.android.purebilibili.core.theme

import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography

internal const val MIUIX_CORNER_RADIUS_SCALE = 1.15f

data class AndroidNativeChromeTokens(
    val containerCornerRadiusDp: Int,
    val pillCornerRadiusDp: Int,
    val selectedContainerAlpha: Float,
    val tonalSurfaceElevationDp: Int,
    val denseHorizontalSpacingDp: Int,
    val rowMinTouchTargetDp: Int,
    val expressiveMotionDurationMillis: Int,
    val motionScale: Float,
    val motionStandardMillis: Int,
    val motionEmphasizedMillis: Int
)

fun resolveAndroidNativeChromeTokens(): AndroidNativeChromeTokens = AndroidNativeChromeTokens(
    containerCornerRadiusDp = 20,
    pillCornerRadiusDp = 22,
    selectedContainerAlpha = 0.18f,
    tonalSurfaceElevationDp = 0,
    denseHorizontalSpacingDp = 16,
    rowMinTouchTargetDp = 48,
    expressiveMotionDurationMillis = 180,
    motionScale = 1f,
    motionStandardMillis = 180,
    motionEmphasizedMillis = 240
)

fun resolveCornerRadiusScale(): Float = MIUIX_CORNER_RADIUS_SCALE

fun resolveMaterialTypography(): Typography = BiliMiuixTypography

fun resolveMaterialMotionScheme(): MotionScheme = MotionScheme.standard()

fun resolveMaterialShapes(): Shapes = MiuixAlignedShapes