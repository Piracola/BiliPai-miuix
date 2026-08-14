package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable

data class AppNavigationCapabilities(
    val usePlatformSideRail: Boolean,
)

fun resolveAppNavigationCapabilities(): AppNavigationCapabilities = AppNavigationCapabilities(
    usePlatformSideRail = true,
)

@Composable
fun rememberAppNavigationCapabilities(): AppNavigationCapabilities =
    resolveAppNavigationCapabilities()
