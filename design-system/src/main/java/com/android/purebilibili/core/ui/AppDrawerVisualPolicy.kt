package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

enum class AppDrawerContainerTreatment {
    TRANSLUCENT,
    OPAQUE,
}

data class AppDrawerVisualPolicy(
    val containerTreatment: AppDrawerContainerTreatment,
    val profileChevronSizeDp: Int,
)

fun resolveAppDrawerVisualPolicy(
    blurEnabled: Boolean,
): AppDrawerVisualPolicy = AppDrawerVisualPolicy(
    containerTreatment = if (blurEnabled) {
        AppDrawerContainerTreatment.TRANSLUCENT
    } else {
        AppDrawerContainerTreatment.OPAQUE
    },
    profileChevronSizeDp = 20,
)

@Composable
fun rememberAppDrawerVisualPolicy(blurEnabled: Boolean): AppDrawerVisualPolicy {
    return remember(blurEnabled) {
        resolveAppDrawerVisualPolicy(
            blurEnabled = blurEnabled,
        )
    }
}
