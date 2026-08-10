package com.android.purebilibili.feature.list

import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.ui.AppTopChromePolicy
internal const val HISTORY_FILTER_LIQUID_DOCK_HEIGHT_DP = 58
internal const val HISTORY_FILTER_LIQUID_DOCK_INDICATOR_HEIGHT_DP = 54
internal const val HISTORY_FILTER_LIQUID_DOCK_LABEL_FONT_SIZE_SP = 15

internal data class HistoryFilterTabChromeSpec(
    val useLiquidDock: Boolean,
    val heightDp: Int,
    val indicatorHeightDp: Int,
    val itemWidthDp: Int?,
    val horizontalPaddingDp: Int,
    val labelFontSizeSp: Int,
    val dragSelectionEnabled: Boolean
)

internal fun shouldUseHistoryFilterLiquidDock(
    androidNativeLiquidGlassEnabled: Boolean
): Boolean = androidNativeLiquidGlassEnabled

internal fun resolveHistoryFilterTabItemWidthDp(filterCount: Int): Int {
    return when {
        filterCount >= 5 -> 56
        filterCount >= 4 -> 60
        else -> 66
    }
}

internal fun resolveHistoryFilterTabChromeSpec(
    homeSettings: HomeSettings,
    topChromePolicy: AppTopChromePolicy,
    filterCount: Int = HistoryContentFilter.entries.size
): HistoryFilterTabChromeSpec {
    val useLiquidDock = shouldUseHistoryFilterLiquidDock(
        androidNativeLiquidGlassEnabled = homeSettings.androidNativeLiquidGlassEnabled
    )
    val compactChrome = topChromePolicy.compactChromeSpec
    return if (useLiquidDock) {
        HistoryFilterTabChromeSpec(
            useLiquidDock = true,
            heightDp = HISTORY_FILTER_LIQUID_DOCK_HEIGHT_DP,
            indicatorHeightDp = HISTORY_FILTER_LIQUID_DOCK_INDICATOR_HEIGHT_DP,
            // 全宽 dock：固定 itemWidth 会把指示器压扁，导致整体显得过小。
            itemWidthDp = null,
            horizontalPaddingDp = 16,
            labelFontSizeSp = HISTORY_FILTER_LIQUID_DOCK_LABEL_FONT_SIZE_SP,
            // Page-level navigation remains tap-first so the selector never competes
            // with Android's predictive back gesture at either screen edge.
            dragSelectionEnabled = false
        )
    } else {
        HistoryFilterTabChromeSpec(
            useLiquidDock = false,
            heightDp = compactChrome.chipHeightDp,
            indicatorHeightDp = 30,
            itemWidthDp = resolveHistoryFilterTabItemWidthDp(filterCount),
            horizontalPaddingDp = 12,
            labelFontSizeSp = HISTORY_FILTER_LIQUID_DOCK_LABEL_FONT_SIZE_SP,
            dragSelectionEnabled = false
        )
    }
}
