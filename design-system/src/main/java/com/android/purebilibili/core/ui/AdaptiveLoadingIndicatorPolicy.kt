package com.android.purebilibili.core.ui

/**
 * Visual recipe for indeterminate loading chrome.
 *
 * - [MIUIX_INFINITE]: Miuix orbiting-dot [top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator]
 *   for page-level loading.
 * - [MIUIX_CIRCULAR]: Miuix [top.yukonga.miuix.kmp.basic.CircularProgressIndicator]
 *   for compact / inline slots.
 */
enum class AdaptiveLoadingVisual {
    MIUIX_INFINITE,
    MIUIX_CIRCULAR,
}

/**
 * Density of the loading slot.
 *
 * [PAGE] is full-screen / empty-state content loading.
 * [COMPACT] is list footers, buttons, and other tight slots (typically ≤ 32.dp).
 */
enum class AdaptiveLoadingDensity {
    PAGE,
    COMPACT,
}

fun resolveAdaptiveLoadingVisual(
    density: AdaptiveLoadingDensity = AdaptiveLoadingDensity.PAGE,
): AdaptiveLoadingVisual = when (density) {
    AdaptiveLoadingDensity.PAGE -> AdaptiveLoadingVisual.MIUIX_INFINITE
    AdaptiveLoadingDensity.COMPACT -> AdaptiveLoadingVisual.MIUIX_CIRCULAR
}

/**
 * Heuristic: treat sizes at or below this as compact slots (list footers, chips, etc.).
 */
const val AdaptiveLoadingCompactSizeThresholdDp = 32f

fun resolveAdaptiveLoadingDensity(sizeDp: Float?): AdaptiveLoadingDensity {
    if (sizeDp == null) return AdaptiveLoadingDensity.PAGE
    return if (sizeDp <= AdaptiveLoadingCompactSizeThresholdDp) {
        AdaptiveLoadingDensity.COMPACT
    } else {
        AdaptiveLoadingDensity.PAGE
    }
}
