package com.android.purebilibili.navigation

internal data class AppNavigationMotionSpec(
    val fastFadeDurationMillis: Int,
    val slowFadeDurationMillis: Int,
)

internal fun resolveAppNavigationMotionSpec(): AppNavigationMotionSpec = AppNavigationMotionSpec(
    fastFadeDurationMillis = 120,
    slowFadeDurationMillis = 190,
)
