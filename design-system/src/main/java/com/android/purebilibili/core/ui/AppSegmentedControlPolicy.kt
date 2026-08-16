package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** Default item height for Miuix [AppNativeTabRow] (channel switchers, day chips, …). */
const val AppNativeTabRowHeightDp = 40

/** Default item height for Miuix compact segmented control. */
const val AppMiuixSegmentedItemHeightDp = 40

/**
 * When corner radius ≥ height/2 the control becomes a full capsule ("sausage").
 * Cap preferred radius so 40–48dp bars keep a short flat edge.
 *
 * @param maxRatio fraction of control height; 0.3 → 48dp max ~14.4dp, 40dp max ~12dp.
 */
fun resolveHeightCappedCornerRadius(
    controlHeight: Dp,
    preferred: Dp,
    maxRatio: Float = 0.3f,
): Dp {
    if (controlHeight <= 0.dp) return preferred
    val capPx = controlHeight.value * maxRatio.coerceIn(0.15f, 0.45f)
    return min(preferred.value, capPx).dp
}

data class AppSegmentedControlPolicy(
    val usesNativeTabRow: Boolean,
    /** Preferred item corner; still height-capped at render time. */
    val pillCornerRadius: Dp,
    val nativeTabRowHeight: Dp,
    val segmentedItemHeight: Dp,
)

internal fun resolveAppSegmentedControlPolicy(): AppSegmentedControlPolicy {
    // Prefer Card-level corners, never full Pill (22–28dp) which saturates 40–48dp bars.
    val preferred = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Card,
    )
    val tabHeight = AppNativeTabRowHeightDp.dp
    val segmentHeight = AppMiuixSegmentedItemHeightDp.dp
    return AppSegmentedControlPolicy(
        usesNativeTabRow = true,
        pillCornerRadius = resolveHeightCappedCornerRadius(tabHeight, preferred),
        nativeTabRowHeight = tabHeight,
        segmentedItemHeight = segmentHeight,
    )
}

@Composable
fun rememberAppSegmentedControlPolicy(): AppSegmentedControlPolicy {
    return remember {
        resolveAppSegmentedControlPolicy()
    }
}
