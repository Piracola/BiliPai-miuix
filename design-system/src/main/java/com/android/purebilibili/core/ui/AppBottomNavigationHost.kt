package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class AppBottomNavigationVisualPolicy(
    val liquidGlassEnabled: Boolean,
)

internal fun resolveAppBottomNavigationVisualPolicy(
    androidNativeLiquidGlassEnabled: Boolean,
): AppBottomNavigationVisualPolicy {
    return AppBottomNavigationVisualPolicy(
        liquidGlassEnabled = androidNativeLiquidGlassEnabled,
    )
}

/** Selects the active bottom-navigation implementation (Miuix only). */
@Composable
fun AppBottomNavigationHost(
    androidNativeLiquidGlassEnabled: Boolean,
    platformContent: @Composable (AppBottomNavigationVisualPolicy) -> Unit,
) {
    val policy = remember(androidNativeLiquidGlassEnabled) {
        resolveAppBottomNavigationVisualPolicy(
            androidNativeLiquidGlassEnabled = androidNativeLiquidGlassEnabled,
        )
    }
    platformContent(policy)
}
